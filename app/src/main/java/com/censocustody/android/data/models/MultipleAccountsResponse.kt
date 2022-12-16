package com.censocustody.android.data.models

data class MultipleAccountsResponse(
    val nonces: List<Nonce>,
    val slot: Int
)

data class Nonce(val value: String)
