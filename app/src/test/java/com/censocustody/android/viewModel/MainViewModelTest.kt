package com.censocustody.android.viewModel

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED
import com.nhaarman.mockitokotlin2.whenever
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.BiometricUtil
import com.censocustody.android.common.Resource
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_STATIC_DATA
import com.censocustody.android.data.KeyRepository
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.CipherRepository
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
    lateinit var encryptionCipher: Cipher

    @Mock
    lateinit var cipherRepository: CipherRepository

    @Mock
    lateinit var decryptionCipher: Cipher

    private lateinit var mainViewModel: MainViewModel

    private val dispatcher = StandardTestDispatcher()

    //region Before After Work
    @Before
    override fun setUp() = runTest {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        whenever(cipherRepository.getCipherForBackgroundDecryption()).then { decryptionCipher }
        whenever(cipherRepository.getCipherForEncryption(SENTINEL_KEY_NAME)).then { encryptionCipher }

        mainViewModel = MainViewModel(
            userRepository = userRepository,
            keyRepository = keyRepository,
            cipherRepository = cipherRepository
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

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(decryptionCipher, mainViewModel.state.bioPromptTrigger.data)
            assertEquals(false, mainViewModel.state.biometryTooManyAttempts)
            assertEquals(BioPromptReason.FOREGROUND_RETRIEVAL, mainViewModel.state.bioPromptReason)
        }

    @Test
    fun `trigger biometry to save sentinel data if user is logged in and we have sentinel data`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { false }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(encryptionCipher, mainViewModel.state.bioPromptTrigger.data)
            assertEquals(false, mainViewModel.state.biometryTooManyAttempts)
            assertEquals(BioPromptReason.FOREGROUND_SAVE, mainViewModel.state.bioPromptReason)
        }


    @Test
    fun `biometry approval to retrieve sentinel data clears blocking UI`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { true }
            whenever(keyRepository.retrieveSentinelData(decryptionCipher)).then { SENTINEL_STATIC_DATA }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

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

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)


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

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

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
            whenever(keyRepository.retrieveSentinelData(decryptionCipher)).then { SENTINEL_STATIC_DATA }

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
            whenever(keyRepository.retrieveSentinelData(decryptionCipher)).then { SENTINEL_STATIC_DATA }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            mainViewModel.biometryFailed(-1)

            advanceUntilIdle()

            mainViewModel.retryBiometricGate()

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(decryptionCipher, mainViewModel.state.bioPromptTrigger.data)
            assertEquals(false, mainViewModel.state.biometryTooManyAttempts)
            assertEquals(BioPromptReason.FOREGROUND_RETRIEVAL, mainViewModel.state.bioPromptReason)
        }


    @Test
    fun `biometry failure for save data then retry will kick off biometry again`() =
        runTest {
            whenever(userRepository.userLoggedIn()).then { true }
            whenever(keyRepository.haveSentinelData()).then { false }

            mainViewModel.onForeground(BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED)

            advanceUntilIdle()

            mainViewModel.biometryFailed(-1)

            advanceUntilIdle()

            mainViewModel.retryBiometricGate()

            advanceUntilIdle()

            assertTrue(mainViewModel.state.bioPromptTrigger is Resource.Success)
            assertEquals(encryptionCipher, mainViewModel.state.bioPromptTrigger.data)
            assertEquals(false, mainViewModel.state.biometryTooManyAttempts)
            assertEquals(BioPromptReason.FOREGROUND_SAVE, mainViewModel.state.bioPromptReason)
        }
}