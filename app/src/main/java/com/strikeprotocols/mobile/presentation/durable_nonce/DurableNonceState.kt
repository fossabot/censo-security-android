package com.strikeprotocols.mobile.presentation.durable_nonce

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.MultipleAccountsResponse

data class DurableNonceState(
    val multipleAccounts: DurableNonceViewModel.MultipleAccounts? = null,
    val multipleAccountsResult: Resource<MultipleAccountsResponse> = Resource.Uninitialized,
    val minimumNonceAccountAddressesSlot: Int = 0,
    val nonceAccountAddresses: List<String> = emptyList()
) {
    val isLoading = multipleAccountsResult is Resource.Loading
}