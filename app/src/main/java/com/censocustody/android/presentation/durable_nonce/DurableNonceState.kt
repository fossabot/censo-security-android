package com.censocustody.android.presentation.durable_nonce

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.MultipleAccountsResponse

data class DurableNonceState(
    val multipleAccounts: DurableNonceViewModel.MultipleAccounts? = null,
    val multipleAccountsResult: Resource<MultipleAccountsResponse> = Resource.Uninitialized,
    val minimumNonceAccountAddressesSlot: Int = 0,
    val nonceAccountAddresses: List<String> = emptyList()
) {
    val isLoading = multipleAccountsResult is Resource.Loading
}