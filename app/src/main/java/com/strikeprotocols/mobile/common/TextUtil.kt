package com.strikeprotocols.mobile.common

import android.content.Context
import com.strikeprotocols.mobile.R

fun Int.convertApprovalsNeededToDisplayMessage(context: Context) : String {
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