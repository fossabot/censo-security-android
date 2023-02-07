package com.censocustody.android.data.models.approvalV2

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory

class TypeFactorySettings {

    companion object {
        val approvalSignatureAdapterFactory: RuntimeTypeAdapterFactory<ApprovalSignature> = RuntimeTypeAdapterFactory.of(
            ApprovalSignature::class.java, "type"
        ).registerSubtype(
            ApprovalSignature.BitcoinSignatures::class.java, "bitcoin"
        ).registerSubtype(
            ApprovalSignature.EthereumSignature::class.java, "ethereum"
        ).registerSubtype(ApprovalSignature.OffChainSignature::class.java, "offchain")
    }
}