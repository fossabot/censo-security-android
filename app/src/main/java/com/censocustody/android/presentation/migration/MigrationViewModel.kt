package com.censocustody.android.presentation.migration

import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.BioCryptoUtil.FAIL_ERROR
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.BioPromptData
import com.censocustody.android.data.KeyRepository
import com.censocustody.android.data.MigrationRepository
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.CipherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.Signature
import javax.crypto.Cipher
import javax.inject.Inject

/***
 *
 * Get root seed. Create all data w/ root seed. Send data to backend.
 *
 * Step 1: Get existing root seed: this involves biometry approval
 * Step 2: Save all new data to v3 storage: public keys
 *  - Will require 1 biometry approval
 * Step 3: Send public keys to backend (sign certain ones if needed)
 *  - Depending if backend does not have certain keys, we may need to sign some local keys when sending
 *
 * Kick off by calling retrieveRootSeed()
 *
 * Step 1:
 * retrieveCipherToDecryptV3RootSeed() -> biometryApproved(RETRIEVE_V3_ROOT_SEED) -> retrieveV3RootSeedAndKickOffKeyStorage()
 *
 * Step 2 + 3 same for all migrations:
 * generateAllNecessaryData() -> biometryApproved(SAVE_V3_ROOT_SEED) -> saveRootSeed() -> biometryApproved(SAVE_V3_KEYS) -> savePrivateAndPublicKeys -> makeApiCallToSaveDataWithBackend()
 */

@HiltViewModel
class MigrationViewModel @Inject constructor(
    private val cipherRepository: CipherRepository,
    private val migrationRepository: MigrationRepository,
    private val keyRepository: KeyRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(MigrationState())
        private set

    //region VM SETUP
    fun onStart(initialData: VerifyUserInitialData) {
        if (state.initialData == null) {
            state = state.copy(
                initialData = initialData,
                verifyUser = initialData.verifyUserDetails,
                addWalletSigner = Resource.Loading()
            )
            viewModelScope.launch {
                retrieveRootSeed()
            }
        }
    }

    //region Step 1: Get Existing Root Seed
    private suspend fun retrieveRootSeed() {
        val haveV3RootSeed = migrationRepository.haveV3RootSeed()

        if (haveV3RootSeed) {
            retrieveCipherToDecryptV3RootSeed()
            return
        }

        //have no data to migrate kick user out
        state = state.copy(kickUserOut = true)
    }

    private suspend fun retrieveCipherToDecryptV3RootSeed() {
        val cipher = cipherRepository.getCipherForV3RootSeedDecryption()
        if (cipher != null) {
            state = state.copy(
                triggerBioPrompt = Resource.Success(CryptoObject(cipher)),
                bioPromptData = BioPromptData(BioPromptReason.RETRIEVE_V3_ROOT_SEED),
            )
        }
    }

    suspend fun triggerBioPromptForDeviceSignature() {
        val userEmail = userRepository.retrieveUserEmail()
        val deviceKeyId = userRepository.retrieveUserDeviceId(userEmail)
        val signature = cipherRepository.getSignatureForDeviceSigning(deviceKeyId)
        if (signature != null) {
            state =
                state.copy(
                    triggerBioPrompt = Resource.Success(CryptoObject(signature)),
                    bioPromptData = BioPromptData(BioPromptReason.RETRIEVE_DEVICE_SIGNATURE),
                )
        }
    }

    //region Step 2: Save new public keys
    private suspend fun retrieveV3RootSeedAndKickOffKeyStorage(cipher: Cipher) {
        val rootSeed = migrationRepository.retrieveV3RootSeed(cipher)

        if (rootSeed == null) {
            state = state.copy(addWalletSigner = Resource.Error())
            return
        }

        migrationRepository.saveV3PublicKeys(rootSeed = rootSeed)
        retrieveWalletSignersAndTriggerDeviceSignatureRetrieval(rootSeed = rootSeed)
    }
    //endregion


    //region Step 3: Save all data
    private suspend fun retrieveWalletSignersAndTriggerDeviceSignatureRetrieval(rootSeed: ByteArray) {
        val verifyUser = state.verifyUser

        if (verifyUser == null) {
            state = state.copy(addWalletSigner = Resource.Error())
            return
        }

        val walletSignersToAdd =
            migrationRepository.retrieveWalletSignersToUpload(rootSeed)

        state = state.copy(walletSigners = walletSignersToAdd)

        triggerBioPromptForDeviceSignature()
    }
    //endregion

    private fun uploadSigners(signature: Signature) {
        viewModelScope.launch {
            //make API call to send up any needed signed keys
            val walletSigner = migrationRepository.migrateSigner(state.walletSigners, signature)

            if (walletSigner is Resource.Success) {
                state = state.copy(finishedMigration = true)
            } else if (walletSigner is Resource.Error) {
                state = state.copy(addWalletSigner = walletSigner)
            }
        }
    }

    //region handle all biometry events
    fun biometryApproved(cryptoObject: CryptoObject) {
        viewModelScope.launch {
            if (state.bioPromptData.bioPromptReason == BioPromptReason.RETRIEVE_V3_ROOT_SEED && cryptoObject.cipher != null) {
                retrieveV3RootSeedAndKickOffKeyStorage(cryptoObject.cipher!!)
            }

            if (state.bioPromptData.bioPromptReason == BioPromptReason.RETRIEVE_DEVICE_SIGNATURE && cryptoObject.signature != null) {
                uploadSigners(cryptoObject.signature!!)
            }
        }
    }
    //endregion

    //region utility
    fun retry() {
        viewModelScope.launch {
            state = state.copy(addWalletSigner = Resource.Loading())
            retrieveRootSeed()
        }
    }

    fun biometryFailed(failedReason: Int? = null) {
        if (failedReason != FAIL_ERROR) {
            state = state.copy(showToast = Resource.Success(""))
            retry()
        }
    }

    fun resetKickOut() {
        state = state.copy(kickUserOut = false)
    }

    fun resetShowToast() {
        state = state.copy(showToast = Resource.Uninitialized)
    }

    fun resetAddWalletSignerCall() {
        state = state.copy(
            addWalletSigner = Resource.Uninitialized,
            finishedMigration = false
        )
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
    //endregion
}