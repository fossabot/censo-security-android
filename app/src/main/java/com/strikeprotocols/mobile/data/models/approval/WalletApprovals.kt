package com.strikeprotocols.mobile.data.models.approval

import com.google.gson.GsonBuilder
import com.strikeprotocols.mobile.data.Signable

data class WalletApproval(
    val approvalTimeoutInSeconds: Int?,
    val id: String?,
    val numberOfApprovalsReceived: Int?,
    val numberOfDeniesReceived: Int?,
    val numberOfDispositionsRequired: Int?,
    val submitDate: String?,
    val submitterEmail: String?,
    val submitterName: String?,
    val walletType: String?,
    @Transient val details: SolanaApprovalRequestDetails?
) : Signable {

    override fun retrieveSignableData(approverPublicKey: String?): String {
        //TODO Implement the signable data here
        return "TODO"
    }

    companion object {
        fun toJson(approval: WalletApproval): String {
            val customGson = GsonBuilder()
                .registerTypeAdapter(WalletApproval::class.java, WalletApprovalDeserializer())
                .create()
            return customGson.toJson(approval)
        }

        fun fromJson(json: String): WalletApproval {
            val customGson = GsonBuilder()
                .registerTypeAdapter(WalletApproval::class.java, WalletApprovalDeserializer())
                .create()
            return customGson.fromJson(json, WalletApproval::class.java)
        }
    }
}
