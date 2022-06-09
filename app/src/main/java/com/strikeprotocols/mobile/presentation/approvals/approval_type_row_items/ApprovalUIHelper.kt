package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.convertSecondsIntoCountdownText
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.*
import com.strikeprotocols.mobile.data.models.approval.AccountType.*
import com.strikeprotocols.mobile.ui.theme.GreyText
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.*

fun SolanaApprovalRequestType.getHeader(context: Context): String {
    return when (this) {
        is AddressBookUpdate -> {
            val entryMetaData: Pair<AddRemoveChange, SlotDestinationInfo>? = this.getEntryMetaData()

            if (entryMetaData == null) {
                context.getString(R.string.not_applicable)
            } else if (entryMetaData.first == AddRemoveChange.ADD) {
                context.getString(R.string.add_address_book_update_approval_header)
            } else {
                context.getString(R.string.remove_address_book_update_approval_header)
            }
        }
        is BalanceAccountAddressWhitelistUpdate ->
            context.getString(R.string.balance_account_address_whitelist_update_approval_header)
        is BalanceAccountCreation ->
            if (accountInfo.accountType == BalanceAccount) {
                context.getString(R.string.balance_account_creation_approval_header)
            } else {
                context.getString(R.string.balance_account_creation_approval_header)
            }
        is BalanceAccountNameUpdate ->
            context.getString(R.string.balance_account_name_update_approval_header)
        is BalanceAccountPolicyUpdate ->
            context.getString(R.string.balance_account_policy_update_approval_header)
        is BalanceAccountSettingsUpdate -> {
            val change = this.changeValue()
            if (change is SettingsChange.DAppsEnabled && !change.dappsEnabled) {
                context.getString(R.string.disable_dapp_balance_account_settings_update_approval_header)
            } else if (change is SettingsChange.DAppsEnabled && change.dappsEnabled) {
                context.getString(R.string.enable_dapp_balance_account_settings_update_approval_header)
            } else if (change is SettingsChange.WhitelistEnabled && !change.whiteListEnabled) {
                context.getString(R.string.disable_transfer_balance_account_settings_update_approval_header)
            } else {
                context.getString(R.string.enable_transfer_balance_account_settings_update_approval_header)
            }
        }
        is ConversionRequest ->
            context.getString(R.string.conversion_request_approval_header, symbolAndAmountInfo.amount, symbolAndAmountInfo.symbolInfo.symbol)
        is DAppBookUpdate ->
            context.getString(R.string.dapp_book_update_approval_header)
        is DAppTransactionRequest ->
            context.getString(R.string.dapp_transaction_request_approval_header)
        is LoginApprovalRequest ->
            context.getString(R.string.login_approval_header)
        is SPLTokenAccountCreation ->
            context.getString(R.string.spl_token_account_creation_approval_header)
        is SignersUpdate -> {
            if (slotUpdateType == SlotUpdateType.Clear) {
                context.getString(R.string.remove_signers_update_approval_header)
            } else {
                context.getString(R.string.add_signers_update_approval_header)
            }
        }
        is WalletConfigPolicyUpdate ->
            context.getString(R.string.wallet_config_policy_update_approval_header)
        is WithdrawalRequest ->
            context.getString(R.string.withdrawal_request_approval_header, symbolAndAmountInfo.amount, symbolAndAmountInfo.symbolInfo.symbol)
        is WrapConversionRequest ->
            context.getString(R.string.wrap_conversion_request_approval_header, symbolAndAmountInfo.amount, symbolAndAmountInfo.symbolInfo.symbol)
        else ->
            context.getString(R.string.unknown_approval_header)
    }
}

