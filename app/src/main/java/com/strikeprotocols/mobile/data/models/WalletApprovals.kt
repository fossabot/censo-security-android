package com.strikeprotocols.mobile.data.models

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
)

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