package com.censocustody.android.presentation.key_creation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.common.Resource
import com.censocustody.android.common.generateUserImageObject
import com.censocustody.android.common.hashOfUserImage
import com.censocustody.android.data.*
import com.censocustody.android.data.models.UserImage
import com.censocustody.android.data.models.VerifyUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeyCreationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val cryptographyManager: CryptographyManager
) : ViewModel() {

    var state by mutableStateOf(KeyCreationState())
        private set

    //region VM SETUP
    fun onStart(verifyUser: VerifyUser?, bootstrapUserDeviceImage: Bitmap?) {
        if (verifyUser != null && bootstrapUserDeviceImage != null) {
            state = state.copy(
                verifyUserDetails = verifyUser,
                bootstrapUserDeviceImage = bootstrapUserDeviceImage,
            )
        } else if (verifyUser != null) {
            state = state.copy(
                verifyUserDetails = verifyUser
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
                val phrase = state.keyGeneratedPhrase ?: throw Exception("Missing phrase when trying to save keys")

                keyRepository.saveV3RootKey(
                    Mnemonics.MnemonicCode(phrase = phrase)
                )

                val walletSigners =
                    keyRepository.saveV3PublicKeys(
                        rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed()
                    )

                state = state.copy(walletSigners = walletSigners)

                if (state.verifyUserDetails != null && state.verifyUserDetails?.shardingPolicy == null && state.bootstrapUserDeviceImage != null) {
                    uploadBootStrapData(state.bootstrapUserDeviceImage!!)
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

    private suspend fun uploadBootStrapData(bitmap: Bitmap) {
        val userEmail = userRepository.retrieveUserEmail()
        val deviceId = SharedPrefsHelper.retrieveDeviceId(email = userEmail)

        //Get user image all ready
        val userImage = generateUserImageObject(
            userPhoto = bitmap,
            keyName = deviceId,
            cryptographyManager = cryptographyManager
        )

        val imageByteArray = BaseWrapper.decodeFromBase64(userImage.image)
        val hashOfImage = hashOfUserImage(imageByteArray)

        val signatureToCheck = BaseWrapper.decodeFromBase64(userImage.signature)

        val verified = cryptographyManager.verifySignature(
            keyName = deviceId,
            dataSigned = hashOfImage,
            signatureToCheck = signatureToCheck
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