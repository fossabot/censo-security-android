package com.strikeprotocols.mobile.data

class WalletSignersException : Exception(NO_WALLET_SIGNERS) {
    companion object {
        val NO_WALLET_SIGNERS = Exception("No wallet Signers")
    }
}