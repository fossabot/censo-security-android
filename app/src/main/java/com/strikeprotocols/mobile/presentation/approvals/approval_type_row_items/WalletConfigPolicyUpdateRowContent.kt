package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalDispositionsRequired
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader

@Composable
fun WalletConfigPolicyUpdateRowContent(
    walletConfigPolicyUpdate: SolanaApprovalRequestType.WalletConfigPolicyUpdate,
    approvalsNeeded: Int
) {
    val header = walletConfigPolicyUpdate.getHeader(LocalContext.current)
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 6)
    ApprovalDispositionsRequired(approvalsNeeded = approvalsNeeded)
}

