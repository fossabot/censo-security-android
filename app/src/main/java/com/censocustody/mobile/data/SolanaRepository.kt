package com.censocustody.mobile.data

import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.models.MultipleAccountsResponse
import com.censocustody.mobile.presentation.durable_nonce.DurableNonceViewModel
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