package com.censocustody.android.data.models.approval

import com.censocustody.android.data.SignedPayload
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory

sealed class ApprovalSignature {

    companion object {
        val approvalSignatureAdapterFactory: RuntimeTypeAdapterFactory<ApprovalSignature> =
            RuntimeTypeAdapterFactory.of(
                ApprovalSignature::class.java, "type"
            ).registerSubtype(
                SolanaSignature::class.java, "solana"
            ).registerSubtype(
                BitcoinSignatures::class.java, "bitcoin"
            ).registerSubtype(
                EthereumSignature::class.java, "ethereum"
            ).registerSubtype(OffChainSignature::class.java, "offchain")
    }

    data class SolanaSignature(
        val signature: String,
        val nonce: String,
        val nonceAccountAddress: String,
    ) : ApprovalSignature()

    data class BitcoinSignatures(
        val signatures: List<String>
    ) : ApprovalSignature()

    data class EthereumSignature(
        val signature: String,
        val offchainSignature: ApprovalSignature?,
    ) : ApprovalSignature()

    data class PolygonSignature(
        val signature: String,
        val offchainSignature: ApprovalSignature?,
    ) : ApprovalSignature()

    data class OffChainSignature(
        val signature: String,
        val signedData: String
    ) : ApprovalSignature() {
        constructor(signedPayload: SignedPayload): this(signedPayload.signature, signedPayload.payload)
    }
}
