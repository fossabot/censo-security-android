package com.censocustody.android.data.models

import com.censocustody.android.common.wrapper.BaseWrapper
import com.censocustody.android.data.models.recovery.RecoveryAppSigningResponse

data class OrgAdminRecoverySignaturesRequest(
    val recoveryAddress: String,
    val signatures: List<Signature>,
) {

    companion object {
        fun fromRecoveryAppSigningResponse(myOrgAdminRecoveryRequest: OrgAdminRecoveryRequest, recoveryAppSigningResponse: RecoveryAppSigningResponse): OrgAdminRecoverySignaturesRequest{
            return OrgAdminRecoverySignaturesRequest(
                recoveryAppSigningResponse.recoveryAddress,
                recoveryAppSigningResponse.items.mapNotNull {
                    when (it.chain) {
                        Chain.ethereum -> Signature.Ethereum(it.signature)
                        Chain.polygon -> Signature.Polygon(it.signature)
                        Chain.offchain -> Signature.OffChain(
                            it.signature,
                            BaseWrapper.encodeToBase64(myOrgAdminRecoveryRequest.getOffchainData())
                        )
                        else -> null
                    }
                }
            )
        }
    }
    sealed class Signature {
        abstract val type: String

        data class Ethereum(
            val signature: String,
            override val type: String = "ethereum"
        ) : Signature()

        data class Polygon(
            val signature: String,
            override val type: String = "polygon"
        ) : Signature()

        data class OffChain(
            val signature: String,
            val signedData: String,
            override val type: String = "offchain"
        ) : Signature()
    }


}
