package com.censocustody.android.viewModel

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.censocustody.android.data.models.CipherRepository
import com.censocustody.android.data.models.LoginResponse
import com.censocustody.android.data.models.PushBody
import com.censocustody.android.presentation.sign_in.LoginStep
import com.censocustody.android.presentation.sign_in.SignInViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import javax.crypto.Cipher


@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest : BaseViewModelTest() {

    private val dispatcher = TestCoroutineDispatcher()

    lateinit var signInViewModel: SignInViewModel

    @Mock
    lateinit var keyRepository: KeyRepository

    @Mock
    lateinit var cipherRepository: CipherRepository

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var pushRepository: PushRepository

    @Mock
    lateinit var censoUserData: CensoUserData

    @Mock
    lateinit var cipher: Cipher

    private val validEmail = "sam@ok.com"
    private val invalidEmail = ""
    private val validPassword = "ez4GK8testing!"
    private val invalidPassword = ""

    private val timestamp = "2022-09-12T17:23:58.407+00:00"
    private val signedTimestamp = "lkhjgfds5665ertyghjvbmnghjkf"
    private val jwt = "SDGFHDJFKGLHNNKBNM764534236475869"

    private val pushToken = "9867543267890"
    private val deviceId = "647477534GDDH6669689"

    @Before
    override fun setUp() = runTest {
        super.setUp()
        Dispatchers.setMain(dispatcher)
        MockitoAnnotations.openMocks(this)

        whenever(userRepository.retrieveCachedUserEmail()).then { "" }
        whenever(cipherRepository.getCipherForV3RootSeedDecryption()).then { cipher }
        whenever(keyRepository.generateTimestamp()).then { timestamp }
        whenever(keyRepository.signTimestamp(any(), any())).then { signedTimestamp }

        whenever(pushRepository.getDeviceId()).then { deviceId }
        whenever(pushRepository.retrievePushToken()).then { pushToken }
    }

    @Test
    fun `retrieve cached email on initialization`() {
        whenever(userRepository.retrieveCachedUserEmail()).then { validEmail }

        initVM()

        assertTrue(signInViewModel.state.email == validEmail)
    }

    @Test
    fun `valid email with no private key moves us to password entry`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            assertTrue(signInViewModel.state.loginStep == LoginStep.PASSWORD_ENTRY)
        }

    @Test
    fun `sanitize email when inputted by user`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }

            initVM()

            signInViewModel.updateEmail("         ${validEmail.uppercase()}        ")
            signInViewModel.signInActionCompleted()

            assertTrue(signInViewModel.state.loginStep == LoginStep.PASSWORD_ENTRY)
            assertTrue(signInViewModel.state.email == validEmail)
            verify(userRepository, times(1)).saveUserEmail(validEmail)
        }

    @Test
    fun `invalid email with no private key does not move us to password entry`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }

            initVM()

            signInViewModel.updateEmail(invalidEmail)
            signInViewModel.signInActionCompleted()

            assertEquals(LoginStep.EMAIL_ENTRY, signInViewModel.state.loginStep)
            assertEquals(true, signInViewModel.state.emailErrorEnabled)
        }

    @Test
    fun `invalid password does not attempt password based login`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()
            signInViewModel.updatePassword(invalidPassword)
            signInViewModel.signInActionCompleted()

            verify(userRepository, times(0))
                .loginWithPassword(validEmail, invalidPassword)
            assertTrue(signInViewModel.state.loginResult !is Resource.Loading)
            assertEquals(true, signInViewModel.state.passwordErrorEnabled)
        }

    @Test
    fun `valid password attempts password based login`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()
            signInViewModel.updatePassword(validPassword)
            signInViewModel.signInActionCompleted()

            verify(userRepository, times(1))
                .loginWithPassword(validEmail, validPassword)
            assertTrue(signInViewModel.state.loginResult is Resource.Loading)
        }

    @Test
    fun `valid password login triggers successful login`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(cipherRepository.getCipherForEncryption(SENTINEL_KEY_NAME)).then { cipher }
            whenever(userRepository.loginWithPassword(validEmail, validPassword)).then {
                Resource.Success(LoginResponse(jwt))
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()
            signInViewModel.updatePassword(validPassword)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            assertSavedDataAfterSuccessfulApiLogin()

            //assert we kicked off next step in login
            assertTrue(signInViewModel.state.loginResult is Resource.Success)
            assertEquals(jwt, signInViewModel.state.loginResult.data?.token)
            assertTrue(signInViewModel.state.triggerBioPrompt is Resource.Success)
            assertEquals(cipher, signInViewModel.state.triggerBioPrompt.data)
            assertEquals(BioPromptReason.SAVE_SENTINEL, signInViewModel.state.bioPromptReason)
        }

    @Test
    fun `no token returned on password login triggers login error`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithPassword(validEmail, validPassword)).then {
                Resource.Success(LoginResponse(null))
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()
            signInViewModel.updatePassword(validPassword)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            assertFailedLogin()
        }

    @Test
    fun `invalid password login triggers login error`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithPassword(validEmail, validPassword)).then {
                Resource.Error<LoginResponse>()
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()
            signInViewModel.updatePassword(validPassword)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            assertFailedLogin()
        }

    @Test
    fun `valid email with a private key attempts biometric login`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(userRepository.userLoggedIn()).then { false }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            assertTrue(signInViewModel.state.triggerBioPrompt is Resource.Success)
            assertTrue(signInViewModel.state.triggerBioPrompt.data == cipher)
        }

    @Test
    fun `valid email with a private key but null cipher does not attempt biometric login`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(cipherRepository.getCipherForV3RootSeedDecryption()).then { null }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            assertTrue(signInViewModel.state.triggerBioPrompt !is Resource.Success)
            assertTrue(signInViewModel.state.bioPromptReason == BioPromptReason.UNINITIALIZED)
        }

    @Test
    fun `biometry approved during return login attempts signature based login`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(userRepository.userLoggedIn()).then { false }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryApproved(cipher)

            verify(userRepository, times(1)).loginWithTimestamp(
                validEmail, timestamp, signedTimestamp
            )
            assertTrue(signInViewModel.state.loginResult is Resource.Loading)
        }

    @Test
    fun `biometry approved during initial login attempts saving sentinel data`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithPassword(validEmail, validPassword)).then {
                Resource.Success(LoginResponse(jwt))
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()
            signInViewModel.updatePassword(validPassword)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryApproved(cipher)

            verify(keyRepository, times(1)).saveSentinelData(cipher)
            assertTrue(signInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `valid signature based login triggers successful login`() =
        runTest {
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(userRepository.loginWithTimestamp(any(), any(), any())).then {
                Resource.Success(LoginResponse(jwt))
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryApproved(cipher)

            advanceUntilIdle()

            assertSavedDataAfterSuccessfulApiLogin()
            assertTrue(signInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `valid signature based login with no sentinel data triggers sentinel data save`() =
        runTest {
            whenever(cipherRepository.getCipherForEncryption(SENTINEL_KEY_NAME)).then { cipher }
            whenever(keyRepository.haveSentinelData()).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(userRepository.loginWithTimestamp(any(), any(), any())).then {
                Resource.Success(LoginResponse(jwt))
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryApproved(cipher)

            advanceUntilIdle()

            assertSavedDataAfterSuccessfulApiLogin()

            advanceUntilIdle()

            assertEquals(LoginStep.EMAIL_ENTRY, signInViewModel.state.loginStep)
            assertEquals(validEmail, signInViewModel.state.email)
            assertTrue(signInViewModel.state.triggerBioPrompt is Resource.Success)
            assertEquals(cipher, signInViewModel.state.triggerBioPrompt.data)
            assertEquals(BioPromptReason.SAVE_SENTINEL, signInViewModel.state.bioPromptReason)
        }

    @Test
    fun `no token returned on signature based login triggers login error`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithTimestamp(any(), any(), any())).then {
                Resource.Success(LoginResponse(null))
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryApproved(cipher)

            advanceUntilIdle()

            assertFailedLogin()
        }

    @Test
    fun `invalid signature based login triggers login error`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithTimestamp(any(), any(), any())).then {
                Resource.Error<LoginResponse>()
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryApproved(cipher)

            advanceUntilIdle()

            assertFailedLogin()
        }

    @Test
    fun `biometry failure does not attempts signature based login`() =
        runTest {
            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(userRepository.userLoggedIn()).then { false }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryFailed()

            verify(userRepository, times(0)).loginWithTimestamp(
                validEmail, timestamp, signedTimestamp
            )
            assertTrue(signInViewModel.state.loginResult is Resource.Error)
        }

    private fun initVM() {
        signInViewModel = SignInViewModel(
            keyRepository = keyRepository,
            userRepository = userRepository,
            censoUserData = censoUserData,
            pushRepository = pushRepository,
            cipherRepository = cipherRepository
        )
    }

    private suspend fun assertSavedDataAfterSuccessfulApiLogin() {
        verify(censoUserData, times(1)).setEmail(validEmail)
        verify(userRepository, times(1)).setUserLoggedIn()
        verify(userRepository, times(1)).saveToken(jwt)
        assertPushNotificationRegistrationAttempted()
    }

    private fun assertFailedLogin() {
        assertTrue(signInViewModel.state.loginResult is Resource.Error)
    }

    private suspend fun assertPushNotificationRegistrationAttempted() {
        verify(pushRepository, times(1)).retrievePushToken()
        verify(pushRepository, times(1)).getDeviceId()
        verify(pushRepository, times(1)).addPushNotification(
            PushBody(deviceId = deviceId, token = pushToken)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

}