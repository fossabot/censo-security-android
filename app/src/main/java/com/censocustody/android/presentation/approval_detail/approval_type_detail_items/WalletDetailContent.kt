package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.censocustody.android.common.convertSecondsIntoReadableText
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.R
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.approval_type_row_items.retrieveSlotSignerRowData
import com.censocustody.android.presentation.components.RowData

@Composable
fun WalletDetailContent(walletCreationUIData: WalletCreationUIData) {

    val approverRowInfoData = generateWalletDetailRows(
        walletCreationUIData = walletCreationUIData, context = LocalContext.current
    )

    ApprovalContentHeader(header = walletCreationUIData.header, topSpacing = 24, bottomSpacing = 8)
    Spacer(modifier = Modifier.height(24.dp))
    val factsData = FactsData(
        facts = listOf(
            RowData(title = stringResource(R.string.wallet_name_title), value = walletCreationUIData.name),
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
    walletCreationUIData.fee?.let { feeAmount ->
        getFeeEstimate(LocalContext.current, feeAmount)?.let { factsData ->
            Spacer(modifier = Modifier.height(20.dp))
            FactRow(
                factsData = factsData
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))
}

fun generateWalletDetailRows(
    walletCreationUIData: WalletCreationUIData,
    context: Context
): List<FactsData> {
    val approverRowInfoData = mutableListOf<FactsData>()

    //region Approvals Rows
    val approvalsRequiredRow = FactsData(
        title = context.getString(R.string.approvals),
        facts = listOf(
            RowData(
                title = context.getString(R.string.approvals_required_title),
                value = "${walletCreationUIData.approvalsReceived} ${context.getString(R.string.of)} ${walletCreationUIData.walletApprovalPolicy.approvalsRequired}",
            ),
            RowData(
                title = context.getString(R.string.approval_expiration),
                value = convertSecondsIntoReadableText(
                    walletCreationUIData.walletApprovalPolicy.approvalTimeout.toInt(),
                    context
                ),
            )
        )
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion


    //region Approvers Row
    val approversList = walletCreationUIData.walletApprovalPolicy.approvers.retrieveSlotSignerRowData()
    if (approversList.isEmpty()) {
        approversList.add(
            RowData(title = context.getString(R.string.no_approvers_text), value = ""))
    }

    val approverRow = FactsData(
        title = context.getString(R.string.approvers),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion


    return approverRowInfoData
}

data class WalletCreationUIData(
    val header: String, val name: String, val approvalsReceived: String,
    val walletApprovalPolicy: ApprovalRequestDetailsV2.WalletApprovalPolicy,
    val fee: ApprovalRequestDetailsV2.Amount?
)