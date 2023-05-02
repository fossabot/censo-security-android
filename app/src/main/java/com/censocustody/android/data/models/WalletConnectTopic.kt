package com.censocustody.android.data.models

data class WalletConnectTopic(
    val topic: String,
    val name: String,
    val url: String,
    val description: String,
    val icons: List<String>,
    val status: String
) {
    companion object {
        const val REJECTED = "Rejected"
        const val ACTIVE = "Active"
    }
}

