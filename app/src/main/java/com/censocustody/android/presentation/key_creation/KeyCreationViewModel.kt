package com.censocustody.android.presentation.key_creation

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.*
import com.censocustody.android.common.ui.hashOfUserImage
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.util.sendError
import com.censocustody.android.common.wrapper.BaseWrapper
import com.censocustody.android.data.models.RecoveryType
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.repository.UserRepository
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
    fun onStart(verifyUser: VerifyUser?, recoveryType: RecoveryType, bootstrapUserDeviceImage: Bitmap?) {
        state = if (verifyUser != null && bootstrapUserDeviceImage != null) {
            state.copy(
                verifyUserDetails = verifyUser,
                recoveryType = recoveryType,
                deviceImage = bootstrapUserDeviceImage,
            )
        } else if (verifyUser != null) {
            state.copy(
                recoveryType = recoveryType,
                verifyUserDetails = verifyUser
            )
        } else {
            state.copy(recoveryType = recoveryType)
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
                val phrase = state.keyGeneratedPhrase ?: throw Exception("Missing phrase when trying to save keys")

                keyRepository.saveV3RootKey(
                    Mnemonics.MnemonicCode(phrase = phrase)
                )

                val walletSigners =
                    keyRepository.saveV3PublicKeys(
                        rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed()
                    )

                state = state.copy(walletSigners = walletSigners)

                if (state.verifyUserDetails != null && state.verifyUserDetails?.shardingPolicy == null && state.deviceImage != null) {
                    uploadBootStrapData(state.deviceImage!!)
                } else {
                    uploadKeys()
                }
            } catch (e: Exception) {
                e.sendError(CrashReportingUtil.KEY_CREATION)
                state = state.copy(
                    uploadingKeyProcess = Resource.Error(exception = e)
                )
            }
        }
    }

    private suspend fun uploadBootStrapData(bitmap: Bitmap) {
        val userEmail = userRepository.retrieveUserEmail()
        val deviceId = userRepository.retrieveUserDeviceId(email = userEmail)

        //Get user image all ready
        val userImage = userRepository.createUserImage(
            userPhoto = bitmap,
            keyName = deviceId,
        )

        val imageByteArray = BaseWrapper.decodeFromBase64(userImage.image)
        val hashOfImage = hashOfUserImage(imageByteArray)

        val signatureToCheck = BaseWrapper.decodeFromBase64(userImage.signature)

        val verified = keyRepository.verifySignature(
            keyName = deviceId,
            signedData = hashOfImage,
            signature = signatureToCheck
        )

        if (!verified) {
            throw Exception("Device image signature not valid.")
        }


        val phrase = state.keyGeneratedPhrase ?: throw Exception("Missing phrase when trying to create bootstrap")

        val walletSigners = state.walletSigners

        val bootStrapResource = userRepository.addBootstrapUser(
            userImage = userImage,
            walletSigners = walletSigners,
            rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed()
        )

        if (bootStrapResource is Resource.Success) {
            userRepository.clearBootstrapImageUrl(userEmail)
        }

        if (bootStrapResource is Resource.Error) {
            (bootStrapResource.exception ?: Exception("Failed to upload bootstrap data"))
                .sendError(CrashReportingUtil.KEY_CREATION)
        }

        state = state.copy(uploadingKeyProcess = bootStrapResource)
    }

    private suspend fun uploadKeys() {
        val phrase = state.keyGeneratedPhrase ?: throw Exception("Missing phrase when trying to upload keys")
        val shardingPolicy = state.verifyUserDetails?.shardingPolicy ?: throw Exception("Missing sharding policy when trying to upload keys")

        val walletSigners = state.walletSigners
        val walletSignerResource = userRepository.addWalletSigner(
            walletSigners = walletSigners,
            policy = shardingPolicy,
            rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed()
        )

        if (walletSignerResource is Resource.Error) {
            (walletSignerResource.exception ?: Exception("Failed to upload device data"))
                .sendError(CrashReportingUtil.KEY_CREATION)
        }

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