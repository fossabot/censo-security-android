package com.censocustody.android.presentation.approvals.approval_type_row_items

import android.content.Context
import androidx.compose.ui.platform.LocalContext
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
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.components.RowData
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

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
        is ConversionRequest ->
            context.getString(R.string.conversion_request_approval_header, symbolAndAmountInfo.amount, symbolAndAmountInfo.symbolInfo.symbol)
        is DAppBookUpdate ->
            context.getString(R.string.dapp_book_update_approval_header)
        is DAppTransactionRequest ->
            context.getString(R.string.dapp_transaction_request_approval_header)
        is LoginApprovalRequest ->
            context.getString(R.string.login_approval_header)
        is SignersUpdate -> {
            if (slotUpdateType == SlotUpdateType.Clear) {
                context.getString(R.string.remove_signers_update_approval_header)
            } else {
                context.getString(R.string.add_signers_update_approval_header)
            }
        }
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
        is WrapConversionRequest ->
            context.getString(R.string.wrap_conversion_request_approval_header, symbolAndAmountInfo.amount, symbolAndAmountInfo.symbolInfo.symbol)
        else ->
            context.getString(R.string.unknown_approval_header)
    }
}

fun ApprovalRequestDetailsV2.getHeader(context: Context) =

    when (this) {
        //WalletConfigPolicyUpdate
        is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
            context.getString(R.string.wallet_config_policy_update_approval_header)
        }
        //Wallet Creation
        is ApprovalRequestDetailsV2.BitcoinWalletCreation,
        is ApprovalRequestDetailsV2.EthereumWalletCreation,
        is ApprovalRequestDetailsV2.PolygonWalletCreation -> {
            context.getString(R.string.balance_account_creation_approval_header)
            //todo: check if we can re-add this
//            if (accountInfo.accountType == BalanceAccount) {
//                "${context.getString(R.string.add)} ${
//                    accountInfo.chain?.label() ?: context.getString(
//                        R.string.solana
//                    )
//                } ${context.getString(R.string.wallet_title)}"
//            } else {
//                context.getString(R.string.balance_account_creation_approval_header)
//            }
        }
        //BalanceAccountNameUpdate
        is ApprovalRequestDetailsV2.EthereumWalletNameUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletNameUpdate -> {
            context.getString(R.string.balance_account_name_update_approval_header)
        }

        //CreateAddressBookEntry
        is ApprovalRequestDetailsV2.CreateAddressBookEntry -> {
            context.getString(R.string.add_address_book_update_approval_header)
        }

        //DeleteAddressBookEntry
        is ApprovalRequestDetailsV2.DeleteAddressBookEntry -> {
            context.getString(R.string.remove_address_book_update_approval_header)
        }

        //BalanceAccountAddressWhitelistUpdate
        is ApprovalRequestDetailsV2.EthereumWalletWhitelistUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletWhitelistUpdate -> {
            context.getString(R.string.balance_account_address_whitelist_update_approval_header)
        }

        //BalanceAccountSettingsUpdate
        is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
            val change = when (this) {
                is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate -> {
                    this.changeValue()
                }
                is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
                    this.changeValue()
                }
                else -> null
            }
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

        //WithdrawalRequest
        is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest,
        is ApprovalRequestDetailsV2.EthereumWithdrawalRequest,
        is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> {
            var replacementFee: ApprovalRequestDetailsV2.Amount? = null
            var amount: String? = null
            var symbol: String? = null

            when (this) {
                is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest -> {
                    replacementFee = this.replacementFee
                    amount = this.amount.value
                    symbol = this.symbolInfo.symbol
                }
                is ApprovalRequestDetailsV2.EthereumWithdrawalRequest -> {
                    amount = this.amount.value
                    symbol = this.symbolInfo.symbol
                }
                is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> {
                    amount = this.amount.value
                    symbol = this.symbolInfo.symbol
                }
                else -> null
            }

            if (replacementFee == null) {
                context.getString(R.string.withdrawal_request_approval_header, amount, symbol)
            } else {
                context.getString(R.string.bump_fee_request_approval_header)
            }
        }

        //BalanceAccountPolicyUpdate
        is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate,
        is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
            context.getString(R.string.balance_account_policy_update_approval_header)
        }

        //LoginApproval
        is ApprovalRequestDetailsV2.Login -> {
            context.getString(R.string.login_approval_header)
        }

        //PasswordReset
        is ApprovalRequestDetailsV2.PasswordReset -> {
            context.getString(R.string.password_reset_approval_header)
        }

        //AcceptVaultInvitation
        is ApprovalRequestDetailsV2.VaultInvitation -> {
            context.getString(R.string.accept_vault_invitation_approval_header)
        }
        ApprovalRequestDetailsV2.UnknownApprovalType -> {
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

fun ApprovalRequestDetailsV2.getDialogMessages(
    context: Context,
    approvalDisposition: ApprovalDisposition,
) : Pair<String, String> {
    val mainText = approvalDisposition.getDialogMessage(context, false)
    val secondaryText = this.getHeader(context)

    return Pair(mainText, secondaryText)
}

fun ApprovalRequestDetailsV2.getApprovalRowMetaData(vaultName: String?): ApprovalRowMetaData {
    if (this.isUnknownTypeOrUIUnimplemented()) {
        return ApprovalRowMetaData(
            vaultName = null
        )
    }

    return ApprovalRowMetaData(
        vaultName = getRowTitle(vaultName)
    )
}

fun ApprovalRequestDetailsV2.isUnknownTypeOrUIUnimplemented() =
    this is ApprovalRequestDetailsV2.UnknownApprovalType

fun ApprovalRequestDetailsV2.getRowTitle(vaultName: String?): String? =
    when (this) {
        is ApprovalRequestDetailsV2.VaultInvitation -> null
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

fun ApprovalRequestDetailsV2.walletCreationAccountName() =
    when (this) {
        is ApprovalRequestDetailsV2.BitcoinWalletCreation -> {
            this.name
        }
        is ApprovalRequestDetailsV2.EthereumWalletCreation -> {
            this.name
        }
        is ApprovalRequestDetailsV2.PolygonWalletCreation -> {
            this.name
        }
        else -> ""
    }

fun ApprovalRequestDetailsV2.walletNameOldAccountName() =
    when (this) {
        is ApprovalRequestDetailsV2.EthereumWalletNameUpdate -> {
            this.wallet.name
        }
        is ApprovalRequestDetailsV2.PolygonWalletNameUpdate -> {
            this.wallet.name
        }
        else -> ""
    }

fun ApprovalRequestDetailsV2.walletNameNewAccountName() =
    when (this) {
        is ApprovalRequestDetailsV2.EthereumWalletNameUpdate -> {
            this.newName
        }
        is ApprovalRequestDetailsV2.PolygonWalletNameUpdate -> {
            this.newName
        }
        else -> ""
    }

fun ApprovalRequestDetailsV2.whitelistUpdateName() =
    when (this) {
        is ApprovalRequestDetailsV2.EthereumWalletWhitelistUpdate -> {
            wallet.name
        }
        is ApprovalRequestDetailsV2.PolygonWalletWhitelistUpdate -> {
            wallet.name
        }
        else -> ""
    }

fun ApprovalRequestDetailsV2.walletSettingsUpdateName() =
    when (this) {
        is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate -> {
            wallet.name
        }
        is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
            wallet.name
        }
        else -> ""
    }

fun ApprovalRequestDetailsV2.withdrawalRequestSubtitle(context: Context): String =
    when (this) {
        is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest -> {
            val symbolAndAmountInfo = symbolInfo
            if (replacementFee == null) {
                getUSDEquivalentTextV2(
                    context = context,
                    usdEquivalent = amount.usdEquivalent,
                    hideSymbol = true
                )
            } else {
                context.getString(
                    R.string.bump_fee_request_approval_subtitle,
                    amount,
                    symbolAndAmountInfo.symbol
                )
            }
        }
        is ApprovalRequestDetailsV2.EthereumWithdrawalRequest -> {
            getUSDEquivalentTextV2(
                context = context,
                usdEquivalent = amount.usdEquivalent,
                hideSymbol = true
            )
        }
        is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> {
            getUSDEquivalentTextV2(
                context = context,
                usdEquivalent = amount.usdEquivalent,
                hideSymbol = true
            )
        }
        else -> ""
    }

fun ApprovalRequestDetailsV2.withdrawalRequestFromAndToAccount() =
    when (this) {
        is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest -> Pair(wallet.name, destination.name)
        is ApprovalRequestDetailsV2.EthereumWithdrawalRequest -> Pair(wallet.name, destination.name)
        is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> Pair(wallet.name, destination.name)
        else -> Pair("", "")
    }


fun ApprovalRequestDetailsV2.transferPolicyUpdateName() =
    when (this) {
        is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate -> wallet.name
        is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> wallet.name
        else -> ""
    }

fun formattedUSDEquivalentV2(usdEquivalent: String?, hideSymbol: Boolean = true): String {
    if (usdEquivalent == null) {
        return ""
    }

    val decimal = usdEquivalent.toBigDecimal()
    return usdFormatterV2(hideSymbol).format(decimal)
}

fun getUSDEquivalentTextV2(context: Context, usdEquivalent: String?, hideSymbol: Boolean = false) =
    "${formattedUSDEquivalentV2(hideSymbol = hideSymbol, usdEquivalent = usdEquivalent)} ${context.getString(R.string.usd_equivalent)}"

private fun usdFormatterV2(hideSymbol: Boolean = true): DecimalFormat {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US) as DecimalFormat
    if (hideSymbol) {
        val symbols: DecimalFormatSymbols = formatter.decimalFormatSymbols
        symbols.currencySymbol = ""
        formatter.decimalFormatSymbols = symbols
    }
    return formatter
}

