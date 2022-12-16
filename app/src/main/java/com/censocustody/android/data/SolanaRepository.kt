package com.censocustody.android.data

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.MultipleAccountsResponse
import com.censocustody.android.presentation.durable_nonce.DurableNonceViewModel
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