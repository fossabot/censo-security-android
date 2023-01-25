package com.censocustody.android.presentation.key_recovery

import com.censocustody.android.common.Resource

data class KeyRecoveryState(
    val keyRecoveryData: Resource<KeyRecoveryData> = Resource.Uninitialized
)


data class KeyRecoveryData(
    val ephemeralPublicKey: String,
    val encryptedData: String
)