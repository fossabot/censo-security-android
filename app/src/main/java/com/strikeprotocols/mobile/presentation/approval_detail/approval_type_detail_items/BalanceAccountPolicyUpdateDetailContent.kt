package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.convertSecondsIntoReadableText
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getUITitle
import com.strikeprotocols.mobile.presentation.components.AccountChangeItem
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData

@Composable
fun BalanceAccountPolicyUpdateDetailContent(
    accountPolicyUpdate: SolanaApprovalRequestType.BalanceAccountPolicyUpdate
) {
    val header = accountPolicyUpdate.getHeader(LocalContext.current)
    val accountName = accountPolicyUpdate.accountInfo.name
    val accountType = accountPolicyUpdate.accountInfo.accountType.getUITitle(LocalContext.current)

    AccountChangeItem(
        header = header,
        title = accountName,
        subtitle = accountType,
        headerTopSpacing = 20,
        headerBottomSpacing = 36
    )
    Spacer(modifier = Modifier.height(24.dp))

    val approverRowInfoData = generateAccountPolicyUpdateRows(
        accountPolicyUpdate = accountPolicyUpdate, context = LocalContext.current)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (approvalRow in approverRowInfoData) {
            FactRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                factsData = approvalRow
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

}

fun generateAccountPolicyUpdateRows(accountPolicyUpdate: SolanaApprovalRequestType.BalanceAccountPolicyUpdate, context: Context) : List<FactsData>{
    val approverRowInfoData = mutableListOf<FactsData>()

    //region Approvers Row
    val approversList = mutableListOf<Pair<String, String>>()
    if (accountPolicyUpdate.approvalPolicy.approvers.isNotEmpty()) {
        for (approver in accountPolicyUpdate.approvalPolicy.approvers) {
            approversList.add(Pair(approver.value.name, approver.value.email))
        }
    } else {
        approversList.add(Pair(context.getString(R.string.no_wallet_approvers_text), ""))
    }

    val approverRow = FactsData(
        title = context.getString(R.string.wallet_approvers),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion

    //region Approvals Required Rows
    val approvalsRequiredRow = FactsData(
        title = context.getString(R.string.approvals_required),
        facts = listOf(Pair(accountPolicyUpdate.approvalPolicy.approvalsRequired.toInt().toString(), ""))
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion

    //region Approval Timeout Row
    val approvalTimeoutRow = FactsData(
        title = context.getString(R.string.approval_expiration),
        facts = listOf(
            Pair(convertSecondsIntoReadableText(accountPolicyUpdate.approvalPolicy.approvalTimeout.toInt(), context), "")
        )
    )
    approverRowInfoData.add(approvalTimeoutRow)
    //endregion

    return approverRowInfoData
}