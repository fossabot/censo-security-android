package com.strikeprotocols.mobile.presentation.blockhash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.SolanaApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BlockHashViewModel @Inject constructor(
    private val solanaApiService: SolanaApiService
) : ViewModel() {

    var state by mutableStateOf(BlockHashState())
        private set

    fun resetState() {
        state = BlockHashState()
    }

    fun setPromptTrigger() {
        state = state.copy(triggerBioPrompt = true)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = false)
    }

    fun setUserBiometricVerified(isVerified: Boolean) {
        state = state.copy(
            userBiometricVerified = BiometricVerified(
                biometryVerified = isVerified
            )
        )
        if (state.userBiometricVerified?.biometryVerified == true) {
            retrieveRecentBlockHash()
        }
    }

    private fun retrieveRecentBlockHash() {
        viewModelScope.launch {
            state = state.copy(recentBlockhashResult = Resource.Loading())
            state = try {
                val recentBlockhashResult = solanaApiService.recentBlockhash(RecentBlockHashBody())

                val blockHash =
                    BlockHash(blockHashString = recentBlockhashResult.result.value.blockhash)
                state.copy(
                    recentBlockhashResult = Resource.Success(recentBlockhashResult),
                    blockHash = blockHash
                )
            } catch (e: Exception) {
                state.copy(recentBlockhashResult = Resource.Success(null))
            }
        }
    }

    companion object {
        const val RECENT_BLOCKHASH_METHOD = "getRecentBlockhash"
        const val RPC_VERSION = "2.0"
    }

    inner class BlockHash(
        val blockHashString: String
    )

    inner class BiometricVerified(
        val biometryVerified: Boolean
    )

    inner class RecentBlockHashBody(
        val id: String = UUID.randomUUID().toString(),
        val method: String = RECENT_BLOCKHASH_METHOD,
        val jsonrpc: String = RPC_VERSION,
        val params: List<RPCParam> = listOf(RPCParam())
    )

    data class RPCParam(val commitment: String = BuildConfig.SOLANA_COMMITMENT)
}