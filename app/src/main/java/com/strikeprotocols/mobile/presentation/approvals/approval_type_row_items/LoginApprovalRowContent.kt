package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader

@Composable
fun LoginApprovalRowContent(loginApproval: SolanaApprovalRequestType.LoginApprovalRequest) {
    val header = loginApproval.getHeader(LocalContext.current)
    ApprovalRowContentHeader(header = header)
}