package com.censocustody.mobile.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.censocustody.mobile.R
import com.censocustody.mobile.common.convertSecondsIntoReadableText
import com.censocustody.mobile.data.models.approval.ApprovalRequestDetails
import com.censocustody.mobile.presentation.approvals.ApprovalContentHeader
import com.censocustody.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.mobile.presentation.approvals.approval_type_row_items.retrieveSlotRowData
import com.censocustody.mobile.presentation.components.FactRow
import com.censocustody.mobile.presentation.components.FactsData

@Composable
fun BalanceAccountDetailContent(
    walletCreation: ApprovalRequestDetails.WalletCreation,
    approvalsReceived: String
) {
    val header = walletCreation.getHeader(LocalContext.current)
    val accountName = walletCreation.accountInfo.name

    val approverRowInfoData = generateBalanceAccountDetailRows(
        walletCreation = walletCreation, approvalsReceived = approvalsReceived, context = LocalContext.current
    )

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
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
    walletCreation: ApprovalRequestDetails.WalletCreation,
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
                "$approvalsReceived ${context.getString(R.string.of)} ${walletCreation.approvalPolicy.approvalsRequired.toInt()}"
            ),
            Pair(
                context.getString(R.string.approval_expiration),
                convertSecondsIntoReadableText(
                    walletCreation.approvalPolicy.approvalTimeout.toInt(),
                    context
                )
            )
        )
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion


    //region Approvers Row
    val approversList = walletCreation.approvalPolicy.approvers.retrieveSlotRowData()
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