package com.censocustody.android.presentation.approvals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.common.convertSecondsIntoCountdownText
import com.censocustody.android.common.toVaultName
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.*
import com.censocustody.android.presentation.approvals.approval_type_row_items.*
import com.censocustody.android.ui.theme.*


@Composable
fun ApprovalItemHeader(
    timeRemainingInSeconds: Long?,
    vaultName: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBlack)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultTitle(
            modifier = Modifier.weight(7.5f),
            vaultName = vaultName ?: ""
        )

        convertSecondsIntoCountdownText(LocalContext.current, timeRemainingInSeconds)?.let { timerText ->
            Text(
                modifier = Modifier.weight(2.5f),
                text = timerText,
                textAlign = TextAlign.End,
                color = GreyText
            )
        }
    }
}

@Composable
fun ApprovalContentHeader(
    header: String,
    topSpacing: Int = 0,
    bottomSpacing: Int = 0
) {
    if (topSpacing > 0) {
        Spacer(Modifier.height(topSpacing.dp))
    }

    ApprovalRowTitleText(title = header)

    if (bottomSpacing > 0) {
        Spacer(Modifier.height(bottomSpacing.dp))
    }
}

@Composable
fun ApprovalRowContentHeader(
    header: String,
    topSpacing: Int = 16,
    bottomSpacing: Int = 20
) {
    ApprovalContentHeader(header = header, topSpacing = topSpacing, bottomSpacing = bottomSpacing)
}

@Composable
fun ApprovalButtonRow(
    onApproveClicked: () -> Unit,
    onMoreInfoClicked: () -> Unit,
    positiveButtonText: String
) {
    Column(modifier = Modifier.background(DetailInfoLightBackground)) {
        Divider(color = DividerGrey, modifier = Modifier.height(0.5.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp, bottom = 4.dp),
                onClick = onApproveClicked) {
                Text(
                    positiveButtonText,
                    color = ApprovalGreen,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
            }
            Divider(
                color = DividerGrey,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(0.5.dp)
            )
            TextButton(
                modifier = Modifier
                    .weight(1f),
                onClick = onMoreInfoClicked) {
                Text(
                    stringResource(R.string.more_info),
                    color = CensoWhite,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
            }
        }
    }
}


@Composable
fun VaultTitle(
    vaultName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(start = 2.dp),
            text = vaultName,
            color = CensoWhite
        )
    }
}

@Composable
fun ApprovalRowContent(
    type: ApprovalRequestDetailsV2,
    approval: ApprovalRequestV2,
) {

    when (type) {
        //WalletConfigPolicyUpdate
        is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
            WalletConfigPolicyUpdateRowContent(header = type.getHeader(LocalContext.current))
        }
        //Wallet Creation
        is ApprovalRequestDetailsV2.BitcoinWalletCreation,
        is ApprovalRequestDetailsV2.EthereumWalletCreation,
        is ApprovalRequestDetailsV2.PolygonWalletCreation -> {
            val header = type.getHeader(LocalContext.current)
            val accountName = type.walletCreationAccountName()
            WalletCreationRowContent(header = header, accountName = accountName)
        }
        //BalanceAccountNameUpdate
        is ApprovalRequestDetailsV2.EthereumWalletNameUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletNameUpdate -> {
            val header = type.getHeader(LocalContext.current)
            val oldName = type.walletNameOldAccountName()
            val newName = type.walletNameNewAccountName()
            BalanceAccountNameUpdateRowContent(
                header = header, oldName = oldName, newName = newName
            )
        }

        //CreateAddressBookEntry
        is ApprovalRequestDetailsV2.CreateAddressBookEntry -> {
            CreateOrUpdateAddressBookEntryRowContent(
                header = type.getHeader(LocalContext.current),
                entryName = type.name
            )
        }

        //DeleteAddressBookEntry
        is ApprovalRequestDetailsV2.DeleteAddressBookEntry -> {
            CreateOrUpdateAddressBookEntryRowContent(
                header = type.getHeader(LocalContext.current),
                entryName = type.name
            )
        }

        //BalanceAccountAddressWhitelistUpdate
        is ApprovalRequestDetailsV2.EthereumWalletWhitelistUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletWhitelistUpdate -> {
            val header = type.getHeader(LocalContext.current)
            val accountName = type.whitelistUpdateName()
            BalanceAccountAddressWhitelistUpdateRowContent(
                header = header, accountName = accountName
            )
        }

        //BalanceAccountSettingsUpdate
        is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
            val header = type.getHeader(LocalContext.current)
            val name = type.walletSettingsUpdateName()
            BalanceAccountSettingsUpdateRowContent(
                header = header,
                name = name
            )
        }

        //WithdrawalRequest
        is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest,
        is ApprovalRequestDetailsV2.EthereumWithdrawalRequest,
        is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> {
            val header = type.getHeader(LocalContext.current)
            val subtitle = type.withdrawalRequestSubtitle(LocalContext.current)
            val fromAndToAccount = type.withdrawalRequestFromAndToAccount()
            WithdrawalRequestRowContent(
                header = header, subtitle = subtitle,
                fromAccount = fromAndToAccount.first,
                toAccount = fromAndToAccount.second
            )
        }

        //BalanceAccountPolicyUpdate
        is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate,
        is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
            val header = type.getHeader(LocalContext.current)
            val name = type.transferPolicyUpdateName()
            BalanceAccountPolicyUpdateRowContent(header = header, name = name)
        }

        //LoginApproval
        is ApprovalRequestDetailsV2.Login -> {
            LoginApprovalRowContent(
                header = type.getHeader(LocalContext.current),
                email = type.email
            )
        }

        //PasswordReset
        is ApprovalRequestDetailsV2.PasswordReset -> {
            PasswordResetRowContent(
                header = type.getHeader(LocalContext.current),
                email = approval.submitterEmail
            )
        }

        //AcceptVaultInvitation
        is ApprovalRequestDetailsV2.VaultInvitation -> {
            AcceptVaultInvitationRowContent(
                header = type.getHeader(LocalContext.current),
                vaultName = type.vaultName.toVaultName(LocalContext.current)
            )
        }

        //Unknown
        ApprovalRequestDetailsV2.UnknownApprovalType -> {
            val header = type.getHeader(LocalContext.current)
            Text(text = header, color = CensoWhite)
        }
    }
}

