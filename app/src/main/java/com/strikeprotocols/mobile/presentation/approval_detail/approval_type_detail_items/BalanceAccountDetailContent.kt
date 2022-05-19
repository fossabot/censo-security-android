package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.convertSecondsIntoReadableText
import com.strikeprotocols.mobile.data.models.approval.BooleanSetting
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getUITitle
import com.strikeprotocols.mobile.presentation.components.AccountChangeItem
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData

@Composable
fun BalanceAccountDetailContent(
    balanceAccountCreation: SolanaApprovalRequestType.BalanceAccountCreation,
    approvalsNeeded: Int
) {
    val mainTitle = stringResource(R.string.new_balance_account_details_title)
    val accountName = balanceAccountCreation.accountInfo.name
    val accountType =
        balanceAccountCreation.accountInfo.accountType.getUITitle(LocalContext.current)

    val approverRowInfoData = generateBalanceAccountDetailRows(
        balanceAccountCreation = balanceAccountCreation, context = LocalContext.current)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AccountChangeItem(mainTitle = mainTitle, title = accountName, subtitle = accountType)
        Spacer(modifier = Modifier.height(16.dp))
        ApprovalDispositionsRequired(approvalsNeeded = approvalsNeeded)
        Spacer(modifier = Modifier.height(28.dp))

        for (approvalRow in approverRowInfoData) {
            FactRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                factsData = approvalRow
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

fun generateBalanceAccountDetailRows(balanceAccountCreation: SolanaApprovalRequestType.BalanceAccountCreation, context: Context) : List<FactsData>{
    val approverRowInfoData = mutableListOf<FactsData>()

    //region Approvers Row
    val approversList = mutableListOf<Pair<String, String>>()
    if (balanceAccountCreation.approvalPolicy.approvers.isNotEmpty()) {
        for (approver in balanceAccountCreation.approvalPolicy.approvers) {
            approversList.add(Pair(approver.value.name, approver.value.email))
        }
    } else {
        approversList.add(Pair(context.getString(R.string.no_approvers_text), ""))
    }

    val approverRow = FactsData(
        title = context.getString(R.string.outbound_transfers_title),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion

    //region Approvals Required Rows
    val approvalsRequiredRow = FactsData(
        title = context.getString(R.string.approval_outbound_title),
        facts = listOf(Pair(balanceAccountCreation.approvalPolicy.approvalsRequired.toInt().toString(), ""))
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion

    //region Approval Timeout Row
    val approvalTimeoutRow = FactsData(
        title = context.getString(R.string.approval_timeout),
        facts = listOf(
            Pair(convertSecondsIntoReadableText(balanceAccountCreation.approvalPolicy.approvalTimeout.toInt(), context), "")
        )
    )
    approverRowInfoData.add(approvalTimeoutRow)
    //endregion

    //region Whitelist Enabled Row
    val whiteListTextValue =
        if (balanceAccountCreation.whitelistEnabled == BooleanSetting.On) {
            context.getString(R.string.yes)
        } else {
            context.getString(R.string.no)
        }
    val whitelistingEnabledRow = FactsData(
        title = context.getString(R.string.whitelisting_enabled),
        facts = listOf(Pair(whiteListTextValue, ""))
    )
    approverRowInfoData.add(whitelistingEnabledRow)
    //endregion

    //region Supports DApp Row
    val supportDAppTextValue =
        if (balanceAccountCreation.dappsEnabled == BooleanSetting.On) {
            context.getString(R.string.yes)
        } else {
            context.getString(R.string.no)
        }
    val supportsDAppsRow = FactsData(
        title = context.getString(R.string.supports_dapps),
        facts = listOf(Pair(supportDAppTextValue, ""))
    )
    approverRowInfoData.add(supportsDAppsRow)
    //endregion

    return approverRowInfoData
}