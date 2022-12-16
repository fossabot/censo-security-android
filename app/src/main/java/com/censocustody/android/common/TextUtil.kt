package com.censocustody.android.common

import android.content.Context
import com.censocustody.android.R
import com.censocustody.android.data.EncryptionManagerException

fun Int.convertApprovalsNeededToDisplayMessage(context: Context): String {
    return when (this) {
        0 -> {
            context.getString(R.string.no_more_approvals_needed)
        }
        1 -> {
            "$this ${context.getString(R.string.more_approval_needed)}"
        }
        else -> {
            "$this ${context.getString(R.string.more_approvals_needed)}"
        }
    }
}

//TODO: String resource these loose strings
fun String.toWalletName(): String {
    if (this.lowercase().endsWith("wallet")) {
        return this
    }
    return "$this Wallet"
}

fun String.toVaultName(context: Context): String {
    if (this.lowercase().endsWith("vault")) {
        return this
    }

    return "$this ${context.getString(R.string.vault)}"
}

fun String.convertPublicKeyToDisplayText(): String {
    if (this.length <= 9) {
        return ""
    }
    //Only want to keep the last 8 characters
    val startIndex = this.length - 9
    val subStringedPublicKey = this.substring(startIndex = startIndex)

    return StringBuilder().append("••••••••••••").append(subStringedPublicKey).toString()
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

fun getAuthFlowErrorMessage(e: Exception, context: Context): String =
    if (e is EncryptionManagerException) {
        when (e) {
            is EncryptionManagerException.KeyPairGenerationFailedException -> {
                context.getString(R.string.key_pair_generation_failed)
            }
            is EncryptionManagerException.DecryptionFailedException -> {
                context.getString(R.string.decryption_failed)
            }
            is EncryptionManagerException.EncryptionFailedException -> {
                context.getString(R.string.encryption_failed)
            }
            is EncryptionManagerException.PublicKeyRegenerationFailedException -> {
                context.getString(R.string.key_regeneration_failed)
            }
            is EncryptionManagerException.SignDataException -> {
                context.getString(R.string.sign_data_failed)
            }
            is EncryptionManagerException.VerifyFailedException -> {
                context.getString(R.string.verify_key_failed)
            }
        }
    } else {
        context.getString(R.string.auth_flow_exception)
    }