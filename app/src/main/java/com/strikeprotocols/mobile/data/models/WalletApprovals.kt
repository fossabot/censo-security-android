package com.strikeprotocols.mobile.data.models

import com.google.gson.Gson
import com.strikeprotocols.mobile.data.Signable

data class WalletApproval(
    val approvalTimeoutInSeconds: Int?,
    val details: Details?,
    val id: String?,
    val numberOfApprovalsReceived: Int?,
    val numberOfDeniesReceived: Int?,
    val numberOfDispositionsRequired: Int?,
    val submitDate: String?,
    val submitterEmail: String?,
    val submitterName: String?,
    val walletType: String?
) : Signable {
    companion object {
        fun toJson(approval: WalletApproval) : String {
            return Gson().toJson(approval)
        }

        fun fromJson(json: String) : WalletApproval {
            return Gson().fromJson(json, WalletApproval::class.java)
        }
    }

    override fun retrieveSignableData(approverPublicKey: String?): String {
        //TODO Implement the signable data here
        return "TODO"
    }
}

data class SigningData(
    val feePayer: String?,
    val multisigOpAccountAddress: String?,
    val walletAddress: String?,
    val walletProgramId: String?
)

data class Details(
    val signingData: SigningData?,
    val type: String?
)