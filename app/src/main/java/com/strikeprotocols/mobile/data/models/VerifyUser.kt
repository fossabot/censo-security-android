package com.strikeprotocols.mobile.data.models

data class VerifyUser(
    val fullName: String?,
    val hasApprovalPermission: Boolean?,
    val id: String?,
    val loginName: String?,
    val organization: Organization?,
    val publicKeys: List<WalletPublicKey?>?,
    val useStaticKey: Boolean?
) {
    val firstPublicKey: String? = publicKeys?.firstOrNull { !it?.key.isNullOrEmpty() }?.key
}

data class Organization(
    val id: String?,
    val name: String?
)

data class WalletPublicKey(
    val key: String?,
    val walletType: String?
)