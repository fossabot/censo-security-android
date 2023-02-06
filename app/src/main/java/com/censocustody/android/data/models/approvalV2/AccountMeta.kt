package com.censocustody.android.data.models.approvalV2

data class AccountMeta(
    val publicKey: PublicKey,
    val isSigner: Boolean,
    val isWritable: Boolean
)
