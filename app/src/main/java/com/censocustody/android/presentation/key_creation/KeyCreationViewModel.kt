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
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.repository.UserRepository
import com.censocustody.android.presentation.entrance.UserType
import com.raygun.raygun4android.RaygunClient
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
    fun onStart(verifyUser: VerifyUser?, bootstrapUserDeviceImage: Bitmap?, userType: UserType) {
        if (verifyUser != null && bootstrapUserDeviceImage != null) {
            state = state.copy(
                verifyUserDetails = verifyUser,
                bootstrapUserDeviceImage = bootstrapUserDeviceImage,
                userType = userType
            )
        } else if (verifyUser != null) {
            state = state.copy(
                verifyUserDetails = verifyUser,
                userType = userType
            )
        } else {
            state = state.copy(
                userType = userType
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

                when (state.userType) {
                    UserType.STANDARD -> uploadKeys()
                    UserType.ORGANIZATION -> uploadOrgKeys(state.bootstrapUserDeviceImage)
                    UserType.BOOTSTRAP -> uploadBootStrapData(state.bootstrapUserDeviceImage)
                }
            } catch (e: Exception) {
                e.sendError(CrashReportingUtil.KEY_CREATION)
                state = state.copy(
                    uploadingKeyProcess = Resource.Error(exception = e)
                )
            }
        }
    }

    private suspend fun uploadBootStrapData(bitmap: Bitmap?) {
        if (bitmap == null) {
            throw Exception("Missing user image to sign for bootstrap user")
        }

        val userEmail = userRepository.retrieveUserEmail()
        val deviceId = userRepository.retrieveUserDeviceId(email = userEmail)

        //Get user image all ready
        val userImage = userRepository.createUserImage(
            userPhoto = bitmap,
            keyName = deviceId,
        )

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

    private suspend fun uploadOrgKeys(bitmap: Bitmap?) {
        val verifyUser = state.verifyUserDetails

        if (bitmap == null) {
            throw Exception("Missing user image or user details when creating org user keys")
        }

        if (verifyUser?.shardingPolicy == null
            || verifyUser.orgAdminInfo?.participantId == null
            || verifyUser.orgAdminInfo.bootstrapParticipantId == null) {
            throw Exception("Missing user user details when creating org user keys")
        }

        val userEmail = userRepository.retrieveUserEmail()

        val orgDeviceId = userRepository.retrieveOrgDeviceId(email = userEmail)
        val orgPublicKey = userRepository.retrieveOrgDevicePublicKey(email = userEmail)

        userRepository.saveDeviceId(email = userEmail, deviceId = orgDeviceId)
        userRepository.saveDevicePublicKey(email = userEmail, publicKey = orgPublicKey)

        //Get user image all ready
        val userImage = userRepository.createUserImage(
            userPhoto = bitmap,
            keyName = orgDeviceId,
        )

        val phrase = state.keyGeneratedPhrase ?: throw Exception("Missing phrase when trying to create org device")

        val walletSigners = state.walletSigners

        val orgAdminRecoveredResource = userRepository.addOrgAdminRecoveredDevice(
            userImage = userImage,
            walletSigners = walletSigners,
            rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed(),
            policy = verifyUser.shardingPolicy,
            shardingParticipantId = verifyUser.orgAdminInfo.participantId,
            bootstrapShardingParticipantId = verifyUser.orgAdminInfo.bootstrapParticipantId
        )

        if (orgAdminRecoveredResource is Resource.Success) {
            userRepository.clearOrgDeviceId(userEmail)
            userRepository.clearBootstrapImageUrl(userEmail)
        }

        if (orgAdminRecoveredResource is Resource.Error) {
            (orgAdminRecoveredResource.exception ?: Exception("Failed to upload org device data"))
                .sendError(CrashReportingUtil.KEY_CREATION)
        }

        state = state.copy(uploadingKeyProcess = orgAdminRecoveredResource)
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