package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.common.convertSecondsIntoReadableText
import com.censocustody.android.common.toWalletName
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.retrieveSlotRowData
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.R

@Composable
fun BalanceAccountPolicyUpdateDetailContent(
    accountPolicyUpdate: ApprovalRequestDetails.BalanceAccountPolicyUpdate
) {
    val header = accountPolicyUpdate.getHeader(LocalContext.current)

    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = accountPolicyUpdate.accountInfo.name.toWalletName(), fontSize = 20.sp)

    Spacer(modifier = Modifier.height(24.dp))

    val approverRowInfoData = generateAccountPolicyUpdateRows(
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
    accountPolicyUpdate: ApprovalRequestDetails.BalanceAccountPolicyUpdate,
) : List<FactsData>{
    val approverRowInfoData = mutableListOf<FactsData>()
    //region Approvals Row
    val approvalsInfoRow = FactsData(
        title = context.getString(R.string.approvals).uppercase(),
        facts = listOf(
            Pair(context.getString(R.string.approvals_required), "${accountPolicyUpdate.approvalPolicy.approvalsRequired}"),
            Pair(context. getString(R.string.approval_expiration), convertSecondsIntoReadableText(accountPolicyUpdate.approvalPolicy.approvalTimeout.toInt(), context))
        )
    )
    approverRowInfoData.add(approvalsInfoRow)
    //endregion

    //region Approvers Row
    val approversList = accountPolicyUpdate.approvalPolicy.approvers.retrieveSlotRowData()
    if (approversList.isEmpty()) {
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