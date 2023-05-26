package com.censocustody.android.data.models

data class WalletConnectPairingRequest(val uri: String, val walletAddresses: List<String>? = null)

data class WalletConnectPairingResponse(val topic: String)