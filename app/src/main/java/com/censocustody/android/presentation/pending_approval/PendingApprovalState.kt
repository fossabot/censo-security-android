package com.censocustody.android.presentation.pending_approval

import com.censocustody.android.common.Resource
import com.censocustody.android.common.UriWrapper
import com.censocustody.android.data.models.VerifyUser
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier

data class PendingApprovalState(
    val verifyUserResult: Resource<VerifyUser> = Resource.Uninitialized,
    val sendUserToEntrance: Resource<Boolean> = Resource.Uninitialized
)


data class PendingApprovalInitialData(
    val verifyUserDetails: VerifyUser?,
) {
    companion object {
        fun toJson(
            deviceRegistrationInitialData: PendingApprovalInitialData,
            uriWrapper: UriWrapper
        ): String {
            val jsonString = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .toJson(deviceRegistrationInitialData)
            return uriWrapper.encode(jsonString)
        }

        fun fromJson(json: String): PendingApprovalInitialData {
            return GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .fromJson(json, PendingApprovalInitialData::class.java)
        }
    }
}