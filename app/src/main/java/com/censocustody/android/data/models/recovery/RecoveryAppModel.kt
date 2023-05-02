package com.censocustody.android.data.models.recovery

import com.censocustody.android.data.models.Chain

data class SignableRecoveryItem(
    val chain: Chain,
    val dataToSign: String
)

data class RecoveryAppSigningRequest(
    val items: List<SignableRecoveryItem>
)

data class RecoverySignatureItem(
    val chain: Chain,
    val signature: String
)

data class RecoveryAppSigningResponse(
    val recoveryAddress: String,
    val items: List<RecoverySignatureItem>
)