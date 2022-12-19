package com.censocustody.android.presentation.key_creation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import com.censocustody.android.data.models.CipherRepository
import com.censocustody.android.data.models.WalletSigner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class KeyCreationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val cipherRepository: CipherRepository,
) : ViewModel() {

    var state by mutableStateOf(KeyCreationState())
        private set

    //region VM SETUP
    fun onStart() {
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
        triggerBioPrompt()
    }

    suspend fun triggerBioPrompt() {
        val cipher =
            cipherRepository.getCipherForEncryption(EncryptionManagerImpl.Companion.ROOT_SEED_KEY_NAME)
        val bioPromptReason = BioPromptReason.SAVE_V3_ROOT_SEED

        if (cipher != null) {
            state =
                state.copy(
                    triggerBioPrompt = Resource.Success(cipher),
                    bioPromptReason = bioPromptReason
                )
        }
    }


    fun biometryApproved(cipher: Cipher) {
        saveRootSeed(cipher)
    }

    fun biometryFailed() {
        state = state.copy(uploadingKeyProcess = Resource.Error())
    }

    private fun saveRootSeed(cipher: Cipher) {
        viewModelScope.launch {
            try {
                val phrase = state.keyGeneratedPhrase ?: throw Exception("Missing Phrase")

                keyRepository.saveV3RootKey(
                    Mnemonics.MnemonicCode(phrase = phrase),
                    cipher = cipher
                )

                val walletSigners =
                    keyRepository.saveV3PublicKeys(mnemonic = Mnemonics.MnemonicCode(phrase = phrase))

                uploadKeys(walletSigners = walletSigners)
            } catch (e: Exception) {
                state = state.copy(
                    uploadingKeyProcess = Resource.Error(exception = e)
                )
            }
        }
    }

    private suspend fun uploadKeys(walletSigners: List<WalletSigner?>) {
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