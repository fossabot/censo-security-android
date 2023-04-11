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
import com.censocustody.android.common.*
import com.censocustody.android.common.CrashReportingUtil.MANUALLY_REPORTED_TAG
import com.censocustody.android.data.*
import com.censocustody.android.data.models.UserImage
import com.censocustody.android.data.models.VerifyUser
import com.raygun.raygun4android.RaygunClient
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
                    uploadStandardKeys()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                RaygunClient.send(e, listOf(MANUALLY_REPORTED_TAG))
                state = state.copy(
                    uploadingKeyProcess = Resource.Error(exception = e)
                )
            }
        }
    }

    private suspend fun uploadBootStrapData(bitmap: Bitmap) {
        val userEmail = userRepository.retrieveUserEmail()

        val keys = createBootstrapKeysForDevice()

        SharedPrefsHelper.saveDevicePublicKey(userEmail, keys.standardDevicePublicKey)

        try {

            //Get user image all ready
            val userImage = state.userImage ?: generateUserImageObject(
                userPhoto = bitmap,
                keyName = keys.standardDevicePublicKey,
                cryptographyManager = cryptographyManager
            )

            state = state.copy(userImage = userImage)

            val imageByteArray = BaseWrapper.decodeFromBase64(userImage.image)
            val hashOfImage = hashOfUserImage(imageByteArray)

            val signatureToCheck = BaseWrapper.decodeFromBase64(userImage.signature)

//        val verified = cryptographyManager.verifySignature(
//            keyName = keys.standardDevicePublicKey,
//            dataSigned = hashOfImage,
//            signatureToCheck = signatureToCheck
//        )
//
//        if (!verified) {
//            throw Exception("Device image signature not valid.")
//        }

            val phrase = state.keyGeneratedPhrase
                ?: throw Exception("Missing phrase when trying to create bootstrap")

            val walletSigners = state.walletSigners

            val bootStrapResource = userRepository.addBootstrapUser(
                userImage = userImage,
                walletSigners = walletSigners,
                rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed(),
                deviceKey = keys.standardDevicePublicKey,
                bootstrapKey = keys.bootstrapPublicKey
            )

            if (bootStrapResource is Resource.Success) {
                userRepository.clearPreviousDeviceInfo(userEmail)

                userRepository.saveDevicePublicKey(
                    email = userEmail,
                    publicKey = keys.standardDevicePublicKey
                )
                userRepository.saveBootstrapDevicePublicKey(
                    email = userEmail,
                    publicKey = keys.bootstrapPublicKey
                )
            }

            if (bootStrapResource is Resource.Error) {
                bootStrapResource.exception?.printStackTrace()
                RaygunClient.send(
                    bootStrapResource.exception ?: Exception("Failed to add bootstrap user"),
                    listOf(MANUALLY_REPORTED_TAG)
                )
            }

            state = state.copy(uploadingKeyProcess = bootStrapResource)
        } catch (e: Exception) {
            SharedPrefsHelper.clearDevicePublicKey(userEmail)
        }
    }

    private fun createBootstrapKeysForDevice(): DevicePublicKeys {
        val standardKeyId = cryptographyManager.createDeviceKeyId()
        val bootstrapKeyId = cryptographyManager.createDeviceKeyId()

        cryptographyManager.getOrCreateKey(keyName = standardKeyId)
        cryptographyManager.getOrCreateKey(keyName = bootstrapKeyId)

        val standardPublicKey =
            cryptographyManager.getPublicKeyFromDeviceKey(keyName = standardKeyId)
        val bootstrapPublicKey =
            cryptographyManager.getPublicKeyFromDeviceKey(keyName = bootstrapKeyId)

        val compressedStandardPublicKey =
            ECIESManager.extractUncompressedPublicKey(standardPublicKey.encoded)

        val compressedBootstrapPublicKey =
            ECIESManager.extractUncompressedPublicKey(bootstrapPublicKey.encoded)

        return DevicePublicKeys(
            standardDevicePublicKey = BaseWrapper.encode(compressedStandardPublicKey),
            bootstrapPublicKey = BaseWrapper.encode(compressedBootstrapPublicKey)
        )
    }

    private suspend fun uploadStandardKeys() {
        val phrase = state.keyGeneratedPhrase ?: throw Exception("Missing phrase when trying to upload keys")
        val shardingPolicy = state.verifyUserDetails?.shardingPolicy ?: throw Exception("Missing sharding policy when trying to upload keys")
        val email = userRepository.retrieveUserEmail()
        val deviceId = userRepository.retrieveUserDeviceId(email)

        val walletSigners = state.walletSigners
        val walletSignerResource = userRepository.addWalletSigner(
            walletSigners = walletSigners,
            policy = shardingPolicy,
            rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed(),
            deviceId = deviceId
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