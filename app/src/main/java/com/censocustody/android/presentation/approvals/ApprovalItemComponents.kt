package com.censocustody.android.presentation.approvals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.common.convertSecondsIntoCountdownText
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
            .background(Color.White)
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
                color = DarkGreyText
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
    Column(modifier = Modifier.background(Color.White)) {
        Divider(color = BorderGrey, modifier = Modifier.height(1.0.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalRippleTheme provides CustomRippleTheme) {
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp, bottom = 4.dp),
                    onClick = onApproveClicked
                ) {
                    Text(
                        positiveButtonText,
                        color = ApprovalGreen,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )
                }
            }
            Divider(
                color = BorderGrey,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.0.dp)
            )
            CompositionLocalProvider(LocalRippleTheme provides CustomRippleTheme) {
                TextButton(
                    modifier = Modifier
                        .weight(1f),
                    onClick = onMoreInfoClicked
                ) {
                    Text(
                        stringResource(R.string.more_info),
                        color = TextBlack,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )
                }
            }
        }
    }
}

object CustomRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = ButtonRed

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleAlpha(0.0f, 0.0f, 0.0f, 0.0f)
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
            color = TextBlack,
            fontWeight = FontWeight.W400,
        )
    }
}

@Composable
fun ApprovalRowContent(
    type: ApprovalRequestDetailsV2,
    approval: ApprovalRequestV2,
) {

    when (type) {
        //EnableDevice
        is ApprovalRequestDetailsV2.EnableDevice -> {
            EnableOrDisableDeviceRowContent(header = type.getHeader(LocalContext.current), type.email)
        }
        is ApprovalRequestDetailsV2.DisableDevice -> {
            EnableOrDisableDeviceRowContent(header = type.getHeader(LocalContext.current), type.email)
        }
        //OrgAdminPolicyUpdate
        is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate -> {
            OrgAdminPolicyUpdateRowContent(header = type.getHeader(LocalContext.current))
        }
        //VaultPolicyUpdate
        is ApprovalRequestDetailsV2.VaultCreation -> {
            VaultCreationRowContent(header = type.getHeader(LocalContext.current), type.name)
        }
        //VaultPolicyUpdate
        is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
            VaultConfigPolicyUpdateRowContent(header = type.getHeader(LocalContext.current), type.vaultName)
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
            NameUpdateRowContent(
                header = type.getHeader(LocalContext.current),
                oldName = type.walletNameOldAccountName(),
                newName = type.walletNameNewAccountName(),
                renameType = RenameType.Wallet
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
            WalletAddressWhitelistUpdateRowContent(
                header = type.getHeader(LocalContext.current),
                accountName = type.whitelistUpdateName()
            )
        }

        //BalanceAccountSettingsUpdate
        is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
            WalletSettingsUpdateRowContent(
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
            WalletPolicyUpdateRowContent(
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

        is ApprovalRequestDetailsV2.VaultNameUpdate -> {
            NameUpdateRowContent(
                header = type.getHeader(LocalContext.current),
                oldName = type.oldName,
                newName = type.newName,
                renameType = RenameType.Vault
            )
        }

        is ApprovalRequestDetailsV2.OrgNameUpdate -> {
            NameUpdateRowContent(
                header = type.getHeader(LocalContext.current),
                oldName = type.oldName,
                newName = type.newName,
                renameType = RenameType.Org
            )
        }

        is ApprovalRequestDetailsV2.VaultUserRolesUpdate -> {
            VaultUserRolesUpdateRowContent(header = type.getHeader(LocalContext.current), type.vaultName)
        }

        is ApprovalRequestDetailsV2.SuspendUser -> {
            SuspendUserRowContent(header = type.getHeader(LocalContext.current), type.name)
        }

        is ApprovalRequestDetailsV2.RestoreUser -> {
            RestoreUserRowContent(header = type.getHeader(LocalContext.current), type.name)
        }
        is ApprovalRequestDetailsV2.EnableRecoveryContract -> {
            EnableRecoveryContractRowContent(header = type.getHeader(LocalContext.current))
        }

        is ApprovalRequestDetailsV2.EthereumDAppRequest,
        is ApprovalRequestDetailsV2.PolygonDAppRequest -> {
            when (type.dAppParams()) {
                is ApprovalRequestDetailsV2.DAppParams.EthSendTransaction -> DAppEthSendTransactionContent(
                    header = type.getHeader(LocalContext.current),
                    subtitle = type.dAppInfo()!!.name,
                )
                else -> throw RuntimeException("not implemented")
            }
        }

        //Unknown
        is ApprovalRequestDetailsV2.UnknownApprovalType -> {
            Text(text = type.getHeader(LocalContext.current), color = TextBlack)
        }
    }
}

@Composable
fun ApprovalDetailContent(approval: ApprovalRequestV2, type: ApprovalRequestDetailsV2) {

    when (type) {
        //EnableDevice
        is ApprovalRequestDetailsV2.EnableDevice -> {
            EnableOrDisableDeviceDetailContent(type.getHeader(LocalContext.current), userDevice = type.toUserDeviceUI())
        }
        is ApprovalRequestDetailsV2.DisableDevice -> {
            EnableOrDisableDeviceDetailContent(type.getHeader(LocalContext.current), userDevice = type.toUserDeviceUI())
        }
        //OrgAdminPolicyUpdate
        is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate -> {
            OrgAdminPolicyUpdateDetailContent(orgAdminPolicyUpdate = type)
        }
        //VaultPolicyUpdate
        is ApprovalRequestDetailsV2.VaultCreation -> {
            VaultCreationDetailContent(vaultCreation = type)
        }
        //VaultConfigPolicyUpdate
        is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
            VaultConfigPolicyUpdateDetailContent(vaultPolicyUpdate = type)
        }
        //Wallet Creation
        is ApprovalRequestDetailsV2.BitcoinWalletCreation,
        is ApprovalRequestDetailsV2.EthereumWalletCreation,
        is ApprovalRequestDetailsV2.PolygonWalletCreation -> {
            val header = type.getHeader(LocalContext.current)
            val approvalPolicy = type.walletApprovalPolicy()

            approvalPolicy?.let {
                WalletDetailContent(
                    WalletCreationUIData(
                        header = header,
                        name = type.walletCreationAccountName(),
                        approvalsReceived = approval.numberOfApprovalsReceived.toString(),
                        walletApprovalPolicy = it,
                        fee = type.fee()
                    )
                )
            } ?: run {
                Text(text = header, color = TextBlack)
            }
        }
        //BalanceAccountNameUpdate
        is ApprovalRequestDetailsV2.EthereumWalletNameUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletNameUpdate -> {
            NameUpdateDetailContent(
                header = type.getHeader(LocalContext.current),
                oldName = type.walletNameOldAccountName(),
                newName = type.walletNameNewAccountName(),
                renameType = RenameType.Wallet
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
                WalletAddressWhitelistUpdateDetailContent(whitelistUpdate = it)
            } ?: run {
                Text(type.getHeader(LocalContext.current), color = TextBlack)
            }
        }

        //BalanceAccountSettingsUpdate
        is ApprovalRequestDetailsV2.EthereumWalletSettingsUpdate,
        is ApprovalRequestDetailsV2.PolygonWalletSettingsUpdate -> {
            WalletSettingsUpdateDetailContent(
                header = type.getHeader(LocalContext.current),
                name = type.walletSettingsUpdateName(),
                fee = type.fee()!!
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
                Text(text = header, color = TextBlack)
            }
        }

        //BalanceAccountPolicyUpdate
        is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate,
        is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
            val policyUpdateUIData = type.policyUpdateUI(LocalContext.current)
            policyUpdateUIData?.let {
                WalletPolicyUpdateDetailContent(policyUpdateUIData = it)
            } ?: run {
                Text(
                    text = type.getHeader(LocalContext.current),
                    color = TextBlack
                )
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

        is ApprovalRequestDetailsV2.VaultNameUpdate -> {
            NameUpdateDetailContent(
                header = type.getHeader(LocalContext.current),
                oldName = type.oldName,
                newName = type.newName,
                renameType = RenameType.Vault
            )
        }

        is ApprovalRequestDetailsV2.OrgNameUpdate -> {
            NameUpdateDetailContent(
                header = type.getHeader(LocalContext.current),
                oldName = type.oldName,
                newName = type.newName,
                renameType = RenameType.Org
            )
        }

        is ApprovalRequestDetailsV2.VaultUserRolesUpdate -> {
            VaultUserRolesUpdateDetailContent(update = type)
        }

        is ApprovalRequestDetailsV2.SuspendUser -> {
            SuspendUserDetailContent(details = type)
        }

        is ApprovalRequestDetailsV2.RestoreUser -> {
            RestoreUserDetailContent(details = type)
        }

        is ApprovalRequestDetailsV2.EnableRecoveryContract -> {
            EnableRecoveryContractDetailContent(details = type)
        }

        is ApprovalRequestDetailsV2.EthereumDAppRequest,
        is ApprovalRequestDetailsV2.PolygonDAppRequest -> {
            when (type.dAppParams()) {
                is ApprovalRequestDetailsV2.DAppParams.EthSendTransaction -> DAppEthSendTransactionDetailContent(
                    header = type.getHeader(LocalContext.current),
                    fromAccount = type.dAppFromAccount(),
                    fee = type.fee()!!,
                    dAppInfo = type.dAppInfo()!!,
                    simulationResults = type.dAppSimulationResults()
                )
                else -> throw RuntimeException("not implemented")
            }
        }

        //Unknown
        is ApprovalRequestDetailsV2.UnknownApprovalType -> {
            val header = type.getHeader(LocalContext.current)
            Text(text = header, color = TextBlack)
        }
    }
}