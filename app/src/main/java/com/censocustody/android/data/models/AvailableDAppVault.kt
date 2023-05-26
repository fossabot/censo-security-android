package com.censocustody.android.data.models

data class AvailableDAppVault(
    val vaultName: String,
    val wallets: List<AvailableDAppWallet>
)

data class AvailableDAppWallet(
    val walletName: String,
    val walletAddress: String,
    val chains: List<Chain>
)

fun List<Chain>.toSingleText() =
    this.joinToString(separator = ", ") { it.label() }

data class AvailableDAppVaults(
    val vaults: List<AvailableDAppVault>
)