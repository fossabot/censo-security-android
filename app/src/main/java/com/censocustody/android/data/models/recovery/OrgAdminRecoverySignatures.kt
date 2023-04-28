package com.censocustody.android.data.models

data class OrgAdminRecoverySignaturesRequest(
    val recoveryAddress: String,
    val signatures: List<Signature>,
) {
    sealed class Signature {
        abstract val type: String

        data class EthereumSignature(
            val signature: String,
            override val type: String = "ethereum"
        ) : Signature()

        data class PolygonSignature(
            val signature: String,
            override val type: String = "polygon"
        ) : Signature()

        data class OffChainSignature(
            val signature: String,
            val signedData: String,
            override val type: String = "offchain"
        ) : Signature()
    }
}