@Composable
fun ApprovalDetailContent(approval: ApprovalRequestV2, type: ApprovalRequestDetailsV2) {
    when (type) {
        is ApprovalRequestDetailsV2.VaultInvitation -> TODO()
        is ApprovalRequestDetailsV2.BitcoinWalletCreation -> TODO()
        is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest -> TODO()
        is ApprovalRequestDetailsV2.CreateAddressBookEntry -> TODO()
        is ApprovalRequestDetailsV2.DeleteAddressBookEntry -> TODO()
        is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate -> TODO()
        is ApprovalRequestDetailsV2.EthereumWalletCreation -> TODO()
        is ApprovalRequestDetailsV2.EthereumWalletNameUpdate -> TODO()
        is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate -> TODO()
        is ApprovalRequestDetailsV2.EthereumWalletWhitelistUpdate -> TODO()
        is ApprovalRequestDetailsV2.EthereumWithdrawalRequest -> TODO()
        is ApprovalRequestDetailsV2.Login -> TODO()
        ApprovalRequestDetailsV2.PasswordReset -> TODO()
        is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> TODO()
        is ApprovalRequestDetailsV2.PolygonWalletCreation -> TODO()
        is ApprovalRequestDetailsV2.PolygonWalletNameUpdate -> TODO()
        is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> TODO()
        is ApprovalRequestDetailsV2.PolygonWalletWhitelistUpdate -> TODO()
        is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> TODO()
        ApprovalRequestDetailsV2.UnknownApprovalType -> TODO()
        is ApprovalRequestDetailsV2.VaultPolicyUpdate -> TODO()
        else -> Text(text = stringResource(R.string.unknown_approval_item))

//        is ApprovalRequestDetails.WalletCreation ->
//            BalanceAccountDetailContent(walletCreation = type, approvalsReceived = approval.numberOfApprovalsReceived.toString())
//        is ApprovalRequestDetails.ConversionRequest ->
//            ConversionDetailContent(conversionRequest = type)
//        is ApprovalRequestDetails.DAppTransactionRequest ->
//            DAppTransactionDetailContent(dAppTransactionRequest = type)
//        is ApprovalRequestDetails.LoginApprovalRequest -> {
//            LoginApprovalDetailContent(loginApproval = type)
//        }
//        is ApprovalRequestDetails.SignersUpdate ->
//            SignersUpdateDetailContent(signersUpdate = type)
//        is ApprovalRequestDetails.WithdrawalRequest ->
//            WithdrawalRequestDetailContent(withdrawalRequest = type)
//        is ApprovalRequestDetails.CreateAddressBookEntry ->
//            CreateOrDeleteAddressBookEntryDetailContent(
//                header = type.getHeader(LocalContext.current),
//                chain = type.chain,
//                entryName = type.name,
//                entryAddress = type.address
//            )
//        is ApprovalRequestDetails.DeleteAddressBookEntry ->
//            CreateOrDeleteAddressBookEntryDetailContent(
//                header = type.getHeader(LocalContext.current),
//                chain = type.chain,
//                entryName = type.name,
//                entryAddress = type.address
//            )
//        is ApprovalRequestDetails.BalanceAccountAddressWhitelistUpdate ->
//            BalanceAccountAddressWhitelistUpdateDetailContent(addressWhitelistUpdate = type)
//        is ApprovalRequestDetails.BalanceAccountNameUpdate ->
//            BalanceAccountNameUpdateDetailContent(accountNameUpdate = type)
//        is ApprovalRequestDetails.BalanceAccountPolicyUpdate ->
//            BalanceAccountPolicyUpdateDetailContent(accountPolicyUpdate = type)
//        is ApprovalRequestDetails.BalanceAccountSettingsUpdate ->
//            BalanceAccountSettingsUpdateDetailContent(accountSettingsUpdate = type)
//        is ApprovalRequestDetails.DAppBookUpdate -> DAppBookUpdateDetailContent()
//        is ApprovalRequestDetails.WalletConfigPolicyUpdate ->
//            WalletConfigPolicyUpdateDetailContent(walletConfigPolicyUpdate = type)
//        is ApprovalRequestDetails.WrapConversionRequest ->
//            WrapConversionRequestDetailContent(wrapConversionRequest = type)
//        is ApprovalRequestDetails.AcceptVaultInvitation ->
//            AcceptVaultInvitationDetailContent(acceptVaultInvitation = type)
//        is ApprovalRequestDetails.PasswordReset ->
//            PasswordResetDetailContent(passwordReset = type)
    }
}

@Composable
fun DurableNonceErrorDialog(
    dismissDialog: () -> Unit
) {
    AlertDialog(
        backgroundColor = UnfocusedGrey,
        onDismissRequest = dismissDialog,
        title = {
            Text(
                text = stringResource(R.string.approval_disposition_error_title),
                color = CensoWhite,
                fontSize = 22.sp
            )
        },
        text = {
            Text(
                text = stringResource(R.string.default_error_message),
                color = CensoWhite,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = dismissDialog
            ) {
                Text(text = stringResource(R.string.ok))
            }
        }
    )
}