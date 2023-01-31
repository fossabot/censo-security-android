package com.censocustody.android.data.models.approvalV2

import com.censocustody.android.data.SignedPayload

sealed class ApprovalSignatureV2 {

    data class BitcoinSignatures(
        val signatures: List<String>
    ) : ApprovalSignatureV2()

    data class EthereumSignature(
        val signature: String,
        val offchainSignature: NoChainSignature?,
    ) : ApprovalSignatureV2()

    data class PolygonSignature(
        val signature: String,
        val offchainSignature: NoChainSignature?,
    ) : ApprovalSignatureV2()

    data class NoChainSignature(
        val signature: String,
        val signedData: String
    ) : ApprovalSignatureV2() {
        constructor(signedPayload: SignedPayload): this(signedPayload.signature, signedPayload.payload)
    }
}