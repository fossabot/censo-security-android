package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.convertSecondsIntoReadableText
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getUITitle
import com.strikeprotocols.mobile.presentation.components.AccountChangeItem
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData
import com.strikeprotocols.mobile.ui.theme.DividerGrey

@Composable
fun BalanceAccountPolicyUpdateDetailContent(
    accountPolicyUpdate: SolanaApprovalRequestType.BalanceAccountPolicyUpdate,
    approvalsReceived: Int
) {
    val header = accountPolicyUpdate.getHeader(LocalContext.current)

    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = accountPolicyUpdate.accountInfo.name)

    Spacer(modifier = Modifier.height(24.dp))

    val approverRowInfoData = generateAccountPolicyUpdateRows(
        approvalsReceived = approvalsReceived,
        accountPolicyUpdate = accountPolicyUpdate,
        context = LocalContext.current)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (approvalRow in approverRowInfoData) {
            FactRow(
                factsData = approvalRow
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

fun generateAccountPolicyUpdateRows(
    context: Context,
    accountPolicyUpdate: SolanaApprovalRequestType.BalanceAccountPolicyUpdate,
    approvalsReceived: Int
) : List<FactsData>{
    val approverRowInfoData = mutableListOf<FactsData>()
    //region Approvals Row
    val approvalsInfoRow = FactsData(
        title = context.getString(R.string.approvals).uppercase(),
        facts = listOf(
            Pair(context.getString(R.string.approvals_required), "$approvalsReceived ${context.getString(R.string.of)} ${accountPolicyUpdate.approvalPolicy.approvalsRequired.toInt()}"),
            Pair(context. getString(R.string.approval_expiration), convertSecondsIntoReadableText(accountPolicyUpdate.approvalPolicy.approvalTimeout.toInt(), context))
        )
    )
    approverRowInfoData.add(approvalsInfoRow)
    //endregion

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
        title = context.getString(R.string.approvers).uppercase(),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion

    return approverRowInfoData
}