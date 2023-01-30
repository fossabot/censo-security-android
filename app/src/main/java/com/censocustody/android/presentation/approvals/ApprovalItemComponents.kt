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
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.data.models.approval.ApprovalRequest
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
    type: ApprovalRequestDetails,
    approval: ApprovalRequest,
) {
    when (type) {
        is ApprovalRequestDetails.WithdrawalRequest ->
            WithdrawalRequestRowContent(withdrawalRequest = type)
        is ApprovalRequestDetails.WalletCreation ->
            WalletCreationRowContent(walletCreation = type)
        is ApprovalRequestDetails.DAppTransactionRequest ->
            DAppTransactionRowContent(dAppTransactionRequest = type)
        is ApprovalRequestDetails.LoginApprovalRequest ->
            LoginApprovalRowContent(loginApproval = type)
        is ApprovalRequestDetails.CreateAddressBookEntry ->
            CreateOrUpdateAddressBookEntryRowContent(
                header = type.getHeader(LocalContext.current),
                entryName = type.name
            )
        is ApprovalRequestDetails.DeleteAddressBookEntry ->
            CreateOrUpdateAddressBookEntryRowContent(
                header = type.getHeader(LocalContext.current),
                entryName = type.name
            )
        is ApprovalRequestDetails.BalanceAccountAddressWhitelistUpdate ->
            BalanceAccountAddressWhitelistUpdateRowContent(accountAddressWhitelistUpdate = type)
        is ApprovalRequestDetails.BalanceAccountNameUpdate ->
            BalanceAccountNameUpdateRowContent(accountNameUpdate = type)
        is ApprovalRequestDetails.BalanceAccountPolicyUpdate ->
            BalanceAccountPolicyUpdateRowContent(accountPolicyUpdate = type)
        is ApprovalRequestDetails.BalanceAccountSettingsUpdate ->
            BalanceAccountSettingsUpdateRowContent(accountSettingsUpdate = type)
        is ApprovalRequestDetails.WalletConfigPolicyUpdate ->
            WalletConfigPolicyUpdateRowContent(walletConfigPolicyUpdate = type)
        is ApprovalRequestDetails.AcceptVaultInvitation ->
            AcceptVaultInvitationRowContent(acceptVaultInvitation = type)
        is ApprovalRequestDetails.PasswordReset ->
            PasswordResetRowContent(passwordReset = type, email = approval.submitterEmail ?: "")
        else -> {
            val header = type.getHeader(LocalContext.current)
            Text(text = header)
        }
    }
}

@Composable
fun ApprovalDetailContent(approval: ApprovalRequest, type: ApprovalRequestDetails) {
    when (type) {
        is ApprovalRequestDetails.WalletCreation ->
            BalanceAccountDetailContent(walletCreation = type, approvalsReceived = approval.numberOfApprovalsReceived.toString())
        is ApprovalRequestDetails.DAppTransactionRequest ->
            DAppTransactionDetailContent(dAppTransactionRequest = type)
        is ApprovalRequestDetails.LoginApprovalRequest -> {
            LoginApprovalDetailContent(loginApproval = type)
        }
        is ApprovalRequestDetails.WithdrawalRequest ->
            WithdrawalRequestDetailContent(withdrawalRequest = type)
        is ApprovalRequestDetails.CreateAddressBookEntry ->
            CreateOrDeleteAddressBookEntryDetailContent(
                header = type.getHeader(LocalContext.current),
                chain = type.chain,
                entryName = type.name,
                entryAddress = type.address
            )
        is ApprovalRequestDetails.DeleteAddressBookEntry ->
            CreateOrDeleteAddressBookEntryDetailContent(
                header = type.getHeader(LocalContext.current),
                chain = type.chain,
                entryName = type.name,
                entryAddress = type.address
            )
        is ApprovalRequestDetails.BalanceAccountAddressWhitelistUpdate ->
            BalanceAccountAddressWhitelistUpdateDetailContent(addressWhitelistUpdate = type)
        is ApprovalRequestDetails.BalanceAccountNameUpdate ->
            BalanceAccountNameUpdateDetailContent(accountNameUpdate = type)
        is ApprovalRequestDetails.BalanceAccountPolicyUpdate ->
            BalanceAccountPolicyUpdateDetailContent(accountPolicyUpdate = type)
        is ApprovalRequestDetails.BalanceAccountSettingsUpdate ->
            BalanceAccountSettingsUpdateDetailContent(accountSettingsUpdate = type)
        is ApprovalRequestDetails.WalletConfigPolicyUpdate ->
            WalletConfigPolicyUpdateDetailContent(walletConfigPolicyUpdate = type)
        is ApprovalRequestDetails.AcceptVaultInvitation ->
            AcceptVaultInvitationDetailContent(acceptVaultInvitation = type)
        is ApprovalRequestDetails.PasswordReset ->
            PasswordResetDetailContent(passwordReset = type)
        else -> Text(text = stringResource(R.string.unknown_approval_item))
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