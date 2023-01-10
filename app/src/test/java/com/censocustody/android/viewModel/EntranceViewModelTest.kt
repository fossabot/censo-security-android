package com.censocustody.android.viewModel

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.censocustody.android.BuildConfig
import com.censocustody.android.common.Resource
import com.censocustody.android.data.KeyRepository
import com.censocustody.android.data.CensoUserData
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.Organization
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.models.WalletPublicKey
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.data.models.*
import com.censocustody.android.presentation.entrance.EntranceViewModel
import com.censocustody.android.presentation.entrance.UserDestination
import junit.framework.Assert.assertTrue
import junit.framework.TestCase
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
    lateinit var censoUserData: CensoUserData

    private lateinit var currentVersionName: String

    private val higherMajorVersion = BuildConfig.versionNameMajor + 1
    private val higherMinorVersion = BuildConfig.versionNameMinor + 1
    private val higherPatchVersion = BuildConfig.versionNamePatch + 1

    private val currentMajorVersion = BuildConfig.versionNameMajor
    private val currentMinorVersion = BuildConfig.versionNameMinor
    private val currentPatchVersion = BuildConfig.versionNamePatch

    private val lowerMajorVersion = BuildConfig.versionNameMajor - 1
    private val lowerMinorVersion = BuildConfig.versionNameMinor - 1
    private val lowerPatchVersion = BuildConfig.versionNamePatch - 1

    private val testHighMinimumVersion = "100.0.0"
    private val testLowMinimumVersion = "0.0.0"

    val email = "legitimate@ok.com"

    private val basicVerifyUserWithNoPublicKeys = VerifyUser(
        fullName = "Jason Jasonson",
        hasApprovalPermission = true,
        useStaticKey = true,
        id = "09876564534",
        loginName = "jasonson",
        organization = Organization(id = "0987659876", name = "Main Company"),
        publicKeys = emptyList(),
        deviceKey = "1234519988"
    )

    private val validSolanaPublicKey = WalletPublicKey(
        chain = Chain.solana,
        key = "F7JuLRBbyGAS9nAhDdfNX1LbckBAmCnKMB2xTdZfQS1n"
    )

    private val validBitcoinPublicKey = WalletPublicKey(
        chain = Chain.bitcoin,
        key = ""
    )

    private val validEthereumPublicKey = WalletPublicKey(
        chain = Chain.ethereum,
        key = ""
    )

    private val validCensoPublicKey = WalletPublicKey(
        chain = Chain.censo,
        key = ""
    )

    private val validWalletSigners = listOf(
        WalletSigner(chain = Chain.solana, publicKey = validSolanaPublicKey.key)
    )

    private val basicVerifyUserWithValidPublicKey =
        basicVerifyUserWithNoPublicKeys.copy(
            publicKeys = listOf(
                validSolanaPublicKey,
                validBitcoinPublicKey,
                validEthereumPublicKey,
                validCensoPublicKey
            )
        )

    private val basicVerifyUserWithOnlySolanaPublicKey =
        basicVerifyUserWithNoPublicKeys.copy(
            publicKeys = listOf(
                validSolanaPublicKey,
            )
        )

    @Before
    fun setup() = runTest {
        Dispatchers.setMain(dispatcher)
        MockitoAnnotations.openMocks(this)
        currentVersionName = BuildConfig.VERSION_NAME

        whenever(keyRepository.haveSentinelData()).then { true }
        whenever(userRepository.checkMinimumVersion()).then {
            Resource.Success(
                SemanticVersionResponse(androidVersion = OsVersion(testLowMinimumVersion))
            )
        }
        whenever(keyRepository.retrieveV3PublicKeys()).then { validWalletSigners }
        whenever(keyRepository.hasV3RootSeedStored()).then { true }

        entranceViewModel = EntranceViewModel(
            userRepository = userRepository,
            keyRepository = keyRepository,
            censoUserData = censoUserData
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
    fun `if user is logged in then we set email value on the censo user data`() = runTest {
        setupLoggedInUserWithValidEmail()

        entranceViewModel.onStart()

        verify(censoUserData, times(1)).setEmail(email)
    }

    @Test
    fun `if user does not have sentinel data send them to sign in`() = runTest {
        setupLoggedInUserWithValidEmail()
        whenever(keyRepository.haveSentinelData()).then { false }
        whenever(keyRepository.hasV3RootSeedStored()).then { true }

        whenever(userRepository.verifyUser()).then {
            Resource.Success(basicVerifyUserWithValidPublicKey)
        }

        entranceViewModel.onStart()
        advanceUntilIdle()

        verify(censoUserData, times(1))
            .setCensoUser(basicVerifyUserWithValidPublicKey)

        assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
        assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.LOGIN)
    }

    @Test
    fun `if no key is present locally or on backend then send user to create a key destination`() =
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithNoPublicKeys)
            }

            whenever(keyRepository.hasV3RootSeedStored()).then { false }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(censoUserData, times(1))
                .setCensoUser(basicVerifyUserWithNoPublicKeys)

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

            whenever(keyRepository.hasV3RootSeedStored()).then { true }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(censoUserData, times(1))
                .setCensoUser(basicVerifyUserWithNoPublicKeys)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.REGENERATION)
        }

    @Test
    fun `if no key is present locally but a key is present on backend then send user to key recovery destination`() =
        runTest {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithValidPublicKey)
            }

            whenever(keyRepository.hasV3RootSeedStored()).then { false }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(censoUserData, times(1))
                .setCensoUser(basicVerifyUserWithValidPublicKey)

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

            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(
                keyRepository.doesUserHaveValidLocalKey(basicVerifyUserWithValidPublicKey)
            ).then { true }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(censoUserData, times(1))
                .setCensoUser(basicVerifyUserWithValidPublicKey)

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

            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(
                keyRepository.doesUserHaveValidLocalKey(basicVerifyUserWithValidPublicKey)
            ).then { false }

            entranceViewModel.onStart()
            advanceUntilIdle()

            verify(censoUserData, times(1))
                .setCensoUser(basicVerifyUserWithValidPublicKey)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.INVALID_KEY)
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

            verify(censoUserData, times(1))
                .setCensoUser(null)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)
        }

    @Test
    fun `can continue to home after initial verify user failure`() =
        runBlocking {
            setupLoggedInUserWithValidEmail()

            whenever(userRepository.verifyUser()).then {
                Resource.Error<VerifyUser>()
            }

            whenever(keyRepository.hasV3RootSeedStored()).then { true }
            whenever(keyRepository.doesUserHaveValidLocalKey(basicVerifyUserWithValidPublicKey)).then { true }

            entranceViewModel.onStart()

            verify(censoUserData, times(1))
                .setCensoUser(null)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            whenever(userRepository.verifyUser()).then {
                Resource.Success(basicVerifyUserWithValidPublicKey)
            }

            entranceViewModel.retryRetrieveVerifyUserDetails()

            verify(censoUserData, times(1))
                .setCensoUser(basicVerifyUserWithValidPublicKey)

            assertTrue(entranceViewModel.state.userDestinationResult is Resource.Success)
            assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.HOME)
        }

    /**
     * Test that when we check the minimum app version and it is greater than the current app version,
     * the view model should enforce the app update
     *
     * Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is [Resource.Success] and true
     */
    @Test
    fun `check minimum version is greater than current version then view model should enforce update`() =
        runTest {
            setMinimumVersionResponse(testHighMinimumVersion)

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            entranceViewModel.onStart()

            advanceUntilIdle()

            //Should be true since we have to enforce the update
            TestCase.assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.FORCE_UPDATE)
        }

    /**
     * Test that when we check the minimum app version and it is lower than the current app version,
     * the view model should do nothing and not enforce an app update
     *
     * Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is still [Resource.Uninitialized]
     */
    @Test
    fun `check minimum version is lower than current version then view model should not enforce update`() =
        runTest {
            setMinimumVersionResponse(testLowMinimumVersion)

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            entranceViewModel.onStart()

            advanceUntilIdle()

            //Should still be uninitialized since we do not have to enforce app update
            TestCase.assertTrue(entranceViewModel.state.userDestinationResult.data != UserDestination.FORCE_UPDATE)
        }

    /**
     * Test that when we check the minimum version and it is the same as the current version,
     * the view model should do nothing and not enforce an app update
     *
     * Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is still [Resource.Uninitialized]
     */
    @Test
    fun `check minimum version is the same as current version then view model should not enforce update`() =
        runTest {
            setMinimumVersionResponse(currentVersionName)

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            entranceViewModel.onStart()

            advanceUntilIdle()

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult.data != UserDestination.FORCE_UPDATE)
        }

    /**
     *  Test that when we check the minimum version and it has a higher major version then the current version,
     *  the view model should enforce an app update
     *
     *  Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is [Resource.Success]
     */
    @Test
    fun `check minimum version has higher major number than current version then view model should enforce app update`() =
        runTest {
            val testHigherMajorVersion =
                "$higherMajorVersion.$currentMinorVersion.$currentPatchVersion"
            setMinimumVersionResponse(minimumVersionResponse = testHigherMajorVersion)

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            entranceViewModel.onStart()

            advanceUntilIdle()

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.FORCE_UPDATE)
        }

    /**
     *  Test that when we check the minimum version and it has a higher minor version then the current version,
     *  the view model should enforce an app update
     *
     *  Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is [Resource.Success]
     */
    @Test
    fun `check minimum version has higher minor number than current version then view model should enforce app update`() =
        runTest {
            val testHigherMinorVersion =
                "$currentMajorVersion.$higherMinorVersion.$currentPatchVersion"
            setMinimumVersionResponse(minimumVersionResponse = testHigherMinorVersion)

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            entranceViewModel.onStart()

            advanceUntilIdle()

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.FORCE_UPDATE)
        }

    /**
     *  Test that when we check the minimum version and it has a higher patch version then the current version,
     *  the view model should enforce an app update
     *
     *  Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is [Resource.Success]
     */
    @Test
    fun `check minimum version has higher patch number than current version then view model should enforce app update`() =
        runTest {
            val testHigherPatchVersion =
                "$currentMajorVersion.$currentMinorVersion.$higherPatchVersion"
            setMinimumVersionResponse(minimumVersionResponse = testHigherPatchVersion)

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            entranceViewModel.onStart()

            advanceUntilIdle()

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult.data == UserDestination.FORCE_UPDATE)
        }

    @Test
    fun `check if minimum version has lower major number than current major number that view model does not enforce app update`() =
        runTest {
            val testLowerMajorVersion =
                "$lowerMajorVersion.$currentMinorVersion.$currentPatchVersion"
            setMinimumVersionResponse(minimumVersionResponse = testLowerMajorVersion)

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            entranceViewModel.onStart()

            advanceUntilIdle()

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult.data != UserDestination.FORCE_UPDATE)
        }

    @Test
    fun `check if minimum version has lower minor number than current minimum number that view model does not enforce app update`() =
        runTest {
            val testLowerMinorVersion =
                "$currentMajorVersion.$lowerMinorVersion.$currentPatchVersion"

            setMinimumVersionResponse(minimumVersionResponse = testLowerMinorVersion)

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            entranceViewModel.onStart()

            advanceUntilIdle()

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult.data != UserDestination.FORCE_UPDATE)
        }

    @Test
    fun `check if minimum version has lower patch number than current patch number that view model does not enforce app update`() =
        runTest {
            val testLowerPatchVersion =
                "$currentMajorVersion.$currentMinorVersion.$lowerPatchVersion"
            setMinimumVersionResponse(minimumVersionResponse = testLowerPatchVersion)

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult is Resource.Uninitialized)

            entranceViewModel.onStart()

            advanceUntilIdle()

            TestCase.assertTrue(entranceViewModel.state.userDestinationResult.data != UserDestination.FORCE_UPDATE)
        }

    //Helper methods
    private fun setMinimumVersionResponse(minimumVersionResponse: String) = runTest {
        whenever(userRepository.checkMinimumVersion()).thenAnswer {
            Resource.Success(
                data = SemanticVersionResponse(
                    androidVersion = OsVersion(
                        minimumVersion = minimumVersionResponse
                    )
                )
            )
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