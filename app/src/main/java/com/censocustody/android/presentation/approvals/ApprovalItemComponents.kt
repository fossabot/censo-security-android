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
            WalletCreationRowContent(
                header = type.getHeader(LocalContext.current),
                accountName = type.walletCreationAccountName()
            )
        }
        //BalanceAccountNameUpdate
        is ApprovalRequestDetailsV2.EthereumWalletNameUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletNameUpdate -> {
            BalanceAccountNameUpdateRowContent(
                header = type.getHeader(LocalContext.current),
                oldName = type.walletNameOldAccountName(),
                newName = type.walletNameNewAccountName()
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
            BalanceAccountAddressWhitelistUpdateRowContent(
                header =  type.getHeader(LocalContext.current),
                accountName = type.whitelistUpdateName()
            )
        }

        //BalanceAccountSettingsUpdate
        is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
            BalanceAccountSettingsUpdateRowContent(
                header = type.getHeader(LocalContext.current),
                name = type.walletSettingsUpdateName()
            )
        }

        //WithdrawalRequest
        is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest,
        is ApprovalRequestDetailsV2.EthereumWithdrawalRequest,
        is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> {
            val fromAndToAccount = type.withdrawalRequestFromAndToAccount()
            WithdrawalRequestRowContent(
                header = type.getHeader(LocalContext.current),
                subtitle = type.withdrawalRequestSubtitle(LocalContext.current),
                fromAccount = fromAndToAccount.first,
                toAccount = fromAndToAccount.second
            )
        }

        //BalanceAccountPolicyUpdate
        is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate,
        is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
            BalanceAccountPolicyUpdateRowContent(
                header = type.getHeader(LocalContext.current),
                name = type.transferPolicyUpdateName()
            )
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
                vaultName = type.vaultName
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
        //WalletConfigPolicyUpdate
        is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
            WalletConfigPolicyUpdateDetailContent(vaultPolicyUpdate = type)
        }
        //Wallet Creation
        is ApprovalRequestDetailsV2.BitcoinWalletCreation,
        is ApprovalRequestDetailsV2.EthereumWalletCreation,
        is ApprovalRequestDetailsV2.PolygonWalletCreation -> {
            val header = type.getHeader(LocalContext.current)
            val approvalPolicy = type.walletApprovalPolicy()

            approvalPolicy?.let {
                BalanceAccountDetailContent(
                    WalletCreationUIData(
                        header = header,
                        name =  type.walletCreationAccountName(),
                        approvalsReceived = approval.numberOfApprovalsReceived.toString(),
                        walletApprovalPolicy = it
                    )
                )
            } ?: run {
                Text(text = header, color = CensoWhite)
            }
        }
        //BalanceAccountNameUpdate
        is ApprovalRequestDetailsV2.EthereumWalletNameUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletNameUpdate -> {
            BalanceAccountNameUpdateDetailContent(
                header = type.getHeader(LocalContext.current),
                oldName = type.walletNameOldAccountName(),
                newName = type.walletNameNewAccountName()
            )
        }

        //CreateAddressBookEntry
        is ApprovalRequestDetailsV2.CreateAddressBookEntry -> {
            CreateOrDeleteAddressBookEntryDetailContent(
                header = type.getHeader(LocalContext.current),
                chain = type.chain,
                entryName = type.name,
                entryAddress = type.address
            )
        }

        //DeleteAddressBookEntry
        is ApprovalRequestDetailsV2.DeleteAddressBookEntry -> {
            CreateOrDeleteAddressBookEntryDetailContent(
                header = type.getHeader(LocalContext.current),
                chain = type.chain,
                entryName = type.name,
                entryAddress = type.address
            )
        }

        //BalanceAccountAddressWhitelistUpdate
        is ApprovalRequestDetailsV2.EthereumWalletWhitelistUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletWhitelistUpdate -> {
            val whitelistUpdateUI = type.whiteListUpdateUI(LocalContext.current)

            whitelistUpdateUI?.let {
                BalanceAccountAddressWhitelistUpdateDetailContent(whitelistUpdate = it)
            } ?: run {
                Text(type.getHeader(LocalContext.current), color = CensoWhite)
            }
        }

        //BalanceAccountSettingsUpdate
        is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
            BalanceAccountSettingsUpdateDetailContent(
                header = type.getHeader(LocalContext.current),
                name = type.walletSettingsUpdateName()
            )
        }

        is ApprovalRequestDetailsV2.BitcoinWithdrawalRequest,
        is ApprovalRequestDetailsV2.EthereumWithdrawalRequest,
        is ApprovalRequestDetailsV2.PolygonWithdrawalRequest -> {
            val withdrawalRequestUI = type.withdrawalRequestUIData(LocalContext.current)

            withdrawalRequestUI?.let {
                WithdrawalRequestDetailContent(withdrawalRequestUI)
            } ?: run {
                val header = type.getHeader(LocalContext.current)
                Text(text = header, color = CensoWhite)
            }
        }

        //BalanceAccountPolicyUpdate
        is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate,
        is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
            val policyUpdateUIData = type.policyUpdateUI(LocalContext.current)
            policyUpdateUIData?.let {
                BalanceAccountPolicyUpdateDetailContent(policyUpdateUIData = it)
            } ?: run {
                val header = type.getHeader(LocalContext.current)
                Text(text = header, color = CensoWhite)
            }
        }

        //LoginApproval
        is ApprovalRequestDetailsV2.Login -> {
            LoginApprovalDetailContent(
                header = type.getHeader(LocalContext.current),
                email = type.email,
                name = type.name
            )
        }

        //PasswordReset
        is ApprovalRequestDetailsV2.PasswordReset -> {
            PasswordResetDetailContent(type.getHeader(LocalContext.current))
        }

        //AcceptVaultInvitation
        is ApprovalRequestDetailsV2.VaultInvitation -> {
            AcceptVaultInvitationDetailContent(type.getHeader(LocalContext.current))
        }

        //Unknown
        ApprovalRequestDetailsV2.UnknownApprovalType -> {
            val header = type.getHeader(LocalContext.current)
            Text(text = header, color = CensoWhite)
        }
    }
}