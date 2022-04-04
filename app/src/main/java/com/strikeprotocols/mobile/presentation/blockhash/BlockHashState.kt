package com.strikeprotocols.mobile.presentation.blockhash

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.RecentBlockHashResponse
import com.strikeprotocols.mobile.presentation.blockhash.BlockHashViewModel.BlockHash
import com.strikeprotocols.mobile.presentation.blockhash.BlockHashViewModel.BiometricVerified

data class BlockHashState(
    val triggerBioPrompt: Boolean = false,
    val userBiometricVerified: BiometricVerified? = null,
    val blockHash: BlockHash? = null,
    val recentBlockhashResult: Resource<RecentBlockHashResponse> = Resource.Uninitialized,
) {
    val isLoading = recentBlockhashResult is Resource.Loading
}