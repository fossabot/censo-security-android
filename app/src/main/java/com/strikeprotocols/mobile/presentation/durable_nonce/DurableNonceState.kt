package com.strikeprotocols.mobile.presentation.durable_nonce

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.MultipleAccountsResponse

data class DurableNonceState(
    val triggerBioPrompt: Boolean = false,
    val userBiometricVerified: DurableNonceViewModel.BiometricVerified? = null,
    val multipleAccounts: DurableNonceViewModel.MultipleAccounts? = null,
    val multipleAccountsResult: Resource<MultipleAccountsResponse> = Resource.Uninitialized,
    val nonceAccountAddresses: List<String> = emptyList()
) {
    val isLoading = multipleAccountsResult is Resource.Loading
}