package com.censocustody.android.viewModel

import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import com.censocustody.android.data.models.LoginResponse
import com.censocustody.android.data.models.PushBody
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.repository.PushRepository
import com.censocustody.android.data.repository.UserRepository
import com.censocustody.android.data.storage.CensoUserData
import com.censocustody.android.data.validator.EmailValidator
import com.censocustody.android.presentation.token_sign_in.TokenSignInViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import javax.crypto.Cipher

@OptIn(ExperimentalCoroutinesApi::class)
class TokenLoginViewModelTest : BaseViewModelTest() {

    private lateinit var tokenSignInViewModel: TokenSignInViewModel

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var pushRepository: PushRepository

    @Mock
    lateinit var keyRepository: KeyRepository

    @Mock
    lateinit var censoUserData: CensoUserData

    @Mock
    lateinit var emailValidator: EmailValidator

    @Mock
    lateinit var cipher: Cipher

    private val dispatcher = TestCoroutineDispatcher()

    private val validEmail = "sam@samso.com"
    private val invalidEmail = "sam"
    private val validToken = "123456"
    private val invalidToken = ""

    private val returnedToken = "LHKJGFhgcvjbkn678HFGV47658967HJKGFFG"

    val exampleLoginResponse = LoginResponse(token = returnedToken)

    private val pushToken = "9867543267890"
    private val deviceId = "647477534GDDH6669689"

