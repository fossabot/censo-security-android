package com.strikeprotocols.mobile.common

import android.content.Context
import com.strikeprotocols.mobile.R
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
        ApprovalDispositionError.BLOCKHASH_FAILURE -> context.getString(R.string.approval_disposition_error_blockhash)
        ApprovalDispositionError.SIGNING_DATA_FAILURE -> context.getString(R.string.approval_disposition_error_signing_data)
        ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE -> context.getString(R.string.approval_disposition_error)
        else -> {
            context.getString(R.string.approval_disposition_error_general)
        }
    }