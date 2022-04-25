package com.strikeprotocols.mobile.presentation.approvals.approval_type_items

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType

@Composable
fun LoginApprovalRowContent(loginApproval: SolanaApprovalRequestType.LoginApprovalRequest) {
    Text(text = loginApproval.type, color = Color.White)
}