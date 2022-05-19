package com.strikeprotocols.mobile.data.models

data class MultipleAccountsResponse(
    val nonces: List<Nonce>
)

data class Nonce(val value: String)
