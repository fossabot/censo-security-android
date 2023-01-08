package com.censocustody.android.presentation.key_creation

import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.common.censoLog
import com.censocustody.android.data.*
import com.censocustody.android.data.models.CipherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.Signature
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
        triggerBioPromptForRootSeedSave()
    }

    private suspend fun triggerBioPromptForRootSeedSave() {
        val email = userRepository.retrieveUserEmail()
        val deviceId = userRepository.retrieveUserDeviceId(email)
        val cipher =
            cipherRepository.getCipherForDeviceKeyEncryption(deviceId)
        val bioPromptReason = BioPromptReason.SAVE_V3_ROOT_SEED

        if (cipher != null) {
            state =
                state.copy(
                    triggerBioPrompt = Resource.Success(CryptoObject(cipher)),
                    bioPromptReason = bioPromptReason
                )
        }
    }

    private suspend fun triggerBioPromptForDeviceSignature() {
        val userEmail = userRepository.retrieveUserEmail()
        val deviceKeyId = userRepository.retrieveUserDeviceId(userEmail)
        val signature = cipherRepository.getSignatureForDeviceSigning(deviceKeyId)
        val bioPromptReason = BioPromptReason.RETRIEVE_DEVICE_SIGNATURE
        if (signature != null) {
            state =
                state.copy(
                    triggerBioPrompt = Resource.Success(CryptoObject(signature)),
                    bioPromptReason = bioPromptReason
                )
        } else {
            censoLog(message = "No signature to grab because we did not send user to device registration...")
        }
    }

    fun biometryApproved(cryptoObject: CryptoObject) {
        if (state.bioPromptReason == BioPromptReason.SAVE_V3_ROOT_SEED && cryptoObject.cipher != null) {
            saveRootSeed(cryptoObject.cipher!!)
        }

        if (state.bioPromptReason == BioPromptReason.RETRIEVE_DEVICE_SIGNATURE && cryptoObject.signature != null) {
            uploadKeys(cryptoObject.signature!!)
        }
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

                state = state.copy(walletSigners = walletSigners)

                triggerBioPromptForDeviceSignature()
            } catch (e: Exception) {
                state = state.copy(
                    uploadingKeyProcess = Resource.Error(exception = e)
                )
            }
        }
    }

    private fun uploadKeys(signature: Signature) {
        viewModelScope.launch {
            val walletSigners = state.walletSigners
            val walletSignerResource = userRepository.addWalletSigner(walletSigners, signature)

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