package com.censocustody.android.viewModel

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED
import com.nhaarman.mockitokotlin2.whenever
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.util.BiometricUtil
import com.censocustody.android.common.Resource
import com.censocustody.android.data.cryptography.EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.repository.UserRepository
import com.censocustody.android.presentation.semantic_version_check.MainViewModel
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
    lateinit var cipher : Cipher

    private lateinit var mainViewModel: MainViewModel

    private val dispatcher = StandardTestDispatcher()

    //region Before After Work
    @Before
    override fun setUp() = runTest {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        mainViewModel = MainViewModel(
            userRepository = userRepository,
            keyRepository = keyRepository
        )
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
            whenever(keyRepository.getInitializedCipherForSentinelDecryption()).then { cipher }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(false, mainViewModel.state.biometryTooManyAttempts)
            assertEquals(BioPromptReason.FOREGROUND_RETRIEVAL, mainViewModel.state.bioPromptReason)
        }


    @Test
    fun `biometry approval to retrieve sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.getInitializedCipherForSentinelDecryption()).then { cipher }
            whenever(keyRepository.retrieveSentinelData(cipher)).then { SENTINEL_STATIC_DATA }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            mainViewModel.biometryApproved(cipher)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Uninitialized)
            assertEquals(BioPromptReason.UNINITIALIZED, mainViewModel.state.bioPromptReason)
        }

    @Test
    fun `retrieving bad sentinel data does not clear blocking UI and throws error`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.getInitializedCipherForSentinelDecryption()).then { cipher }
            whenever(keyRepository.retrieveSentinelData(cipher)).then { "bad data" }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)


            advanceUntilIdle()

            mainViewModel.biometryApproved(cipher)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
        }

    @Test
    fun `biometry failure for too many attempts to retrieve sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.retrieveSentinelData(cipher)).then { SENTINEL_STATIC_DATA }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            mainViewModel.biometryFailed(BiometricPrompt.ERROR_LOCKOUT)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
            assertEquals(true, mainViewModel.state.biometryTooManyAttempts)
        }


    @Test
    fun `biometry failure for too many attempts to save sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { false }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            mainViewModel.biometryFailed(BiometricPrompt.ERROR_LOCKOUT_PERMANENT)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
            assertEquals(true, mainViewModel.state.biometryTooManyAttempts)
        }

    @Test
    fun `biometry failure for normal error to retrieve sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.retrieveSentinelData(cipher)).then { SENTINEL_STATIC_DATA }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            mainViewModel.biometryFailed(ERROR_USER_CANCELED)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
            assertEquals(false, mainViewModel.state.biometryTooManyAttempts)
        }


    @Test
    fun `biometry failure for normal error to save sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { false }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            mainViewModel.biometryFailed(ERROR_USER_CANCELED)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Error)
            assertEquals(false, mainViewModel.state.biometryTooManyAttempts)
        }

    @Test
    fun `biometry failure for retrieve data then retry will kick off biometry again`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.retrieveSentinelData(cipher)).then { SENTINEL_STATIC_DATA }
            whenever(keyRepository.getInitializedCipherForSentinelDecryption()).then { cipher }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            mainViewModel.biometryFailed(-1)

            advanceUntilIdle()

            mainViewModel.retryBiometricGate()

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(false, mainViewModel.state.biometryTooManyAttempts)
            assertEquals(BioPromptReason.FOREGROUND_RETRIEVAL, mainViewModel.state.bioPromptReason)
        }
}