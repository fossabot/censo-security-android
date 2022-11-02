package com.strikeprotocols.mobile.viewModel

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.StrikeUserData
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.Chain
import com.strikeprotocols.mobile.data.models.Organization
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletPublicKey
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.entrance.EntranceViewModel
import com.strikeprotocols.mobile.presentation.entrance.UserDestination
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class EntranceViewModelTest : BaseViewModelTest() {

    private val dispatcher = TestCoroutineDispatcher()

    lateinit var entranceViewModel: EntranceViewModel

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var keyRepository: KeyRepository

    @Mock
    lateinit var strikeUserData: StrikeUserData

    val email = "legitimate@ok.com"

    private val basicVerifyUserWithNoPublicKeys = VerifyUser(
        fullName = "Jason Jasonson",
        hasApprovalPermission = true,
        useStaticKey = true,
        id = "09876564534",
        loginName = "jasonson",
        organization = Organization(id = "0987659876", name = "Main Company"),
        publicKeys = emptyList()
    )

    private val validPublicKey = WalletPublicKey(
        chain = Chain.solana,
        key = "F7JuLRBbyGAS9nAhDdfNX1LbckBAmCnKMB2xTdZfQS1n"
    )

    private val basicVerifyUserWithValidPublicKey =
        basicVerifyUserWithNoPublicKeys.copy(publicKeys = listOf(validPublicKey))

    private val validWalletSigners = listOf(
        WalletSigner(chain = Chain.solana, publicKey = validPublicKey.key)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        MockitoAnnotations.openMocks(this)

        entranceViewModel = EntranceViewModel(
            userRepository = userRepository,
            keyRepository = keyRepository,
            strikeUserData = strikeUserData
        )
    }

    @Test
    fun `if user is not logged in then send them to login`() = runTest {
        assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

        whenever(userRepository.userLoggedIn()).then { false }

        entranceViewModel.onStart()

        assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
        assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.LOGIN)
    }

    @Test
    fun `if user is logged in then we set email value on the strike user data`() = runTest {
        setupLoggedInUserWithValidEmail()

        entranceViewModel.onStart()

        verify(strikeUserData, times(1)).setEmail(email)
    }

    @Test
    fun `if old key is present then send user to migration destination`() = runTest {
        setupLoggedInUserWithValidEmail()

        whenever(userRepository.verifyUser()).then {
            Resource.Success(basicVerifyUserWithValidPublicKey)
        }

        whenever(keyRepository.havePrivateKey()).then { false }
        whenever(keyRepository.getDeprecatedPrivateKey()).then { "not empty" }

        entranceViewModel.onStart()
        advanceUntilIdle()

        verify(strikeUserData, times(1))
            .setStrikeUser(basicVerifyUserWithValidPublicKey)

        assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
        assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.KEY_MIGRATION)
    }

    @Test
    fun `if no key is present locally or on backend then send user to create a key destination`() =
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithNoPublicKeys)
            }

            whenever(keyRepository.havePrivateKey()).then { false }
            whenever(keyRepository.getDeprecatedPrivateKey()).then { "" }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(strikeUserData, times(1))
                .setStrikeUser(basicVerifyUserWithNoPublicKeys)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.KEY_MANAGEMENT_CREATION)
        }

    @Test
    fun `if a key is present locally but a key is not present on backend then send user to key regeneration destination`() =
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithNoPublicKeys)
            }

            whenever(keyRepository.havePrivateKey()).then { true }
            whenever(keyRepository.getDeprecatedPrivateKey()).then { "" }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(strikeUserData, times(1))
                .setStrikeUser(basicVerifyUserWithNoPublicKeys)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.KEY_MANAGEMENT_REGENERATION)
        }

    @Test
    fun `if no key is present locally but a key is present on backend then send user to key recovery destination`() =
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithValidPublicKey)
            }

            whenever(keyRepository.havePrivateKey()).then { false }
            whenever(keyRepository.getDeprecatedPrivateKey()).then { "" }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(strikeUserData, times(1))
                .setStrikeUser(basicVerifyUserWithValidPublicKey)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.KEY_MANAGEMENT_RECOVERY)
        }

    @Test
    fun `if key is present locally and matches key on backend then send user to home`() =
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithValidPublicKey)
            }

            whenever(keyRepository.havePrivateKey()).then { true }
            whenever(keyRepository.getDeprecatedPrivateKey()).then { "" }
            whenever(userRepository.getWalletSigners()).then { Resource.Success(validWalletSigners) }
            whenever(keyRepository.doesUserHaveValidLocalKey(basicVerifyUserWithValidPublicKey, validWalletSigners)).then { true }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(strikeUserData, times(1))
                .setStrikeUser(basicVerifyUserWithValidPublicKey)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.HOME)
        }

    @Test
    fun `if key is present locally and does not match key on backend then send user to invalid key`() =
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithValidPublicKey)
            }

            whenever(keyRepository.havePrivateKey()).then { true }
            whenever(keyRepository.getDeprecatedPrivateKey()).then { "" }
            whenever(userRepository.getWalletSigners()).then { Resource.Success(validWalletSigners) }
            whenever(keyRepository.doesUserHaveValidLocalKey(basicVerifyUserWithValidPublicKey, validWalletSigners)).then { false }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(strikeUserData, times(1))
                .setStrikeUser(basicVerifyUserWithValidPublicKey)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.INVALID_KEY)
        }

    @Test
    fun `if wallet signers fails then set user null and do not set a user destination`() =
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithValidPublicKey)
            }

            whenever(keyRepository.havePrivateKey()).then { true }
            whenever(keyRepository.getDeprecatedPrivateKey()).then { "" }
            whenever(userRepository.getWalletSigners()).then {
                Resource.Error<List<WalletSigner?>?>()
            }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(strikeUserData, times(1))
                .setStrikeUser(null)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)
        }

    @Test
    fun `if verify user fails then set user null and do not set a user destination`() =
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Error<VerifyUser>()
            }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(strikeUserData, times(1))
                .setStrikeUser(null)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)
        }

    @Test
    fun `can continue to home after initial verify user failure`() =
        runBlocking {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Error<VerifyUser>()
            }

            whenever(keyRepository.havePrivateKey()).then { true }
            whenever(keyRepository.getDeprecatedPrivateKey()).then { "" }
            whenever(userRepository.getWalletSigners()).then { Resource.Success(validWalletSigners) }
            whenever(keyRepository.doesUserHaveValidLocalKey(basicVerifyUserWithValidPublicKey, validWalletSigners)).then { true }

            entranceViewModel.onStart()

            verify(strikeUserData, times(1))
                .setStrikeUser(null)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithValidPublicKey)
            }

            entranceViewModel.retryRetrieveVerifyUserDetails()

            verify(strikeUserData, times(1))
                .setStrikeUser(basicVerifyUserWithValidPublicKey)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.HOME)
        }

    @Test
    fun `can continue to home after initial wallet signers failure`() {
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithValidPublicKey)
            }

            whenever(keyRepository.havePrivateKey()).then { true }
            whenever(keyRepository.getDeprecatedPrivateKey()).then { "" }
            whenever(userRepository.getWalletSigners()).then { Resource.Error<List<WalletSigner?>?>() }
            whenever(
                keyRepository.doesUserHaveValidLocalKey(
                    basicVerifyUserWithValidPublicKey,
                    validWalletSigners
                )
            ).then { true }

            entranceViewModel.onStart()

            verify(strikeUserData, times(1))
                .setStrikeUser(null)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            whenever(userRepository.getWalletSigners()).then { Resource.Success(validWalletSigners) }

            entranceViewModel.retryRetrieveVerifyUserDetails()

            verify(strikeUserData, times(2))
                .setStrikeUser(basicVerifyUserWithValidPublicKey)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.HOME)
        }
    }

    private suspend fun setupLoggedInUserWithValidEmail() {
        whenever(userRepository.retrieveCachedUserEmail()).then { email }
        whenever(userRepository.userLoggedIn()).then { true }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}