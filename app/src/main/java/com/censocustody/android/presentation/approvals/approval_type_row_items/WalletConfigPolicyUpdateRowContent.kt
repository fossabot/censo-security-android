package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.presentation.approvals.ApprovalRowContentHeader

@Composable
fun WalletConfigPolicyUpdateRowContent(
    walletConfigPolicyUpdate: ApprovalRequestDetails.WalletConfigPolicyUpdate
) {
    val header = walletConfigPolicyUpdate.getHeader(LocalContext.current)
    ApprovalRowContentHeader(header = header, topSpacing = 16)
}

