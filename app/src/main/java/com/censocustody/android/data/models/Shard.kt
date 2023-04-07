package com.censocustody.android.data.models

import com.censocustody.android.common.toHexString
import java.math.BigInteger

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

data class ShardEntry(
    val id: String?,
    val participantId: BigInteger,
    val parentId: String?,
    val shard: BigInteger?
) {
    override fun toString(): String {
        return "ShardEntry(id=$id, participantId=${participantId.toHexString()}, parentId=$parentId, shard=${shard!!.toHexString()})"
    }
}

data class GetShardsResponse(
    val shards: List<Shard>
)

data class GetRecoveryShardsResponse(
    val shards: List<Shard>,
    val ancestors: List<AncestorShard>
)