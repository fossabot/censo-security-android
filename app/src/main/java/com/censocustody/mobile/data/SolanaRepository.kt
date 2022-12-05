package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.MultipleAccountsResponse
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import javax.inject.Inject

interface SolanaRepository {
    suspend fun getMultipleAccounts(multipleAccountsBody: DurableNonceViewModel.MultipleAccountsBody): Resource<MultipleAccountsResponse>
}

class SolanaRepositoryImpl @Inject constructor(
    private val api: SolanaApiService,
) : SolanaRepository, BaseRepository() {

    override suspend fun getMultipleAccounts(multipleAccountsBody: DurableNonceViewModel.MultipleAccountsBody): Resource<MultipleAccountsResponse> =
        retrieveApiResource { api.multipleAccounts(multipleAccountsBody) }

}