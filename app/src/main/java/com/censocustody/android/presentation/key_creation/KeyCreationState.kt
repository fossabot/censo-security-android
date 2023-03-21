package com.censocustody.android.presentation.key_creation

import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.common.Resource
import com.censocustody.android.common.UriWrapper
import com.censocustody.android.data.models.UserImage
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.presentation.device_registration.DeviceRegistrationInitialData
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier

data class KeyCreationState(
    val keyGeneratedPhrase: String? = null,
    val triggerBioPrompt: Resource<Unit> = Resource.Uninitialized,
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
    val finishedKeyUpload: Boolean = false,

    val walletSigners: List<WalletSigner> = emptyList(),

    //API calls
    val uploadingKeyProcess: Resource<Unit> = Resource.Uninitialized,
)

data class KeyCreationInitialData(
    val verifyUserDetails: VerifyUser?,
    val userImage: UserImage?
) {
    companion object {
        fun toJson(
            keyCreationInitialData: KeyCreationInitialData,
            uriWrapper: UriWrapper
        ): String {
            val jsonString = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .toJson(keyCreationInitialData)
            return uriWrapper.encode(jsonString)
        }

        fun fromJson(json: String): DeviceRegistrationInitialData {
            return GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .fromJson(json, DeviceRegistrationInitialData::class.java)
        }
    }
}