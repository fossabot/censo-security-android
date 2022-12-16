package com.censocustody.android.presentation.key_management

import com.google.gson.GsonBuilder
import com.censocustody.android.common.UriWrapper
import com.censocustody.android.data.models.VerifyUser
import java.lang.reflect.Modifier

data class KeyManagementInitialData(
    val verifyUserDetails: VerifyUser?,
    val flow: KeyManagementFlow
) {
    companion object {
        fun toJson(
            keyManagementInitialData: KeyManagementInitialData,
            uriWrapper: UriWrapper
        ): String {
            val jsonString = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .toJson(keyManagementInitialData)
            return uriWrapper.encode(jsonString)
        }

        fun fromJson(json: String): KeyManagementInitialData {
            return GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .fromJson(json, KeyManagementInitialData::class.java)
        }
    }
}