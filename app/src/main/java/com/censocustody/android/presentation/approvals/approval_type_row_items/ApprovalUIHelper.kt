package com.censocustody.android.presentation.approvals.approval_type_row_items

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.common.convertSecondsIntoCountdownText
import com.censocustody.android.common.maskAddress
import com.censocustody.android.common.toWalletName
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approval.*
import com.censocustody.android.data.models.approval.AccountType.*
import com.censocustody.android.ui.theme.GreyText
import com.censocustody.android.data.models.approval.ApprovalRequestDetails.*
import com.censocustody.android.presentation.components.RowData

fun ApprovalRequestDetails.getHeader(context: Context): String {
    return when (this) {
        is CreateAddressBookEntry ->
            context.getString(R.string.add_address_book_update_approval_header)
        is DeleteAddressBookEntry ->
            context.getString(R.string.remove_address_book_update_approval_header)
        is BalanceAccountAddressWhitelistUpdate ->
            context.getString(R.string.balance_account_address_whitelist_update_approval_header)
        is WalletCreation ->
            if (accountInfo.accountType == BalanceAccount) {
                "${context.getString(R.string.add)} ${accountInfo.chain?.label() ?: context.getString(R.string.solana)} ${context.getString(R.string.wallet_title)}"
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
        is DAppTransactionRequest ->
            context.getString(R.string.dapp_transaction_request_approval_header)
        is LoginApprovalRequest ->
            context.getString(R.string.login_approval_header)
        is AcceptVaultInvitation ->
            context.getString(R.string.accept_vault_invitation_approval_header)
        is PasswordReset ->
            context.getString(R.string.password_reset_approval_header)
        is WalletConfigPolicyUpdate ->
            context.getString(R.string.wallet_config_policy_update_approval_header)
        is WithdrawalRequest ->
            if (this.symbolAndAmountInfo.replacementFee == null) {
                context.getString(R.string.withdrawal_request_approval_header, symbolAndAmountInfo.amount, symbolAndAmountInfo.symbolInfo.symbol)
            } else {
                context.getString(R.string.bump_fee_request_approval_header)
            }
        else ->
            context.getString(R.string.unknown_approval_header)
    }
}

fun ApprovalRequestDetails.getDialogMessages(
    context: Context,
    approvalDisposition: ApprovalDisposition,
    isInitiationRequest: Boolean
) : Pair<String, String> {
    val mainText = approvalDisposition.getDialogMessage(context, isInitiationRequest)
    val secondaryText = getHeader(context)

    return Pair(mainText, secondaryText)
}

fun ApprovalRequestDetails.getApprovalRowMetaData(vaultName: String?): ApprovalRowMetaData {
    if (this.isUnknownTypeOrUIUnimplemented()) {
        return ApprovalRowMetaData(
            vaultName = null
        )
    }

    return ApprovalRowMetaData(
        vaultName = getRowTitle(vaultName)
    )
}

fun ApprovalRequestDetails.isUnknownTypeOrUIUnimplemented() = this is UnknownApprovalType

fun ApprovalRequestDetails.getRowTitle(vaultName: String?): String? =
    when (this) {
        is AcceptVaultInvitation -> null
        else -> vaultName
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

fun getApprovalTimerText(context: Context, timeRemainingInSeconds: Long?) : String? {
    if(timeRemainingInSeconds == null) return null

    val timerFinished = timeRemainingInSeconds <= 0

    return if (timerFinished) {
        context.getString(R.string.approval_expired)
    } else {
        "${context.getString(R.string.expires_in)} ${convertSecondsIntoCountdownText(context, timeRemainingInSeconds)}"
    }
}

fun String.nameToInitials() =
    try {
        val splitName = this.split(" ")
        val initialBuilder = StringBuilder()
        for (name in splitName) {
            initialBuilder.append(name[0])
        }
        initialBuilder.toString().trim()
    } catch (e: Exception) {
        try {
            this[0].toString()
        } catch (e: Exception) {
            ""
        }
    }

fun buildFromToDisplayText(from: String, to: String, context: Context): String {
    return "${from.toWalletName()} ${context.getString(R.string.to).lowercase()} ${to.toWalletName()}"
}

private fun List<SlotSignerInfo>.sortApprovers() = this.sortedBy { it.value.name }
private fun List<SlotDestinationInfo>.sortDestinations() = this.sortedBy { it.value.name }

fun List<SlotDestinationInfo>.retrieveDestinationsRowData() : MutableList<RowData> {
    val destinationsList = mutableListOf<RowData>()
    if (isNotEmpty()) {
        for (destination in sortDestinations()) {
            destinationsList.add(
                RowData(
                    title = destination.value.name,
                    value = destination.value.address.maskAddress(),
                )
            )
        }
    }
    return destinationsList
}

fun List<SlotSignerInfo>.retrieveSlotRowData(): MutableList<RowData> {
    val approversList = mutableListOf<RowData>()
    if (isNotEmpty()) {
        for (approver in sortApprovers()) {
            approversList.add(
                RowData(
                    title = approver.value.name,
                    value = approver.value.email,
                    userImage = approver.value.jpegThumbnail,
                    userRow = true
                ))
        }
    }
    return approversList
}

