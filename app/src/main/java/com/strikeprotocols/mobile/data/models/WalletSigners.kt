package com.strikeprotocols.mobile.data.models

data class WalletSigner(
    val publicKey: String?,
    val walletType: String?
) {
    companion object {
        const val WALLET_TYPE_SOLANA = "Solana"
        const val WALLET_TYPE_BITCOIN = "Bitcoin"
    }
}
