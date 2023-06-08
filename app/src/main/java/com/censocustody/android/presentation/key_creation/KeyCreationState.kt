package com.censocustody.android.presentation.key_creation

import android.graphics.Bitmap
import com.censocustody.android.common.Resource
import com.censocustody.android.common.UriWrapper
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.presentation.entrance.UserType
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier

data class KeyCreationState(
    val keyGeneratedPhrase: String? = null,
    val triggerBioPrompt: Resource<Unit> = Resource.Uninitialized,
    val finishedKeyUpload: Boolean = false,

    val walletSigners: List<WalletSigner> = emptyList(),

    //API calls
    val uploadingKeyProcess: Resource<Unit> = Resource.Uninitialized,

    val verifyUserDetails: VerifyUser? = null,
    val bootstrapUserDeviceImage: Bitmap? = null,

    val userType: UserType = UserType.STANDARD
)

//If user image is filled in, then we are setting up bootstrap user
data class KeyCreationInitialData(
    val verifyUserDetails: VerifyUser?,
    val userType: UserType,
    val bootstrapUserDeviceImageURI : String = ""
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

        fun fromJson(json: String): KeyCreationInitialData {
            return GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .fromJson(json, KeyCreationInitialData::class.java)
        }
    }
}