package com.censocustody.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.censocustody.mobile.data.models.approval.ApprovalRequestDetails
import com.censocustody.mobile.presentation.approvals.ApprovalRowContentHeader

@Composable
fun WalletConfigPolicyUpdateRowContent(
    walletConfigPolicyUpdate: ApprovalRequestDetails.WalletConfigPolicyUpdate
) {
    val header = walletConfigPolicyUpdate.getHeader(LocalContext.current)
    ApprovalRowContentHeader(header = header, topSpacing = 16)
}

