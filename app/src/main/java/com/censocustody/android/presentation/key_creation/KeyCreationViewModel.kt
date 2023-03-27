package com.censocustody.android.presentation.key_creation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import com.censocustody.android.data.models.UserImage
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
        triggerBioPromptForAllKeyActivity()
    }

    private fun triggerBioPromptForAllKeyActivity() {
        state = state.copy(triggerBioPrompt = Resource.Success(Unit))
    }

    fun biometryApproved() {
        saveRootSeed()
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

                if (state.verifyUserDetails != null && state.verifyUserDetails?.shardingPolicy == null && state.userImage != null) {
                    uploadBootStrapData(state.userImage!!)
                } else {
                    uploadKeys()
                }
            } catch (e: Exception) {
                state = state.copy(
                    uploadingKeyProcess = Resource.Error(exception = e)
                )
            }
        }
    }

    private suspend fun uploadBootStrapData(userImage: UserImage) {
        val phrase = state.keyGeneratedPhrase ?: throw Exception("Missing Phrase")

        Mnemonics.MnemonicCode(phrase = phrase).toSeed()

        val walletSigners = state.walletSigners

        val bootStrapResource = userRepository.addBootstrapUser(
            userImage = userImage,
            walletSigners = walletSigners,
            rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed()
        )

        state = state.copy(uploadingKeyProcess = bootStrapResource)
    }

    private suspend fun uploadKeys() {
        val walletSigners = state.walletSigners
        val walletSignerResource = userRepository.addWalletSigner(walletSigners)

        state = state.copy(uploadingKeyProcess = walletSignerResource)
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