fun SolanaApprovalRequestType.getApprovalTypeDialogTitle(context: Context): String {
    val stringResId = when (this) {
        is WithdrawalRequest -> R.string.transfer_dialog_title
        is UnknownApprovalType -> R.string.unknown_dialog_title
        is ConversionRequest -> R.string.conversion_dialog_title
        is SignersUpdate -> R.string.signers_update_dialog_title
        is DAppTransactionRequest -> R.string.dapp_transaction_dialog_title
        is LoginApprovalRequest -> R.string.login_approval_dialog_title
        is WrapConversionRequest -> R.string.wrap_conversion_dialog_title
        is WalletConfigPolicyUpdate -> R.string.wallet_config_dialog_title
        is BalanceAccountSettingsUpdate -> R.string.balance_account_settings_dialog_title
        is DAppBookUpdate -> R.string.dapp_book_update_dialog_title
        is AddressBookUpdate -> R.string.address_book_dialog_title
        is BalanceAccountNameUpdate -> R.string.balance_account_name_dialog_title
        is BalanceAccountPolicyUpdate -> R.string.balance_account_policy_dialog_title
        is SPLTokenAccountCreation -> R.string.spl_token_account_dialog_title
        is BalanceAccountAddressWhitelistUpdate -> R.string.balance_acct_whitelist_update_dialog_title
        is BalanceAccountCreation -> {
            if (accountInfo.accountType == BalanceAccount) {
                R.string.balance_account_creation_dialog_title
            } else {
                R.string.stake_account_creation_dialog_title
            }
        }
    }

    return context.getString(stringResId)
}

fun SolanaApprovalRequestType.getDialogFullMessage(
    context: Context,
    approvalDisposition: ApprovalDisposition,
    initiationRequest: Boolean
): String {
    return "${approvalDisposition.getDialogMessage(context, initiationRequest)} ${getApprovalTypeDialogMessage(context)}"
}

fun SolanaApprovalRequestType.getApprovalRowMetaData(context: Context): ApprovalRowMetaData {
    if (this.isUnknownTypeOrUIUnimplemented()) {
        return ApprovalRowMetaData(
            approvalImageVector = Icons.Filled.HelpOutline,
            approvalImageContentDescription = context.getString(R.string.approval_type_unknown_content_des),
            approvalTypeTitle = UnknownApprovalType.getRowTitle(context)
        )
    }

    val vectorAndContentDescription: Pair<ImageVector, String> = when (this) {
        is WithdrawalRequest -> {
            Pair(
                Icons.Filled.SyncAlt,
                context.getString(R.string.transfer_icon_content_desc),
            )
        }
        is UnknownApprovalType -> {
            Pair(
                Icons.Filled.HelpOutline,
                context.getString(R.string.approval_type_unknown_content_des),
            )
        }
        is ConversionRequest -> {
            Pair(
                Icons.Filled.Refresh,
                context.getString(R.string.conversion_icon_desc),
            )
        }
        is SignersUpdate ->
            Pair(
                Icons.Filled.PhoneAndroid,
                context.getString(R.string.signers_update_icon_content_desc),
            )
        is BalanceAccountCreation ->
            Pair(
                Icons.Filled.Lock,
                context.getString(R.string.balance_account_creation_icon_content_desc),
            )
        is DAppTransactionRequest ->
            Pair(
                Icons.Filled.Refresh,
                context.getString(R.string.approval_type_dapp_content_des),
            )
        is LoginApprovalRequest -> {
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
        }

        //todo: replace these with correct values. Need to get values from Ata.
        is AddressBookUpdate ->
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
        is BalanceAccountAddressWhitelistUpdate ->
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
        is BalanceAccountNameUpdate ->
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
        is BalanceAccountPolicyUpdate ->
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
        is BalanceAccountSettingsUpdate ->
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
        is DAppBookUpdate ->
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
        is SPLTokenAccountCreation ->
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
        is WalletConfigPolicyUpdate ->
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
        is WrapConversionRequest ->
            Pair(
                Icons.Filled.Login,
                context.getString(R.string.login_icon_content_desc),
            )
    }

    return ApprovalRowMetaData(
        approvalImageVector = vectorAndContentDescription.first,
        approvalImageContentDescription = vectorAndContentDescription.second,
        approvalTypeTitle = getRowTitle(context)
    )
}

fun SolanaApprovalRequestType.isUnknownTypeOrUIUnimplemented() =
    this is UnknownApprovalType || this is DAppBookUpdate

