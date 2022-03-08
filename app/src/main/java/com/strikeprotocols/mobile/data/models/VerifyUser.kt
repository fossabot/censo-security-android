package com.strikeprotocols.mobile.data.models

data class VerifyUser(
    val fullName: String?,
    val hasApprovalPermission: Boolean?,
    val id: String?,
    val loginName: String?,
    val organization: Organization?,
    val publicKeys: List<PublicKey?>?,
    val useStaticKey: Boolean?
)

data class Organization(
    val id: String?,
    val name: String?
)

data class PublicKey(
    val key: String?,
    val walletType: String?
)