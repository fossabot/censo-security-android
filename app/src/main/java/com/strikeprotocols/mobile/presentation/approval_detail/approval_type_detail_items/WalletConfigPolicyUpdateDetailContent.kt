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
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.retrieveSlotRowData
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
                factsData = approvalRow
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

fun generateWalletConfigPolicyRows(
    walletConfigPolicyUpdate: SolanaApprovalRequestType.WalletConfigPolicyUpdate,
    context: Context
): List<FactsData> {
    val approvalsRequired = walletConfigPolicyUpdate.approvalPolicy.approvalsRequired.toInt().toString()

    val approverRowInfoData = mutableListOf<FactsData>()

    //region Approvals Row
    val approvalsRequiredRow = FactsData(
        title = context.getString(R.string.approvals),
        facts = listOf(
            Pair(context.getString(R.string.approvals_required), approvalsRequired),
            Pair(context.getString(R.string.approval_expiration), convertSecondsIntoReadableText(walletConfigPolicyUpdate.approvalPolicy.approvalTimeout.toInt(), context))
        )
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion

    //region Approvers Row
    val approversList = walletConfigPolicyUpdate.approvalPolicy.approvers.retrieveSlotRowData()
    if (approversList.isEmpty()) {
        approversList.add(Pair(context.getString(R.string.no_approvers_text), ""))
    }

    val approverRow = FactsData(
        title = context.getString(R.string.vault_approvers),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion

    return approverRowInfoData
}