    @Before
    override fun setUp() = runTest {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        tokenSignInViewModel =
            TokenSignInViewModel(
                userRepository = userRepository,
                pushRepository = pushRepository,
                keyRepository = keyRepository,
                censoUserData = censoUserData,
                emailValidator = emailValidator
            )

        whenever(userRepository.userLoggedIn()).then { false }

        whenever(emailValidator.validEmail(invalidEmail)).then { false }
        whenever(emailValidator.validEmail(validEmail)).then { true }

        whenever(pushRepository.getDeviceId()).then { deviceId }
        whenever(pushRepository.retrievePushToken()).then { pushToken }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `user already logged in, then kick them out of flow`() =
        runTest {

            whenever(userRepository.userLoggedIn()).then { true }

            tokenSignInViewModel.onStart(validEmail, validToken)

            verify(emailValidator, times(0)).validEmail(any())
            verify(userRepository, times(0)).loginWithVerificationToken(any(), any())

            assert(tokenSignInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `invalid email on start kicks user out`() =
        runTest {
            tokenSignInViewModel.onStart(invalidEmail, validToken)

            verify(userRepository, times(0)).loginWithVerificationToken(any(), any())

            assert(tokenSignInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `invalid token on start kicks user out`() =
        runTest {
            tokenSignInViewModel.onStart(validEmail, invalidToken)

            verify(userRepository, times(0)).loginWithVerificationToken(any(), any())

            assert(tokenSignInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `valid token and valid email attempts login on start kicks user out`() =
        runTest {
            tokenSignInViewModel.onStart(validEmail, validToken)

            verify(userRepository, times(1)).loginWithVerificationToken(validEmail, validToken)
        }


    @Test
    fun `login fails then exit screen`() =
        runTest {
            whenever(userRepository.loginWithVerificationToken(validEmail, validToken)).then {
                Resource.Error<LoginResponse>()
            }

            tokenSignInViewModel.onStart(validEmail, validToken)

            verify(userRepository, times(1)).loginWithVerificationToken(validEmail, validToken)

            assert(tokenSignInViewModel.state.loginResult is Resource.Error)
            assert(tokenSignInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `no token returned login exits screen`() =
        runTest {
            whenever(userRepository.userHasDeviceIdSaved(any())).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithVerificationToken(validEmail, validToken)).then {
                Resource.Success(LoginResponse(""))
            }

            tokenSignInViewModel.onStart(validEmail, validToken)

            //assert we kicked off next step in login
            assertTrue(tokenSignInViewModel.state.loginResult is Resource.Error)
            assertTrue(tokenSignInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `null token returned login exits screen`() =
        runTest {
            whenever(userRepository.userHasDeviceIdSaved(any())).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithVerificationToken(validEmail, validToken)).then {
                Resource.Success(LoginResponse(null))
            }

            tokenSignInViewModel.onStart(validEmail, validToken)

            //assert we kicked off next step in login
            assertTrue(tokenSignInViewModel.state.loginResult is Resource.Error)
            assertTrue(tokenSignInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `valid login triggers successful login`() =
        runTest {
            whenever(userRepository.userHasDeviceIdSaved(any())).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithVerificationToken(validEmail, validToken)).then {
                Resource.Success(exampleLoginResponse)
            }

            whenever(keyRepository.getInitializedCipherForSentinelEncryption()).then { cipher }

            tokenSignInViewModel.onStart(validEmail, validToken)

            //assert we kicked off next step in login
            assertTrue(tokenSignInViewModel.state.loginResult is Resource.Success)
            assertEquals(
                exampleLoginResponse.token,
                tokenSignInViewModel.state.loginResult.data?.token
            )
            assertSavedDataAfterSuccessfulApiLogin()
            assertPushNotificationRegistrationAttempted()
            assertTrue(tokenSignInViewModel.state.triggerBioPrompt is Resource.Success)
            assertEquals(cipher, tokenSignInViewModel.state.triggerBioPrompt.data)
        }

    @Test
    fun `biometry approved during initial login attempts saving sentinel data`() =
        runTest {
            whenever(userRepository.userHasDeviceIdSaved(any())).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithVerificationToken(validEmail, validToken)).then {
                Resource.Success(exampleLoginResponse)
            }

            whenever(keyRepository.getInitializedCipherForSentinelEncryption()).then { cipher }

            tokenSignInViewModel.onStart(validEmail, validToken)

            //assert we kicked off next step in login
            assertTrue(tokenSignInViewModel.state.loginResult is Resource.Success)
            assertEquals(
                exampleLoginResponse.token,
                tokenSignInViewModel.state.loginResult.data?.token
            )
            assertSavedDataAfterSuccessfulApiLogin()
            assertPushNotificationRegistrationAttempted()
            assertTrue(tokenSignInViewModel.state.triggerBioPrompt is Resource.Success)
            assertEquals(cipher, tokenSignInViewModel.state.triggerBioPrompt.data)

            tokenSignInViewModel.biometryApproved(cipher)

            verify(keyRepository, times(1)).saveSentinelData(cipher)
            assertTrue(tokenSignInViewModel.state.exitLoginFlow is Resource.Success)
        }

    @Test
    fun `biometry retry during initial login attempts saving sentinel data`() =
        runTest {
            whenever(userRepository.userHasDeviceIdSaved(any())).then { false }
            whenever(userRepository.userLoggedIn()).then { false }
            whenever(userRepository.loginWithVerificationToken(validEmail, validToken)).then {
                Resource.Success(exampleLoginResponse)
            }

            whenever(keyRepository.getInitializedCipherForSentinelEncryption()).then { cipher }

            tokenSignInViewModel.onStart(validEmail, validToken)

            //assert we kicked off next step in login
            assertTrue(tokenSignInViewModel.state.loginResult is Resource.Success)
            assertEquals(
                exampleLoginResponse.token,
                tokenSignInViewModel.state.loginResult.data?.token
            )
            assertSavedDataAfterSuccessfulApiLogin()
            assertPushNotificationRegistrationAttempted()
            assertTrue(tokenSignInViewModel.state.triggerBioPrompt is Resource.Success)
            assertEquals(cipher, tokenSignInViewModel.state.triggerBioPrompt.data)

            tokenSignInViewModel.biometryFailed()

            assertTrue(tokenSignInViewModel.state.triggerBioPrompt is Resource.Error)

            tokenSignInViewModel.retryBiometry()

            tokenSignInViewModel.biometryApproved(cipher)

            verify(keyRepository, times(1)).saveSentinelData(cipher)
            assertTrue(tokenSignInViewModel.state.exitLoginFlow is Resource.Success)
        }

    private suspend fun assertSavedDataAfterSuccessfulApiLogin() {
        verify(censoUserData, times(1)).setEmail(validEmail)
        verify(userRepository, times(1)).setUserLoggedIn()
        verify(userRepository, times(1)).saveToken(exampleLoginResponse.token!!)
        assertPushNotificationRegistrationAttempted()
    }

    private suspend fun assertPushNotificationRegistrationAttempted() {
        verify(pushRepository, times(1)).retrievePushToken()
        verify(pushRepository, times(1)).getDeviceId()
        verify(pushRepository, times(1)).addPushNotification(
            PushBody(deviceId = deviceId, token = pushToken)
        )
    }


}