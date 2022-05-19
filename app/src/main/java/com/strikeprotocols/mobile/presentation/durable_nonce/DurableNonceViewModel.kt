package com.strikeprotocols.mobile.presentation.durable_nonce

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.SolanaApiService
import com.strikeprotocols.mobile.data.models.Nonce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class DurableNonceViewModel @Inject constructor(
    private val solanaApiService: SolanaApiService
) : ViewModel() {

    var state by mutableStateOf(DurableNonceState())
        private set

    fun setNonceAccountAddresses(nonceAccountAddresses: List<String>) {
        state = state.copy(nonceAccountAddresses = nonceAccountAddresses)
    }

    fun resetState() {
        state = DurableNonceState()
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
            retrieveMultipleAccounts()
        }
    }

    private fun retrieveMultipleAccounts() {
        viewModelScope.launch {
            state = state.copy(multipleAccountsResult = Resource.Loading())
            state = try {
                val multipleAccountsBody =
                    MultipleAccountsBody(
                        params = RPCParam(keys = state.nonceAccountAddresses)
                    )
                val multipleAccountsResult =
                    solanaApiService.multipleAccounts(multipleAccountsBody)

                val multipleAccounts =
                    MultipleAccounts(nonces = multipleAccountsResult.nonces)
                state.copy(
                    multipleAccountsResult = Resource.Success(multipleAccountsResult),
                    multipleAccounts = multipleAccounts
                )
            } catch (e: Exception) {
                state.copy(multipleAccountsResult = Resource.Success(null))
            }
        }
    }

    companion object {
        const val MULTIPLE_ACCOUNTS_METHOD = "getMultipleAccounts"
        const val RPC_VERSION = "2.0"
        const val BASE_64 = "base64"
    }

    inner class MultipleAccounts(
        val nonces: List<Nonce>
    )

    inner class BiometricVerified(
        val biometryVerified: Boolean
    )

    inner class MultipleAccountsBody(
        val id: String = UUID.randomUUID().toString(),
        val method: String = MULTIPLE_ACCOUNTS_METHOD,
        val jsonrpc: String = RPC_VERSION,
        val params: RPCParam
    )

    data class RPCParam(
        val commitment: String = BuildConfig.SOLANA_COMMITMENT,
        val encoding: String = BASE_64,
        val keys: List<String>
    )
}