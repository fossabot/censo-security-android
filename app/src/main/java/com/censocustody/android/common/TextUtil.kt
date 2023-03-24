package com.censocustody.android.common

import android.content.Context
import com.censocustody.android.R


const val HELP_URL = "https://help.censo.co"

fun String.toWalletName(context: Context): String {
    if (this.lowercase().endsWith("wallet")) {
        return this
    }
    return "$this ${context.getString(R.string.wallet)}"
}

fun String.toVaultName(context: Context): String {
    if (this.lowercase().endsWith("vault")) {
        return this
    }

    return "$this ${context.getString(R.string.vault)}"
}

fun String.maskAddress(): String {
    if (this.length <= 8) {
        return ""
    }

    //Grab first 4 and last 4 characters
    val start = this.substring(startIndex = 0, endIndex = 4)
    val end = this.substring(startIndex = this.length - 4)

    return StringBuilder().append(start).append("•••").append(end).toString()
}