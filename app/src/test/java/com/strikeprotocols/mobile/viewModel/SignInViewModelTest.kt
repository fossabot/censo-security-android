package com.strikeprotocols.mobile.viewModel

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.common.BioPromptReason
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.strikeprotocols.mobile.data.models.LoginResponse
import com.strikeprotocols.mobile.data.models.PushBody
import com.strikeprotocols.mobile.presentation.sign_in.LoginStep
import com.strikeprotocols.mobile.presentation.sign_in.SignInViewModel
import junit.framework.TestCase.assertEquals
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
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var pushRepository: PushRepository

    @Mock
    lateinit var strikeUserData: StrikeUserData

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
        whenever(keyRepository.getCipherForPrivateKeyDecryption()).then { cipher }
        whenever(keyRepository.generateTimestamp()).then { timestamp }
        whenever(keyRepository.signTimestamp(any(), any())).then { signedTimestamp }

        whenever(pushRepository.getDeviceId()).then { deviceId }
        whenever(pushRepository.retrievePushToken()).then { pushToken }
    }

    @Test
    fun `retrieve cached email on initialization`() {
        whenever(userRepository.retrieveCachedUserEmail()).then { validEmail }

        initVM()

        assert(signInViewModel.state.email == validEmail)
    }

    @Test
    fun `valid email with no private key moves us to password entry`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { false }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            assert(signInViewModel.state.loginStep == LoginStep.PASSWORD_ENTRY)
        }

    @Test
    fun `sanitize email when inputted by user`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { false }

            initVM()

            signInViewModel.updateEmail("         ${validEmail.toUpperCase()}        ")
            signInViewModel.signInActionCompleted()

            assert(signInViewModel.state.loginStep == LoginStep.PASSWORD_ENTRY)
            assert(signInViewModel.state.email == validEmail)
            verify(userRepository, times(1)).saveUserEmail(validEmail)
        }

    @Test
    fun `invalid email with no private key does not move us to password entry`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { false }

            initVM()

            signInViewModel.updateEmail(invalidEmail)
            signInViewModel.signInActionCompleted()

            assertEquals(LoginStep.EMAIL_ENTRY, signInViewModel.state.loginStep)
            assertEquals(true, signInViewModel.state.emailErrorEnabled)
        }

    @Test
    fun `invalid password does not attempt password based login`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { false }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()
            signInViewModel.updatePassword(invalidPassword)
            signInViewModel.signInActionCompleted()

            verify(userRepository, times(0))
                .loginWithPassword(validEmail, invalidPassword)
            assert(signInViewModel.state.loginResult !is Resource.Loading)
            assertEquals(true, signInViewModel.state.passwordErrorEnabled)
        }

    @Test
    fun `valid password attempts password based login`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { false }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()
            signInViewModel.updatePassword(validPassword)
            signInViewModel.signInActionCompleted()

            verify(userRepository, times(1))
                .loginWithPassword(validEmail, validPassword)
            assert(signInViewModel.state.loginResult is Resource.Loading)
        }

    @Test
    fun `valid password login triggers successful login`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { false }
            whenever(userRepository.loginWithPassword(validEmail, validPassword)).then {
                Resource.Success(LoginResponse(jwt))
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()
            signInViewModel.updatePassword(validPassword)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            assertSuccessfulLogin()
        }

    @Test
    fun `no token returned on password login triggers login error`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { false }
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
            whenever(keyRepository.havePrivateKey()).then { false }
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
            whenever(keyRepository.havePrivateKey()).then { true }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            assert(signInViewModel.state.triggerBioPrompt is Resource.Success)
            assert(signInViewModel.state.triggerBioPrompt.data == cipher)
        }

    @Test
    fun `valid email with a private key but null cipher does not attempt biometric login`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { true }
            whenever(keyRepository.getCipherForPrivateKeyDecryption()).then { null }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            assert(signInViewModel.state.triggerBioPrompt !is Resource.Success)
            assert(signInViewModel.state.bioPromptReason == BioPromptReason.UNINITIALIZED)
        }

    @Test
    fun `biometry approved during return login attempts signature based login`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { true }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryApproved(cipher)

            verify(userRepository, times(1)).loginWithTimestamp(
                validEmail, timestamp, signedTimestamp
            )
            assert(signInViewModel.state.loginResult is Resource.Loading)
        }

    @Test
    fun `biometry approved during initial login attempts saving sentinel data`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { false }
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
            assert(signInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `valid signature based login triggers successful login`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { true }
            whenever(userRepository.loginWithTimestamp(any(), any(), any())).then {
                Resource.Success(LoginResponse(jwt))
            }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryApproved(cipher)

            advanceUntilIdle()

            assertSuccessfulLogin()
        }

    @Test
    fun `no token returned on signature based login triggers login error`() =
        runTest {
            whenever(keyRepository.havePrivateKey()).then { true }
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
            whenever(keyRepository.havePrivateKey()).then { true }
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
            whenever(keyRepository.havePrivateKey()).then { true }

            initVM()

            signInViewModel.updateEmail(validEmail)
            signInViewModel.signInActionCompleted()

            advanceUntilIdle()

            signInViewModel.biometryFailed()

            verify(userRepository, times(0)).loginWithTimestamp(
                validEmail, timestamp, signedTimestamp
            )
            assert(signInViewModel.state.loginResult is Resource.Error)
        }

    private fun initVM() {
        signInViewModel = SignInViewModel(
            keyRepository = keyRepository,
            userRepository = userRepository,
            strikeUserData = strikeUserData,
            pushRepository = pushRepository
        )
    }

    private suspend fun assertSuccessfulLogin() {
        verify(strikeUserData, times(1)).setEmail(validEmail)
        verify(userRepository, times(1)).setUserLoggedIn()
        verify(userRepository, times(1)).saveToken(jwt)
        assert(signInViewModel.state.loginResult is Resource.Success)
        assert(signInViewModel.state.loginResult.data?.token == jwt)
        assertEquals(BioPromptReason.INITIAL_LOGIN, signInViewModel.state.bioPromptReason)
        assert(signInViewModel.state.triggerBioPrompt is Resource.Success)
        assertPushNotificationRegistrationAttempted()
    }

    private fun assertFailedLogin() {
        assert(signInViewModel.state.loginResult is Resource.Error)
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