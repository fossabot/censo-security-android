package com.strikeprotocols.mobile.presentation.approvals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.convertSecondsIntoCountdownText
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.*
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.*
import com.strikeprotocols.mobile.ui.theme.*


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
                    color = StrikeWhite,
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
            color = StrikeWhite
        )
    }
}

@Composable
fun ApprovalRowContent(
    type: SolanaApprovalRequestType,
    approval: WalletApproval,
) {
    when (type) {
        is SolanaApprovalRequestType.WithdrawalRequest ->
            WithdrawalRequestRowContent(withdrawalRequest = type)
        is SolanaApprovalRequestType.ConversionRequest ->
            ConversionRequestRowContent(conversionRequest = type)
        is SolanaApprovalRequestType.SignersUpdate ->
            SignersUpdateRowContent(signersUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountCreation ->
            BalanceAccountCreationRowContent(balanceAccountCreation = type)
        is SolanaApprovalRequestType.DAppTransactionRequest ->
            DAppTransactionRowContent(dAppTransactionRequest = type)
        is SolanaApprovalRequestType.LoginApprovalRequest ->
            LoginApprovalRowContent(loginApproval = type)
        is SolanaApprovalRequestType.AddressBookUpdate -> 
            AddressBookUpdateRowContent(addressBookUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate -> 
            BalanceAccountAddressWhitelistUpdateRowContent(accountAddressWhitelistUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountNameUpdate -> 
            BalanceAccountNameUpdateRowContent(accountNameUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountPolicyUpdate -> 
            BalanceAccountPolicyUpdateRowContent(accountPolicyUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountSettingsUpdate -> 
            BalanceAccountSettingsUpdateRowContent(accountSettingsUpdate = type)
        is SolanaApprovalRequestType.DAppBookUpdate -> DAppBookUpdateRowContent()
        is SolanaApprovalRequestType.WalletConfigPolicyUpdate -> 
            WalletConfigPolicyUpdateRowContent(walletConfigPolicyUpdate = type)
        is SolanaApprovalRequestType.WrapConversionRequest -> 
            WrapConversionRequestRowContent(wrapConversionRequest = type)
        is SolanaApprovalRequestType.AcceptVaultInvitation ->
            AcceptVaultInvitationRowContent(acceptVaultInvitation = type)
        is SolanaApprovalRequestType.PasswordReset ->
            PasswordResetRowContent(passwordReset = type, email = approval.submitterEmail ?: "")
        else -> {
            val header = type.getHeader(LocalContext.current)
            Text(text = header)
        }
    }
}

@Composable
fun ApprovalDetailContent(approval: WalletApproval, type: SolanaApprovalRequestType) {
    when (type) {
        is SolanaApprovalRequestType.BalanceAccountCreation ->
            BalanceAccountDetailContent(balanceAccountCreation = type, approvalsReceived = approval.numberOfApprovalsReceived.toString())
        is SolanaApprovalRequestType.ConversionRequest ->
            ConversionDetailContent(conversionRequest = type)
        is SolanaApprovalRequestType.DAppTransactionRequest ->
            DAppTransactionDetailContent(dAppTransactionRequest = type)
        is SolanaApprovalRequestType.LoginApprovalRequest -> {
            LoginApprovalDetailContent(loginApproval = type)
        }
        is SolanaApprovalRequestType.SignersUpdate ->
            SignersUpdateDetailContent(signersUpdate = type)
        is SolanaApprovalRequestType.WithdrawalRequest ->
            WithdrawalRequestDetailContent(withdrawalRequest = type)
        is SolanaApprovalRequestType.AddressBookUpdate ->
            AddressBookUpdateDetailContent(addressBookUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate ->
            BalanceAccountAddressWhitelistUpdateDetailContent(addressWhitelistUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountNameUpdate ->
            BalanceAccountNameUpdateDetailContent(accountNameUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountPolicyUpdate ->
            BalanceAccountPolicyUpdateDetailContent(accountPolicyUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountSettingsUpdate ->
            BalanceAccountSettingsUpdateDetailContent(accountSettingsUpdate = type)
        is SolanaApprovalRequestType.DAppBookUpdate -> DAppBookUpdateDetailContent()
        is SolanaApprovalRequestType.WalletConfigPolicyUpdate ->
            WalletConfigPolicyUpdateDetailContent(walletConfigPolicyUpdate = type)
        is SolanaApprovalRequestType.WrapConversionRequest ->
            WrapConversionRequestDetailContent(wrapConversionRequest = type)
        is SolanaApprovalRequestType.AcceptVaultInvitation ->
            AcceptVaultInvitationDetailContent(acceptVaultInvitation = type)
        is SolanaApprovalRequestType.PasswordReset ->
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
                color = StrikeWhite,
                fontSize = 22.sp
            )
        },
        text = {
            Text(
                text = stringResource(R.string.default_error_message),
                color = StrikeWhite,
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