package com.censocustody.android.viewModel

import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import com.censocustody.android.presentation.token_sign_in.TokenSignInViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

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

    private val dispatcher = TestCoroutineDispatcher()

    private val validEmail = "sam@samso.com"
    private val invalidEmail = "sam"
    private val validToken = "123456"
    private val invalidToken = ""

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
}