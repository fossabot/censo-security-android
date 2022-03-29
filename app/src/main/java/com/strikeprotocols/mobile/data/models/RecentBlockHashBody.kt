package com.strikeprotocols.mobile.data.models

import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.data.models.RecentBlockHashBody.Companion.RECENT_BLOCKHASH_METHOD
import com.strikeprotocols.mobile.data.models.RecentBlockHashBody.Companion.RPC_VERSION
import java.util.*

data class RecentBlockHashBody(
    val id: String = UUID.randomUUID().toString(),
    val method: String = RECENT_BLOCKHASH_METHOD,
    val jsonrpc: String = RPC_VERSION,
    val params: List<RPCParam> = listOf(RPCParam())
) {
    object Companion {
        const val RECENT_BLOCKHASH_METHOD = "getRecentBlockhash"
        const val RPC_VERSION = "2.0"
    }
}

data class RPCParam(val commitment: String = BuildConfig.SOLANA_COMMITMENT)