fun SolanaApprovalRequestType.getRowTitle(context: Context): String =
    when (this) {
        is WithdrawalRequest,
        is ConversionRequest,
        is WrapConversionRequest,
        is BalanceAccountNameUpdate,
        is BalanceAccountPolicyUpdate,
        is BalanceAccountSettingsUpdate,
        is BalanceAccountAddressWhitelistUpdate,
        is DAppTransactionRequest,
        is SPLTokenAccountCreation -> context.getString(R.string.wallet_change)

        is SignersUpdate,
        is AddressBookUpdate,
        is BalanceAccountCreation,
        is WalletConfigPolicyUpdate,
        is DAppBookUpdate -> context.getString(R.string.vault_change)

        is LoginApprovalRequest -> context.getString(R.string.authentication)

        is UnknownApprovalType -> context.getString(R.string.unknown)
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
        is WithdrawalRequest ->
            "${context.getString(R.string.a_transfer_of_dialog_message)} ${symbolAndAmountInfo.formattedAmount()} ${symbolAndAmountInfo.symbolInfo.symbol} ${symbolAndAmountInfo.formattedUSDEquivalent()} USD"
        is UnknownApprovalType ->
            context.getString(R.string.unknown_dialog_message)
        is ConversionRequest ->
            "${context.getString(R.string.a_conversion_of_dialog_message)} ${symbolAndAmountInfo.formattedAmount()} ${symbolAndAmountInfo.symbolInfo.symbol} ${symbolAndAmountInfo.formattedUSDEquivalent()} USD"
        is SignersUpdate ->
            if (slotUpdateType == SlotUpdateType.Clear) {
                "${context.getString(R.string.the_removal_of_dialog_message)} ${signer.value.name}"
            } else {
                "${context.getString(R.string.the_addition_of_dialog_message)} ${signer.value.name}"
            }
        is BalanceAccountCreation -> {
            "${context.getString(R.string.an_account_creation_of_dialog_message)} ${accountInfo.name}"
        }
        is DAppTransactionRequest ->
            "${context.getString(R.string.a_dapp_transaction_with_dialog_message)} ${dappInfo.name}"
        is LoginApprovalRequest ->
            context.getString(R.string.login_approval_dialog_message)

        is WrapConversionRequest -> {
            "${context.getString(R.string.a_conversion_of_dialog_message)} ${symbolAndAmountInfo.formattedAmount()} ${symbolAndAmountInfo.symbolInfo.symbol} to ${destinationSymbolInfo.symbol}"
        }
        is WalletConfigPolicyUpdate -> {
            context.getString(R.string.vault_config_policy_update_dialog_message)
        }
        is BalanceAccountSettingsUpdate -> {
            "${context.getString(R.string.settings_update_for_dialog_message)} ${account.name}"
        }
        is DAppBookUpdate -> {
            context.getString(R.string.dapp_book_update_dialog_message)
        }
        is AddressBookUpdate -> {
            context.getString(R.string.address_book_update_dialog_message)
        }
        is BalanceAccountNameUpdate -> {
            "${context.getString(R.string.wallet_name_change_to_dialog_message)} $newAccountName"
        }
        is BalanceAccountPolicyUpdate -> {
            "${context.getString(R.string.policy_update_for_dialog_message)} ${accountInfo.name}"
        }
        is SPLTokenAccountCreation -> {
            "${context.getString(R.string.spl_token_account_dialog_message)} ${tokenSymbolInfo.symbolDescription}"
        }
        is BalanceAccountAddressWhitelistUpdate -> {
            "${context.getString(R.string.balance_account_whitelist_dialog_message)} ${accountInfo.name}"
        }
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

fun getApprovalTimerText(context: Context, timeRemainingInSeconds: Long) : String {
    val timerFinished = timeRemainingInSeconds <= 0

    return if (timerFinished) {
        context.getString(R.string.approval_expired)
    } else {
        "${context.getString(R.string.expires_in)} ${convertSecondsIntoCountdownText(context, timeRemainingInSeconds)}"
    }
}

fun buildFromToDisplayText(from: String, to: String, context: Context): String {
    return "$from ${context.getString(R.string.to).lowercase()} $to"
}

