package com.censocustody.android.data.models.approvalV2

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory

class TypeFactorySettings {

    companion object {
        val signingDataAdapterFactory: RuntimeTypeAdapterFactory<ApprovalRequestDetailsV2.SigningData> = RuntimeTypeAdapterFactory.of(
            ApprovalRequestDetailsV2.SigningData::class.java, "type"
        ).registerSubtype(
            ApprovalRequestDetailsV2.SigningData.BitcoinSigningData::class.java, "bitcoin"
        ).registerSubtype(ApprovalRequestDetailsV2.SigningData.EthereumSigningData::class.java, "ethereum")

        val approvalSignatureAdapterFactory: RuntimeTypeAdapterFactory<ApprovalSignature> = RuntimeTypeAdapterFactory.of(
            ApprovalSignature::class.java, "type"
        ).registerSubtype(
            ApprovalSignature.SolanaSignature::class.java, "solana"
        ).registerSubtype(
            ApprovalSignature.BitcoinSignatures::class.java, "bitcoin"
        ).registerSubtype(
            ApprovalSignature.EthereumSignature::class.java, "ethereum"
        ).registerSubtype(ApprovalSignature.OffChainSignature::class.java, "offchain")
    }
}