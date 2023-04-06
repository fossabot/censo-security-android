package com.censocustody.android.presentation.key_recovery

import com.censocustody.android.common.Resource
import com.censocustody.android.common.UriWrapper
import com.censocustody.android.data.models.GetRecoveryShardsResponse
import com.censocustody.android.data.models.VerifyUser
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier

data class KeyRecoveryState(
    val recoverKeyProcess: Resource<RecoveryError?> = Resource.Uninitialized,
    val verifyUserDetails: VerifyUser? = null,
    val triggerBioPrompt: Resource<Unit> = Resource.Uninitialized,
    val recoverShardsData: GetRecoveryShardsResponse? = null,
)

enum class RecoveryError {
    FAILED_DECRYPT, INVALID_ROOT_SEED, MISSING_DATA, BIOMETRY_FAILED, FAILED_RETRIEVE_SHARDS, SAVE_FAILED
}

data class KeyRecoveryInitialData(
    val verifyUserDetails: VerifyUser?,
) {
    companion object {
        fun toJson(
            keyRecoveryInitialData: KeyRecoveryInitialData,
            uriWrapper: UriWrapper
        ): String {
            val jsonString = GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .toJson(keyRecoveryInitialData)
            return uriWrapper.encode(jsonString)
        }

        fun fromJson(json: String): KeyRecoveryInitialData {
            return GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .create()
                .fromJson(json, KeyRecoveryInitialData::class.java)
        }
    }
}