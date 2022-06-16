package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
fun BalanceAccountDetailContent(
    balanceAccountCreation: SolanaApprovalRequestType.BalanceAccountCreation,
    approvalsReceived: String
) {
    val header = balanceAccountCreation.getHeader(LocalContext.current)
    val accountName = balanceAccountCreation.accountInfo.name

    val approverRowInfoData = generateBalanceAccountDetailRows(
        balanceAccountCreation = balanceAccountCreation, approvalsReceived = approvalsReceived, context = LocalContext.current
    )

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    ApprovalSubtitle(text = accountName)
    Spacer(modifier = Modifier.height(24.dp))
    val factsData = FactsData(
        facts = listOf(
            Pair(stringResource(R.string.wallet_name_title), accountName),
        )
    )
    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(20.dp))

    for ((index, approvalRow) in approverRowInfoData.withIndex()) {
        FactRow(
            factsData = approvalRow
        )
        if (index != approverRowInfoData.size - 1) {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    Spacer(modifier = Modifier.height(28.dp))
}

fun generateBalanceAccountDetailRows(
    balanceAccountCreation: SolanaApprovalRequestType.BalanceAccountCreation,
    approvalsReceived: String,
    context: Context
): List<FactsData> {
    val approverRowInfoData = mutableListOf<FactsData>()

    //region Approvals Rows
    val approvalsRequiredRow = FactsData(
        title = context.getString(R.string.approvals),
        facts = listOf(
            Pair(
                context.getString(R.string.approvals_required_title),
                "$approvalsReceived ${context.getString(R.string.of)} ${balanceAccountCreation.approvalPolicy.approvalsRequired.toInt()}"
            ),
            Pair(
                context.getString(R.string.approval_expiration),
                convertSecondsIntoReadableText(
                    balanceAccountCreation.approvalPolicy.approvalTimeout.toInt(),
                    context
                )
            )
        )
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion


    //region Approvers Row
    val approversList = balanceAccountCreation.approvalPolicy.approvers.retrieveSlotRowData()
    if (approversList.isEmpty()) {
        approversList.add(Pair(context.getString(R.string.no_approvers_text), ""))
    }

    val approverRow = FactsData(
        title = context.getString(R.string.approvers),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion


    return approverRowInfoData
}