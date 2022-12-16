package com.censocustody.android.data.models.approval

import com.censocustody.android.data.SignedPayload

sealed class ApprovalSignature {
    data class SolanaSignature(
        val signature: String,
        val nonce: String,
        val nonceAccountAddress: String,
    ) : ApprovalSignature()

    data class BitcoinSignatures(
        val signatures: List<String>
    ) : ApprovalSignature()

    data class NoChainSignature(
        val signature: String,
        val signedData: String
    ) : ApprovalSignature() {
        constructor(signedPayload: SignedPayload): this(signedPayload.signature, signedPayload.payload)
    }
}
