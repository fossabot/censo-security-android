package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.censocustody.android.common.convertSecondsIntoReadableText
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.retrieveSlotRowData
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.R
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.components.RowData

@Composable
fun WalletConfigPolicyUpdateDetailContent(
    vaultPolicyUpdate: ApprovalRequestDetailsV2.VaultPolicyUpdate,
) {
    val header = vaultPolicyUpdate.getHeader(LocalContext.current)
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)

    val approverRowInfoData = generateWalletConfigPolicyRows(
        vaultPolicyUpdate = vaultPolicyUpdate, context = LocalContext.current)

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
    vaultPolicyUpdate: ApprovalRequestDetailsV2.VaultPolicyUpdate,
    context: Context
): List<FactsData> {
    val approvalsRequired = vaultPolicyUpdate.approvalPolicy.approvalsRequired.toInt().toString()

    val approverRowInfoData = mutableListOf<FactsData>()

    //region Approvals Row
    val approvalsRequiredRow = FactsData(
        title = context.getString(R.string.approvals),
        facts = listOf(
            RowData(
                title = context.getString(R.string.approvals_required),
                value = approvalsRequired,
            ),
            RowData(
                title = context.getString(R.string.approval_expiration),
                value = convertSecondsIntoReadableText(vaultPolicyUpdate.approvalPolicy.approvalTimeout.toInt(), context),
            )
        )
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion

    //region Approvers Row
    val approversList = vaultPolicyUpdate.approvalPolicy.approvers.retrieveSlotRowData()
    if (approversList.isEmpty()) {
        approversList.add(
            RowData(
                title = context.getString(R.string.no_approvers_text),
                value = ""
            )
        )
    }

    val approverRow = FactsData(
        title = context.getString(R.string.vault_approvers),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion

    return approverRowInfoData
}