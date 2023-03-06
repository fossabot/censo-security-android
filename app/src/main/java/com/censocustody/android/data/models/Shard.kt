package com.censocustody.android.data.models

data class Shard(
    val adminPublicKey: String,
    val encryptedData: String
)

data class Share(
    val shareId: String,
    val policyRevisionId: String,
    val shards: List<Shard>
)