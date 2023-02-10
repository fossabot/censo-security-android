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
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.R
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.approval_type_row_items.retrieveSlotSignerRowData
import com.censocustody.android.presentation.components.RowData

@Composable
fun WalletPolicyUpdateDetailContent(policyUpdateUIData: PolicyUpdateUIData
) {
    ApprovalContentHeader(header = policyUpdateUIData.header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = policyUpdateUIData.name.toWalletName(LocalContext.current), fontSize = 20.sp)

    Spacer(modifier = Modifier.height(24.dp))

    val approverRowInfoData = generateAccountPolicyUpdateRows(
        approvalsRequired = policyUpdateUIData.approvalsRequired,
        approvalTimeout = policyUpdateUIData.approvalTimeout,
        approvers = policyUpdateUIData.approvers,
        context = LocalContext.current,
        fee = policyUpdateUIData.fee
    )

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
    approvalsRequired: Int,
    approvalTimeout: Long,
    approvers: List<ApprovalRequestDetailsV2.Signer>,
    fee: ApprovalRequestDetailsV2.Amount
) : List<FactsData>{
    val approverRowInfoData = mutableListOf<FactsData>()
    //region Approvals Row
    val approvalsInfoRow = FactsData(
        title = context.getString(R.string.approvals).uppercase(),
        facts = listOf(
            RowData(
                title = context.getString(R.string.approvals_required),
                value = "$approvalsRequired"),
            RowData(
                title = context. getString(R.string.approval_expiration),
                value = convertSecondsIntoReadableText(approvalTimeout.toInt(), context))
        )
    )
    approverRowInfoData.add(approvalsInfoRow)
    //endregion

    //region Approvers Row
    val approversList = approvers.retrieveSlotSignerRowData()
    if (approversList.isEmpty()) {
        approversList.add(
            RowData(
                title = context.getString(R.string.no_wallet_approvers_text),
                value = ""
            )
        )
    }

    val approverRow = FactsData(
        title = context.getString(R.string.approvers).uppercase(),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion

    getFeeEstimate(context, fee)?.let { factsData ->
        approverRowInfoData.add(factsData)
    }

    return approverRowInfoData
}

data class PolicyUpdateUIData(
    val header: String, val name: String,
    val approvalsRequired: Int, val approvalTimeout: Long,
    val approvers: List<ApprovalRequestDetailsV2.Signer>,
    val fee: ApprovalRequestDetailsV2.Amount
)