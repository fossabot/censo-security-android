package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
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
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData

@Composable
fun WalletConfigPolicyUpdateDetailContent(
    walletConfigPolicyUpdate: SolanaApprovalRequestType.WalletConfigPolicyUpdate
) {
    val header = walletConfigPolicyUpdate.getHeader(LocalContext.current)
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)

    val approverRowInfoData = generateWalletConfigPolicyRows(
        walletConfigPolicyUpdate = walletConfigPolicyUpdate, context = LocalContext.current)

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

fun generateWalletConfigPolicyRows(walletConfigPolicyUpdate: SolanaApprovalRequestType.WalletConfigPolicyUpdate, context: Context) : List<FactsData>{
    val approverRowInfoData = mutableListOf<FactsData>()

    //region Approvers Row
    val approversList = mutableListOf<Pair<String, String>>()
    if (walletConfigPolicyUpdate.approvalPolicy.approvers.isNotEmpty()) {
        for (approver in walletConfigPolicyUpdate.approvalPolicy.approvers) {
            approversList.add(Pair(approver.value.name, approver.value.email))
        }
    } else {
        approversList.add(Pair(context.getString(R.string.no_approvers_text), ""))
    }

    val approverRow = FactsData(
        title = context.getString(R.string.vault_approvers),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion

    //region Approvals Required Rows
    val approvalsRequiredRow = FactsData(
        title = context.getString(R.string.approvals_required),
        facts = listOf(Pair(walletConfigPolicyUpdate.approvalPolicy.approvalsRequired.toInt().toString(), ""))
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion

    //region Approval Timeout Row
    val approvalTimeoutRow = FactsData(
        title = context.getString(R.string.approval_expiration),
        facts = listOf(
            Pair(convertSecondsIntoReadableText(walletConfigPolicyUpdate.approvalPolicy.approvalTimeout.toInt(), context), "")
        )
    )
    approverRowInfoData.add(approvalTimeoutRow)
    //endregion

    return approverRowInfoData
}