package com.strikeprotocols.mobile.data.models

data class RecentBlockHashResponse(
    val id: String,
    val result: Result
)

data class Result(
    val value: Value
)

data class Value(
    val blockhash: String,
)