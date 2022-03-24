package com.strikeprotocols.mobile.data.models

import com.google.gson.Gson

data class WalletApprovals(
    val approvals: List<WalletApproval?>?
)

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
) {
    companion object {
        fun toJson(approval: WalletApproval) : String {
            return Gson().toJson(approval)
        }

        fun fromJson(json: String) : WalletApproval {
            return Gson().fromJson(json, WalletApproval::class.java)
        }
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