package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequestDetails
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader

@Composable
fun PasswordResetDetailContent(
    passwordReset: ApprovalRequestDetails.PasswordReset
) {
    val header = passwordReset.getHeader(LocalContext.current)
    ApprovalRowContentHeader(header = header, bottomSpacing = 36)
    Spacer(modifier = Modifier.height(24.dp))
}