package com.strikeprotocols.mobile.data.models

data class MultipleAccountsResponse(
    val nonces: List<Nonce>,
    val slot: Int
)

data class Nonce(val value: String)
