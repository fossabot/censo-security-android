package com.censocustody.android.presentation.key_creation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeyCreationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
) : ViewModel() {

    var state by mutableStateOf(KeyCreationState())
        private set

    //region VM SETUP
    fun onStart(initialData: KeyCreationInitialData) {
        if(initialData.verifyUserDetails != null && initialData.userImage != null) {
            state = state.copy(
                verifyUserDetails = initialData.verifyUserDetails,
                userImage = initialData.userImage,
            )
        }

        viewModelScope.launch {
            createKeyAndStartSaveProcess()
        }
    }

    fun cleanUp() {
        state = KeyCreationState()
    }

    private suspend fun createKeyAndStartSaveProcess() {
        state = state.copy(uploadingKeyProcess = Resource.Loading())
        val phrase = keyRepository.generatePhrase()
        state = state.copy(keyGeneratedPhrase = phrase)
        triggerBioPromptForRootSeedSave()
    }

    private fun triggerBioPromptForRootSeedSave() {
        val bioPromptReason = BioPromptReason.SAVE_V3_ROOT_SEED

        state =
            state.copy(
                triggerBioPrompt = Resource.Success(Unit),
                bioPromptReason = bioPromptReason
            )
    }

    private fun triggerBioPromptForDeviceSignature() {
        val bioPromptReason = BioPromptReason.RETRIEVE_DEVICE_SIGNATURE
        state =
            state.copy(
                triggerBioPrompt = Resource.Success(Unit),
                bioPromptReason = bioPromptReason
            )
    }

    fun biometryApproved() {
        if (state.bioPromptReason == BioPromptReason.SAVE_V3_ROOT_SEED) {
            saveRootSeed()
        }

        if (state.bioPromptReason == BioPromptReason.RETRIEVE_DEVICE_SIGNATURE) {
            uploadKeys()
        }
    }

    fun biometryFailed() {
        state = state.copy(uploadingKeyProcess = Resource.Error())
    }

    private fun saveRootSeed() {
        viewModelScope.launch {
            try {
                val phrase = state.keyGeneratedPhrase ?: throw Exception("Missing Phrase")

                keyRepository.saveV3RootKey(
                    Mnemonics.MnemonicCode(phrase = phrase)
                )

                val walletSigners =
                    keyRepository.saveV3PublicKeys(
                        rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed()
                    )

                state = state.copy(walletSigners = walletSigners)

                triggerBioPromptForDeviceSignature()
            } catch (e: Exception) {
                state = state.copy(
                    uploadingKeyProcess = Resource.Error(exception = e)
                )
            }
        }
    }


    private fun uploadBootStrapData() {
        viewModelScope.launch {
            val walletSigners = state.walletSigners

            val bootStrapResource = userRepository.addBootstrapUser(walletSigners)
        }
    }

    private fun uploadKeys() {
        viewModelScope.launch {
            val walletSigners = state.walletSigners
            val walletSignerResource = userRepository.addWalletSigner(walletSigners)

            state = state.copy(uploadingKeyProcess = walletSignerResource)
        }
    }

    fun retryKeyCreation() {
        viewModelScope.launch {
            createKeyAndStartSaveProcess()
        }
    }

    fun resetAddWalletSignerCall() {
        state = state.copy(uploadingKeyProcess = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
}