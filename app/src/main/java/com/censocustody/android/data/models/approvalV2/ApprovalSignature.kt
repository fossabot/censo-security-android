package com.censocustody.android.data.models.approvalV2

import com.censocustody.android.data.SignedPayload
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory

sealed class ApprovalSignature {

    companion object {
        val approvalSignatureAdapterFactory: RuntimeTypeAdapterFactory<ApprovalSignature> =
            RuntimeTypeAdapterFactory.of(
                ApprovalSignature::class.java, "type"
            ).registerSubtype(
                BitcoinSignatures::class.java, "bitcoin"
            ).registerSubtype(
                EthereumSignature::class.java, "ethereum"
            ).registerSubtype(
                PolygonSignature::class.java, "polygon"
            ).registerSubtype(OffChainSignature::class.java, "offchain")
    }

    data class BitcoinSignatures(
        val signatures: List<String>,
        val offchainSignature: ApprovalSignature
    ) : ApprovalSignature()

    data class EthereumSignature(
        val signature: String,
        val offchainSignature: ApprovalSignature
    ) : ApprovalSignature()

    data class PolygonSignature(
        val signature: String,
        val offchainSignature: ApprovalSignature
    ) : ApprovalSignature()

    data class OffChainSignature(
        val signature: String,
        val signedData: String
    ) : ApprovalSignature() {
        constructor(signedPayload: SignedPayload): this(signedPayload.signature, signedPayload.payload)
    }
}
