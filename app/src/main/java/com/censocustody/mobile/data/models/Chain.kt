package com.censocustody.mobile.data.models

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale

enum class Chain {
    solana,
    bitcoin,
    ethereum;

    fun label(): String {
        return this.name.capitalize(Locale.current)
    }
}