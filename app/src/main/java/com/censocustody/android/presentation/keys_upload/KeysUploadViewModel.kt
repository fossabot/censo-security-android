package com.censocustody.android.presentation.keys_upload

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.BioCryptoUtil.FAIL_ERROR
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import com.censocustody.android.data.models.mapToPublicKeysList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/***
 *
 * User gets here in 2 cases:
 * Case 1: They failed to upload public keys to backend during key creation
 * Case 2: We added a new key to codebase, and user has not uploaded it to backend.
 *
 * Get root seed. Create all data w/ root seed. Send data to backend.
 *
 * Step 1: Get existing root seed (biometry approval)
 * Step 2: Save all public keys to storage
 * Step 3: Sign public keys with device key (biometry approval)
 * Step 4: Upload signed public keys to backend
 */

@HiltViewModel
class KeysUploadViewModel @Inject constructor(
    private val keyRepository: KeyRepository,
    private val userRepository: UserRepository,
    private val securePreferences: SecurePreferences
) : ViewModel() {

    var state by mutableStateOf(KeysUploadState())
        private set

    //region VM SETUP
    fun onStart() {
        viewModelScope.launch {
            retrieveRootSeed()
        }
    }

    //region Step 1: Get Existing Root Seed
    private suspend fun retrieveRootSeed() {
        val haveV3RootSeed = keyRepository.haveV3RootSeed()

        if (haveV3RootSeed) {
            retrieveCipherToDecryptV3RootSeed()
            return
        }

        //have no data to upload kick user out
        state = state.copy(kickUserOut = true)
    }

    private fun retrieveCipherToDecryptV3RootSeed() {
        state = state.copy(
            triggerBioPrompt = Resource.Success(Unit),
            bioPromptData = BioPromptData(BioPromptReason.RETRIEVE_V3_ROOT_SEED),
        )
    }

    private fun triggerBioPromptForDeviceSignature() {
        state =
            state.copy(
                triggerBioPrompt = Resource.Success(Unit),
                bioPromptData = BioPromptData(BioPromptReason.RETRIEVE_DEVICE_SIGNATURE),
            )
    }

    //region Step 2: Save new public keys
    private suspend fun retrieveV3RootSeedAndKickOffKeyStorage() {
        val rootSeed = keyRepository.retrieveV3RootSeed()

        if (rootSeed == null) {
            state = state.copy(addWalletSigner = Resource.Error())
            return
        }

        keyRepository.saveV3PublicKeys(rootSeed = rootSeed)
        retrieveWalletSignersAndTriggerDeviceSignatureRetrieval(rootSeed = rootSeed)
    }
    //endregion


    //region Step 3: Save all data
    private suspend fun retrieveWalletSignersAndTriggerDeviceSignatureRetrieval(rootSeed: ByteArray) {
        val userEmail = userRepository.retrieveUserEmail()
        val publicKeysMap = securePreferences.retrieveV3PublicKeys(userEmail)

        val walletSignersToAdd = keyRepository.signPublicKeys(
            rootSeed = rootSeed,
            publicKeys = publicKeysMap.mapToPublicKeysList()
        )

        state = state.copy(walletSigners = walletSignersToAdd)

        triggerBioPromptForDeviceSignature()
    }
    //endregion

    private fun uploadSigners() {
        viewModelScope.launch {
            //make API call to send up any needed signed keys
            val walletSigner = keyRepository.uploadKeys(state.walletSigners)

            if (walletSigner is Resource.Success) {
                state = state.copy(finishedUpload = true)
            } else if (walletSigner is Resource.Error) {
                state = state.copy(addWalletSigner = walletSigner)
            }
        }
    }

    //region handle all biometry events
    fun biometryApproved() {
        viewModelScope.launch {
            if (state.bioPromptData.bioPromptReason == BioPromptReason.RETRIEVE_V3_ROOT_SEED) {
                retrieveV3RootSeedAndKickOffKeyStorage()
            }

            if (state.bioPromptData.bioPromptReason == BioPromptReason.RETRIEVE_DEVICE_SIGNATURE) {
                uploadSigners()
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
            finishedUpload = false
        )
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
    //endregion
}