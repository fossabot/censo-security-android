package com.censocustody.android.data.models.approval

data class AccountMeta(
    val publicKey: PublicKey,
    val isSigner: Boolean,
    val isWritable: Boolean
)
