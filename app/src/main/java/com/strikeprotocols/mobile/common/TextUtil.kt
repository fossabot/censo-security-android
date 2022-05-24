package com.strikeprotocols.mobile.common

import android.content.Context
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.EncryptionManagerException
import com.strikeprotocols.mobile.data.models.approval.AccountType
import com.strikeprotocols.mobile.data.models.approval.SlotUpdateType
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError

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

fun String.convertPublicKeyToDisplayText(): String {
    //Only want to keep the last 8 characters
    val startIndex = this.length - 9
    val subStringedPublicKey = this.substring(startIndex = startIndex)

    return StringBuilder().append("••••••••••••").append(subStringedPublicKey).toString()
}


fun retrieveApprovalDispositionDialogErrorText(
    approvalDispositionError: ApprovalDispositionError,
    context: Context
) = when (approvalDispositionError) {
    ApprovalDispositionError.SUBMIT_FAILURE -> context.getString(R.string.approval_disposition_error_submit)
    ApprovalDispositionError.DURABLE_NONCE_FAILURE -> context.getString(R.string.approval_disposition_error_nonces)
    ApprovalDispositionError.SIGNING_DATA_FAILURE -> context.getString(R.string.approval_disposition_error_signing_data)
    ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE -> context.getString(R.string.approval_disposition_error)
    else -> {
        context.getString(R.string.approval_disposition_error_general)
    }
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