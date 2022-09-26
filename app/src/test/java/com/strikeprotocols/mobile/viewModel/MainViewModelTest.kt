package com.strikeprotocols.mobile.viewModel

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.BioPromptReason
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.OsVersion
import com.strikeprotocols.mobile.data.models.SemanticVersionResponse
import com.strikeprotocols.mobile.presentation.semantic_version_check.MainViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import javax.crypto.Cipher

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest : BaseViewModelTest() {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var keyRepository: KeyRepository

    @Mock
    lateinit var encryptionCipher: Cipher

    @Mock
    lateinit var decryptionCipher: Cipher

    private lateinit var mainViewModel: MainViewModel

    private val dispatcher = StandardTestDispatcher()

    //region Test data
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
    //endregion

    //region Before After Work
    @Before
    override fun setUp() = runTest {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        whenever(keyRepository.getCipherForBackgroundDecryption()).then { decryptionCipher }
        whenever(keyRepository.getCipherForEncryption(SENTINEL_KEY_NAME)).then { encryptionCipher }

        mainViewModel = MainViewModel(
            userRepository = userRepository,
            keyRepository = keyRepository
        )

        currentVersionName = BuildConfig.VERSION_NAME
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    //endregion

    @Test
    fun `trigger biometry to retrieve sentinel data if user is logged in and we have sentinel data`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }

            mainViewModel.onForeground()

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(decryptionCipher, mainViewModel.state.bioPromptTrigger.data)
            assertEquals(false, mainViewModel.state.biometryUnavailable)
            assertEquals(BioPromptReason.FOREGROUND_RETRIEVAL, mainViewModel.state.bioPromptReason)
        }

    @Test
    fun `trigger biometry to save sentinel data if user is logged in and we have sentinel data`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { false }

            mainViewModel.onForeground()

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(encryptionCipher, mainViewModel.state.bioPromptTrigger.data)
            assertEquals(false, mainViewModel.state.biometryUnavailable)
            assertEquals(BioPromptReason.FOREGROUND_SAVE, mainViewModel.state.bioPromptReason)
        }


    @Test
    fun `biometry approval to retrieve sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.retrieveSentinelData(decryptionCipher)).then { SENTINEL_STATIC_DATA }

            mainViewModel.onForeground()

            advanceUntilIdle()

            mainViewModel.biometryApproved(decryptionCipher)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Uninitialized)
            assertEquals(BioPromptReason.UNINITIALIZED, mainViewModel.state.bioPromptReason)
        }

    @Test
    fun `retrieving bad sentinel data does not clear blocking UI and throws error`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.retrieveSentinelData(decryptionCipher)).then { "bad data" }

            mainViewModel.onForeground()

            advanceUntilIdle()

            mainViewModel.biometryApproved(decryptionCipher)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
        }

    @Test
    fun `biometry approval to save sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { false }

            mainViewModel.onForeground()

            advanceUntilIdle()

            mainViewModel.biometryApproved(encryptionCipher)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Uninitialized)
            assertEquals(BioPromptReason.UNINITIALIZED, mainViewModel.state.bioPromptReason)
        }

    @Test
    fun `biometry failure for too many attempts to retrieve sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.retrieveSentinelData(decryptionCipher)).then { SENTINEL_STATIC_DATA }

            mainViewModel.onForeground()

            advanceUntilIdle()

            mainViewModel.biometryFailed(BiometricPrompt.ERROR_LOCKOUT)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
            assertEquals(true, mainViewModel.state.biometryUnavailable)
        }


    @Test
    fun `biometry failure for too many attempts to save sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { false }

            mainViewModel.onForeground()

            advanceUntilIdle()

            mainViewModel.biometryFailed(BiometricPrompt.ERROR_LOCKOUT_PERMANENT)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
            assertEquals(true, mainViewModel.state.biometryUnavailable)
        }

    @Test
    fun `biometry failure for normal error to retrieve sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.retrieveSentinelData(decryptionCipher)).then { SENTINEL_STATIC_DATA }

            mainViewModel.onForeground()

            advanceUntilIdle()

            mainViewModel.biometryFailed(ERROR_USER_CANCELED)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
            assertEquals(false, mainViewModel.state.biometryUnavailable)
        }


    @Test
    fun `biometry failure for normal error to save sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { false }

            mainViewModel.onForeground()

            advanceUntilIdle()

            mainViewModel.biometryFailed(ERROR_USER_CANCELED)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
            assertEquals(false, mainViewModel.state.biometryUnavailable)
        }

    @Test
    fun `biometry failure for retrieve data then retry will kick off biometry again`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.retrieveSentinelData(decryptionCipher)).then { SENTINEL_STATIC_DATA }

            mainViewModel.onForeground()

            advanceUntilIdle()

            mainViewModel.biometryFailed(-1)

            advanceUntilIdle()

            mainViewModel.retryBiometricGate()

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(decryptionCipher, mainViewModel.state.bioPromptTrigger.data)
            assertEquals(false, mainViewModel.state.biometryUnavailable)
            assertEquals(BioPromptReason.FOREGROUND_RETRIEVAL, mainViewModel.state.bioPromptReason)
        }


    @Test
    fun `biometry failure for save data then retry will kick off biometry again`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { false }

            mainViewModel.onForeground()

            advanceUntilIdle()

            mainViewModel.biometryFailed(-1)

            advanceUntilIdle()

            mainViewModel.retryBiometricGate()

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(encryptionCipher, mainViewModel.state.bioPromptTrigger.data)
            assertEquals(false, mainViewModel.state.biometryUnavailable)
            assertEquals(BioPromptReason.FOREGROUND_SAVE, mainViewModel.state.bioPromptReason)
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
    fun `check minimum version is greater than current version then view model should enforce update`() = runTest {
        setMinimumVersionResponse(testHighMinimumVersion)

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        mainViewModel.checkMinimumVersion()

        advanceUntilIdle()

        //Should be true since we have to enforce the update
        assertTrue(mainViewModel.state.shouldEnforceAppUpdate.data == true)
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
    fun `check minimum version is lower than current version then view model should not enforce update`() = runTest {
        setMinimumVersionResponse(testLowMinimumVersion)

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        mainViewModel.checkMinimumVersion()

        advanceUntilIdle()

        //Should still be uninitialized since we do not have to enforce app update
        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
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
    fun `check minimum version is the same as current version then view model should not enforce update`() = runTest {
        setMinimumVersionResponse(currentVersionName)

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        mainViewModel.checkMinimumVersion()

        advanceUntilIdle()

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
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
    fun `check minimum version has higher major number than current version then view model should enforce app update`() = runTest {
        val testHigherMajorVersion = "$higherMajorVersion.$currentMinorVersion.$currentPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testHigherMajorVersion)

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        mainViewModel.checkMinimumVersion()

        advanceUntilIdle()

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate.data == true)
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
    fun `check minimum version has higher minor number than current version then view model should enforce app update`() = runTest {
        val testHigherMinorVersion = "$currentMajorVersion.$higherMinorVersion.$currentPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testHigherMinorVersion)

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        mainViewModel.checkMinimumVersion()

        advanceUntilIdle()

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate.data == true)
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
    fun `check minimum version has higher patch number than current version then view model should enforce app update`() = runTest {
        val testHigherPatchVersion = "$currentMajorVersion.$currentMinorVersion.$higherPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testHigherPatchVersion)

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        mainViewModel.checkMinimumVersion()

        advanceUntilIdle()

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate.data == true)
    }

    @Test
    fun `check if minimum version has lower major number than current major number that view model does not enforce app update`() = runTest {
        val testLowerMajorVersion = "$lowerMajorVersion.$currentMinorVersion.$currentPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testLowerMajorVersion)

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        mainViewModel.checkMinimumVersion()

        advanceUntilIdle()

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
    }

    @Test
    fun `check if minimum version has lower minor number than current minimum number that view model does not enforce app update`() = runTest {
        val testLowerMinorVersion = "$currentMajorVersion.$lowerMinorVersion.$currentPatchVersion"

        setMinimumVersionResponse(minimumVersionResponse = testLowerMinorVersion)

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        mainViewModel.checkMinimumVersion()

        advanceUntilIdle()

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
    }

    @Test
    fun `check if minimum version has lower patch number than current patch number that view model does not enforce app update`() = runTest {
        val testLowerPatchVersion = "$currentMajorVersion.$currentMinorVersion.$lowerPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testLowerPatchVersion)

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        mainViewModel.checkMinimumVersion()

        advanceUntilIdle()

        assertTrue(mainViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
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

}