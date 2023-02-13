package com.censocustody.android.data.models

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale

enum class Chain {
    bitcoin,
    ethereum,
    polygon,
    offchain;

    fun label(): String {
        return this.name.capitalize(Locale.current)
    }

    companion object {
        fun chainsWithSigningKeys(): List<Chain> = listOf(bitcoin, ethereum, offchain)
    }
}