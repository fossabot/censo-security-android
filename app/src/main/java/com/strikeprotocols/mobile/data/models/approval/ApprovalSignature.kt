package com.strikeprotocols.mobile.data.models.approval

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
    ) : ApprovalSignature()
}
