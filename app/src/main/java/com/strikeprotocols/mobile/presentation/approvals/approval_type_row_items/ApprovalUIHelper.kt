package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.*
import com.strikeprotocols.mobile.data.models.approval.AccountType.*
import com.strikeprotocols.mobile.ui.theme.GreyText
import com.strikeprotocols.mobile.ui.theme.MoneyRed

fun SolanaApprovalRequestType.getApprovalTypeDialogTitle(context: Context): String {
    return when (this) {
        is SolanaApprovalRequestType.WithdrawalRequest ->
            context.getString(R.string.transfer_dialog_title)
        is SolanaApprovalRequestType.UnknownApprovalType ->
            context.getString(R.string.unknown_dialog_title)
        is SolanaApprovalRequestType.ConversionRequest ->
            context.getString(R.string.conversion_dialog_title)
        is SolanaApprovalRequestType.SignersUpdate ->
            context.getString(R.string.signers_update_dialog_title)
        is SolanaApprovalRequestType.BalanceAccountCreation -> {
            if (accountInfo.accountType == BalanceAccount) {
                context.getString(R.string.balance_account_creation_dialog_title)
            } else {
                context.getString(R.string.stake_account_creation_dialog_title)
            }
        }
        is SolanaApprovalRequestType.DAppTransactionRequest ->
            context.getString(R.string.dapp_transaction_dialog_title)
        is SolanaApprovalRequestType.LoginApprovalRequest ->
            context.getString(R.string.login_approval_dialog_title)
    }
}

fun SolanaApprovalRequestType.getDialogFullMessage(
    context: Context,
    approvalDisposition: ApprovalDisposition,
    initiationRequest: Boolean
): String {
    return "${approvalDisposition.getDialogMessage(context, initiationRequest)} ${getApprovalTypeDialogMessage(context)}"
}

fun SolanaApprovalRequestType.getApprovalRowMetaData(context: Context): ApprovalRowMetaData {
    return when (this) {
        is SolanaApprovalRequestType.WithdrawalRequest -> {
            ApprovalRowMetaData(
                approvalImageVector = Icons.Filled.SyncAlt,
                approvalImageContentDescription = context.getString(R.string.transfer_icon_content_desc),
                approvalTypeTitle = context.getString(R.string.withdrawal_type_title),
            )
        }
        is SolanaApprovalRequestType.UnknownApprovalType -> {
            ApprovalRowMetaData(
                approvalImageVector = Icons.Filled.HelpOutline,
                approvalImageContentDescription = context.getString(R.string.approval_type_unknown_content_des),
                approvalTypeTitle = context.getString(R.string.approval_type_unknown)
            )
        }
        is SolanaApprovalRequestType.ConversionRequest -> {
            ApprovalRowMetaData(
                approvalImageVector = Icons.Filled.Refresh,
                approvalImageContentDescription = context.getString(R.string.conversion_icon_desc),
                approvalTypeTitle = context.getString(R.string.conversion_row_title),
            )
        }
        is SolanaApprovalRequestType.SignersUpdate ->
            ApprovalRowMetaData(
                approvalImageVector = Icons.Filled.PhoneAndroid,
                approvalImageContentDescription = context.getString(R.string.signers_update_icon_content_desc),
                approvalTypeTitle = context.getString(R.string.approval_type_signers_update_title),
            )
        is SolanaApprovalRequestType.BalanceAccountCreation ->
            ApprovalRowMetaData(
                approvalImageVector = Icons.Filled.Lock,
                approvalImageContentDescription = context.getString(R.string.balance_account_creation_icon_content_desc),
                approvalTypeTitle = context.getString(R.string.balance_account_creation_title),
            )
        is SolanaApprovalRequestType.DAppTransactionRequest ->
            ApprovalRowMetaData(
                approvalImageVector = Icons.Filled.Refresh,
                approvalImageContentDescription = context.getString(R.string.approval_type_dapp_content_des),
                approvalTypeTitle = context.getString(R.string.approval_type_dapp_transaction)
            )
        is SolanaApprovalRequestType.LoginApprovalRequest -> {
            ApprovalRowMetaData(
                approvalImageVector = Icons.Filled.Login,
                approvalImageContentDescription = context.getString(R.string.login_icon_content_desc),
                approvalTypeTitle = context.getString(R.string.login_approval_title),
            )
        }
    }
}

fun ApprovalDisposition.getDialogMessage(context: Context, initiationRequest: Boolean): String {
    return if (this == ApprovalDisposition.APPROVE) {
        context.getString(R.string.you_are_about_to_approve)
    } else {
        if(initiationRequest) {
            context.getString(R.string.you_are_about_to_cancel)
        } else {
            context.getString(R.string.you_are_about_to_deny)
        }
    }
}

private fun SolanaApprovalRequestType.getApprovalTypeDialogMessage(context: Context): String {
    return when (this) {
        is SolanaApprovalRequestType.WithdrawalRequest ->
            "${context.getString(R.string.a_transfer_of_dialog_message)} ${symbolAndAmountInfo.formattedAmount()} ${symbolAndAmountInfo.symbolInfo.symbol} ${symbolAndAmountInfo.formattedUSDEquivalent()} USD"
        is SolanaApprovalRequestType.UnknownApprovalType ->
            context.getString(R.string.unknown_dialog_message)
        is SolanaApprovalRequestType.ConversionRequest ->
            "${context.getString(R.string.a_conversion_of_dialog_message)} ${symbolAndAmountInfo.formattedAmount()} ${symbolAndAmountInfo.symbolInfo.symbol} ${symbolAndAmountInfo.formattedUSDEquivalent()} USD"
        is SolanaApprovalRequestType.SignersUpdate ->
            if (slotUpdateType == SlotUpdateType.Clear) {
                "${context.getString(R.string.the_removal_of_dialog_message)} ${signer.value.name}"
            } else {
                "${context.getString(R.string.the_addition_of_dialog_message)} ${signer.value.name}"
            }
        is SolanaApprovalRequestType.BalanceAccountCreation -> {
            "${context.getString(R.string.an_account_creation_of_dialog_message)} ${accountInfo.name}"
        }
        is SolanaApprovalRequestType.DAppTransactionRequest ->
            "${context.getString(R.string.a_dapp_transaction_with_dialog_message)} ${dappInfo.name}"
        is SolanaApprovalRequestType.LoginApprovalRequest ->
            context.getString(R.string.login_approval_dialog_message)
    }
}

fun AccountType.getUITitle(context: Context) =
    when(this) {
        BalanceAccount -> context.getString(R.string.balance_account_title)
        StakeAccount -> context.getString(R.string.stake_account_title)
    }

fun SymbolAndAmountInfo.getMainValueText(): String {
    return "${formattedAmount()} ${symbolInfo.symbol}"
}

fun SymbolAndAmountInfo.getUSDEquivalentText(context: Context, hideSymbol: Boolean = false) =
    "${formattedUSDEquivalent(hideSymbol = hideSymbol)} ${context.getString(R.string.usd_equivalent)}"


fun getFullDestinationName(initialValue: String, subText: String): AnnotatedString {
    return if (subText.isEmpty()) {
        buildAnnotatedString { append(initialValue) }
    } else {
        val annotatedString = buildAnnotatedString {
            append(initialValue)
            withStyle(style = SpanStyle(color = GreyText, fontSize = 8.sp)) {
                append("\n\n")
            }
            withStyle(style = SpanStyle(color = GreyText, fontSize = 14.sp)) {
                append(subText)
            }
        }
        annotatedString
    }
}

