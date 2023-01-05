package com.censocustody.android.data.models

import com.censocustody.android.common.BaseWrapper

//Going to append bytes of the keys together, and create one signature
//order of the wallet signers in signers must match how we append bytes of keys together
data class Signers(
    val signers: List<WalletSigner>,
    val signature: String
) {
    companion object {
        fun retrieveDataToSign(walletSigners: List<WalletSigner>) =
            walletSigners
                .filter { it.publicKey != null }
                .map { BaseWrapper.decode(it.publicKey!!) }
                .reduce { array, next -> array + next }
    }
}