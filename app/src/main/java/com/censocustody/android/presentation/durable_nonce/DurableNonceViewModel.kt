package com.censocustody.android.presentation.durable_nonce

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.BuildConfig
import com.censocustody.android.common.Resource
import com.censocustody.android.common.CensoError
import com.censocustody.android.data.BaseRepository.Companion.NO_CODE
import com.censocustody.android.data.BaseRepository.Companion.TOO_MANY_REQUESTS_CODE
import com.censocustody.android.data.SolanaRepository
import com.censocustody.android.data.models.Nonce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class DurableNonceViewModel @Inject constructor(
    private val solanaRepository: SolanaRepository
) : ViewModel() {

    private var nonceRetryCount: Int

    var state by mutableStateOf(DurableNonceState())
        private set

    init {
        nonceRetryCount = 0
    }

    fun setInitialData(nonceAccountAddresses: List<String>, minimumNonceAccountAddressesSlot: Int) {
        state = state.copy(
            nonceAccountAddresses = nonceAccountAddresses,
            minimumNonceAccountAddressesSlot = minimumNonceAccountAddressesSlot
        )
        retrieveMultipleAccounts()
    }

    fun resetState() {
        state = DurableNonceState()
    }

    fun resetMultipleAccountsResource() {
        state = state.copy(multipleAccountsResult = Resource.Uninitialized)
    }

    fun retrieveMultipleAccounts() {
        viewModelScope.launch {
            state = state.copy(multipleAccountsResult = Resource.Loading())

            val multipleAccountsBody =
                MultipleAccountsBody(
                    params = RPCParam(keys = state.nonceAccountAddresses)
                )
            val multipleAccountsResult =
                solanaRepository.getMultipleAccounts(multipleAccountsBody)

            if (multipleAccountsResult is Resource.Success) {

                val multipleAccountsResponse = multipleAccountsResult.data

                if (state.minimumNonceAccountAddressesSlot > (multipleAccountsResponse?.slot ?: 0)
                    && nonceRetryCount < NONCE_RETRIES
                ) {
                    nonceRetryCount++
                    retrieveMultipleAccounts()
                    return@launch
                } else if (nonceRetryCount >= NONCE_RETRIES) {
                    nonceRetryCount = 0
                    state = state.copy(
                        multipleAccountsResult = Resource.Error(
                            censoError = CensoError.DefaultApiError(statusCode = NO_CODE),
                            exception = Exception(
                                UNABLE_TO_RETRIEVE_VALID_NONCE
                            )
                        )
                    )
                    return@launch
                }

                nonceRetryCount = 0
                val multipleAccounts =
                    MultipleAccounts(nonces = multipleAccountsResponse?.nonces ?: emptyList())
                state = state.copy(
                    multipleAccountsResult = Resource.Success(multipleAccountsResponse),
                    multipleAccounts = multipleAccounts
                )
            } else if (multipleAccountsResult is Resource.Error) {
                if (multipleAccountsResult.censoError is CensoError.ApiError) {
                    if (multipleAccountsResult.censoError.statusCode == TOO_MANY_REQUESTS_CODE) {
                        delay(TOO_MANY_REQUESTS_DELAY)
                        nonceRetryCount++
                        retrieveMultipleAccounts()
                    } else {
                        state = state.copy(
                            multipleAccountsResult = multipleAccountsResult
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val MULTIPLE_ACCOUNTS_METHOD = "getMultipleAccounts"
        const val RPC_VERSION = "2.0"
        const val BASE_64 = "base64"
        const val UNABLE_TO_RETRIEVE_VALID_NONCE = "Unable to retrieve valid nonce data"

        const val NONCE_RETRIES = 20
        const val TOO_MANY_REQUESTS_DELAY: Long = 10_000L
    }

    inner class MultipleAccounts(
        val nonces: List<Nonce>
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