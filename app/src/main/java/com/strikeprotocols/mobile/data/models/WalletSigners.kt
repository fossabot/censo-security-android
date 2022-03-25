package com.strikeprotocols.mobile.data.models

data class WalletSigner(
    val encryptedKey: String?,
    val publicKey: String?,
    val walletType: String?
) {
    companion object {
        const val WALLET_TYPE_SOLANA = "Solana"
    }
}
