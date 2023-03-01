package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2

@Composable
fun OrgAdminPolicyUpdateDetailContent(
    orgAdminPolicyUpdate: ApprovalRequestDetailsV2.OrgAdminPolicyUpdate,
) {
    val header = orgAdminPolicyUpdate.getHeader(LocalContext.current)
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)

    val approverRowInfoData = generatePolicyRows(
        policy = orgAdminPolicyUpdate.approvalPolicy,
        chainFees = orgAdminPolicyUpdate.chainFees,
        context = LocalContext.current,
        isOrgPolicy = true
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
    Spacer(modifier = Modifier.height(8.dp))
}
