package com.strikeprotocols.mobile.data.models

import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.BITCOIN_KEY
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.ETHEREUM_KEY
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.SOLANA_KEY

data class Signers(
    val signers: List<WalletSigner?>?
)

data class WalletSigner(
    val publicKey: String?,
    val chain: Chain?,
    val signature: String? = null
) {
    companion object {
        const val WALLET_TYPE_SOLANA = "Solana"
        const val WALLET_TYPE_BITCOIN = "Bitcoin"
        const val WALLET_TYPE_ETHEREUM = "Ethereum"
    }
}


fun HashMap<String, String>.mapToPublicKeysList() =
    map {
        val walletType = when (it.key) {
            BITCOIN_KEY -> Chain.bitcoin
            SOLANA_KEY -> Chain.solana
            ETHEREUM_KEY -> Chain.ethereum
            else -> throw Exception("Missing chain locally")
        }
        WalletSigner(
            publicKey = it.value,
            chain = walletType
        )
    }
