package com.censocustody.android.data.models.approvalV2

import com.google.gson.*
import java.lang.reflect.Type

class ApprovalRequestV2Deserializer : JsonDeserializer<ApprovalRequestV2> {
    private fun getGson(): Gson = GsonBuilder()
        .registerTypeAdapterFactory(ApprovalRequestDetailsV2.approvalRequestDetailsV2AdapterFactory)
        .registerTypeAdapterFactory(ApprovalRequestDetailsV2.OnChainPolicy.onChainPolicyAdapterFactory)
        .registerTypeAdapterFactory(ApprovalRequestDetailsV2.EvmTokenInfo.evmTokenInfoAdapterFactory)
        .registerTypeAdapterFactory(ApprovalRequestDetailsV2.SigningData.signingDataAdapterFactory)
        .registerTypeAdapterFactory(ApprovalSignature.approvalSignatureAdapterFactory)
        .create()

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ApprovalRequestV2 {
        return try {
            getGson().fromJson(json, ApprovalRequestV2::class.java)
        } catch (e: Exception) {
            ApprovalRequestV2("", "", "", "", 0L,
                details=ApprovalRequestDetailsV2.UnknownApprovalType, vaultName=null)
        }
    }

    fun toObjectWithParsedDetails(json: String?) : ApprovalRequestV2 {
        return try {
            getGson().fromJson(json, ApprovalRequestV2::class.java)
        } catch (e: Exception) {
            ApprovalRequestV2("", "", "", "", 0L,
                details=ApprovalRequestDetailsV2.UnknownApprovalType, vaultName=null)
        }
    }
}