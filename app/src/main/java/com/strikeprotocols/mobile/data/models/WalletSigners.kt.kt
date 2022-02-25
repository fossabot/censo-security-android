package com.strikeprotocols.mobile.data.models

data class WalletSigner(
    val encryptedKey: String?,
    val publicKey: String?,
    val walletType: String?
)

data class WalletSigners(
    val items: List<WalletSigner?>?
)