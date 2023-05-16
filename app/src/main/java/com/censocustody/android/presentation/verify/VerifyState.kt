package com.censocustody.android.presentation.verify

import com.censocustody.android.common.Resource

data class VerifyState(
    val signedData: ByteArray = byteArrayOf(),
    val verified: Resource<Boolean> = Resource.Uninitialized
)