//MAPPING OLD TYPES TO NEW TYPES FOR WHEN CLAUSES
//when(this) {
//    //WalletConfigPolicyUpdate
//    is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
//    }
//    //Wallet Creation
//    is ApprovalRequestDetailsV2.BitcoinWalletCreation,
//    is ApprovalRequestDetailsV2.EthereumWalletCreation,
//    is ApprovalRequestDetailsV2.PolygonWalletCreation -> {
//
//    }
//    //BalanceAccountNameUpdate
//    is ApprovalRequestDetailsV2.EthereumWalletNameUpdate,
//    is ApprovalRequestDetailsV2.PolygonWalletNameUpdate -> {
//
//    }
//
//    //CreateAddressBookEntry
//    is ApprovalRequestDetailsV2.CreateAddressBookEntry -> {
//        context.getString(R.string.add_address_book_update_approval_header)
//    }
//
//    //DeleteAddressBookEntry
//    is ApprovalRequestDetailsV2.DeleteAddressBookEntry -> {
//        context.getString(R.string.remove_address_book_update_approval_header)
//    }
//
//    //BalanceAccountAddressWhitelistUpdate
//    is ApprovalRequestDetailsV2.EthereumWalletWhitelistUpdate,
//    is ApprovalRequestDetailsV2.PolygonWalletWhitelistUpdate -> {
//        context.getString(R.string.balance_account_address_whitelist_update_approval_header)
//    }
//
//    //BalanceAccountSettingsUpdate
//    is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate,
//    is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
//
//    }
//
//    //WithdrawalRequest
//    is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest,
//    is ApprovalRequestDetailsV2.EthereumWithdrawalRequest,
//    is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> {
//
//    }
//
//    //DAppTransactionRequest
//
//    //BalanceAccountPolicyUpdate
//    is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate,
//    is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
//
//    }
//
//    //LoginApproval
//    is ApprovalRequestDetailsV2.Login -> {
//
//    }
//
//    //PasswordReset
//    is ApprovalRequestDetailsV2.PasswordReset -> {
//
//    }
//
//    //AcceptVaultInvitation
//    is ApprovalRequestDetailsV2.VaultInvitation -> {
//
//    }
//    ApprovalRequestDetailsV2.UnknownApprovalType -> TODO()
//}