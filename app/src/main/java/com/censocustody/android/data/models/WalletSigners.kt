package com.censocustody.android.data.models

import com.censocustody.android.data.models.StoredKeyData.Companion.BITCOIN_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.CENSO_KEY
import com.censocustody.android.data.models.StoredKeyData.Companion.ETHEREUM_KEY

data class WalletSigner(
    val publicKey: String?,
    val chain: Chain?,
    val signature: String? = null
)

fun HashMap<String, String>.mapToPublicKeysList(): List<WalletSigner> {
    if (this.isEmpty()) return emptyList()

    return map {
        val walletType = when (it.key) {
            BITCOIN_KEY -> Chain.bitcoin
            ETHEREUM_KEY -> Chain.ethereum
            CENSO_KEY -> Chain.censo
            else -> throw Exception("Missing chain locally")
        }
        WalletSigner(
            publicKey = it.value,
            chain = walletType
        )
    }
}
