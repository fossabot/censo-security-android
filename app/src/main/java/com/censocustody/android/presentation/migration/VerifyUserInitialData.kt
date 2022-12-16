package com.censocustody.android.presentation.migration

import com.google.gson.GsonBuilder
import com.censocustody.android.common.UriWrapper
import com.censocustody.android.data.models.VerifyUser
import java.lang.reflect.Modifier

data class VerifyUserInitialData(val verifyUserDetails: VerifyUser?) {
    companion object {
        fun toJson(
            verifyUserInitialData: VerifyUserInitialData,
            uriWrapper: UriWrapper
        ): String {
            val jsonString = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .toJson(verifyUserInitialData)
            return uriWrapper.encode(jsonString)
        }

        fun fromJson(json: String): VerifyUserInitialData {
            return GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .fromJson(json, VerifyUserInitialData::class.java)
        }
    }
}