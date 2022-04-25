package com.strikeprotocols.mobile.presentation.approvals.approval_type_items

import android.content.Context
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.AccountType
import com.strikeprotocols.mobile.data.models.approval.SlotUpdateType
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType

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
            if (accountInfo.accountType == AccountType.BalanceAccount) {
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
    approvalDisposition: ApprovalDisposition
): String {
    return "${approvalDisposition.getDialogMessage(context)} ${getApprovalTypeDialogMessage(context)}"
}

fun ApprovalDisposition.getDialogMessage(context: Context): String {
    return if (this == ApprovalDisposition.APPROVE) {
        context.getString(R.string.you_are_about_to_approve)
    } else {
        context.getString(R.string.you_are_about_to_deny)
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

