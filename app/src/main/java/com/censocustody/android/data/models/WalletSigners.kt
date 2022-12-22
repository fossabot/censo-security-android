package com.censocustody.android.data.models

import com.censocustody.android.data.models.StoredKeyData.Companion.BITCOIN_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.CENSO_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.ETHEREUM_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.SOLANA_KEY

data class WalletSigner(
    val publicKey: String?,
    val chain: Chain?,
    val signature: String? = null
)

fun HashMap<String, String>.mapToPublicKeysList() =
    map {
        val walletType = when (it.key) {
            BITCOIN_KEY -> Chain.bitcoin
            SOLANA_KEY -> Chain.solana
            ETHEREUM_KEY -> Chain.ethereum
            CENSO_KEY -> Chain.censo
            else -> throw Exception("Missing chain locally")
        }
        WalletSigner(
            publicKey = it.value,
            chain = walletType
        )
    }
