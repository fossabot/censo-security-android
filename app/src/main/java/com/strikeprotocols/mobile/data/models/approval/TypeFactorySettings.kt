package com.strikeprotocols.mobile.data.models.approval

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory

class TypeFactorySettings {

    companion object {
        val signingDataAdapterFactory: RuntimeTypeAdapterFactory<SigningData> = RuntimeTypeAdapterFactory.of(
            SigningData::class.java, "type"
        ).registerSubtype(SigningData.SolanaSigningData::class.java, "solana"
        ).registerSubtype(SigningData.BitcoinSigningData::class.java, "bitcoin"
        ).registerSubtype(SigningData.EthereumSigningData::class.java, "ethereum")

        val approvalSignatureAdapterFactory: RuntimeTypeAdapterFactory<ApprovalSignature> = RuntimeTypeAdapterFactory.of(
            ApprovalSignature::class.java, "type"
        ).registerSubtype(ApprovalSignature.SolanaSignature::class.java, "solana"
        ).registerSubtype(ApprovalSignature.BitcoinSignatures::class.java, "bitcoin"
        ).registerSubtype(ApprovalSignature.EthereumSignature::class.java, "ethereum"
        ).registerSubtype(ApprovalSignature.NoChainSignature::class.java, "nochain")
    }
}