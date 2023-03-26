package com.censocustody.android.data.models

data class ShardCopy(
    val encryptionPublicKey: String,
    val encryptedData: String,
)

data class Shard(
    val participantId: String,
    val shardCopies: List<ShardCopy>,
    val shardId: String? = null,
    val parentShardId: String? = null,
)

data class RecoveryShard(
    val shardId: String,
    val encryptedData: String,
)

data class Share(
    val policyRevisionId: String,
    val shards: List<Shard>
)

data class AncestorShard(
    val shardId: String,
    val participantId: String,
    val parentShardId: String? = null
)

data class GetShardsResponse(
    val shards: List<Shard>
)

data class GetRecoveryShardsResponse(
    val shards: List<Shard>,
    val ancestors: List<AncestorShard>
)