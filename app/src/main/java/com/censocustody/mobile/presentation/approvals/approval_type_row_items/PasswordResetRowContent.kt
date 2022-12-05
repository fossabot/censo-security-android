package com.censocustody.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.mobile.data.models.approval.ApprovalRequestDetails
import com.censocustody.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.censocustody.mobile.presentation.approvals.ApprovalRowContentHeader

@Composable
fun PasswordResetRowContent(
    passwordReset: ApprovalRequestDetails.PasswordReset,
    email: String
) {
    val header = passwordReset.getHeader(LocalContext.current)
    ApprovalRowContentHeader(header = header, bottomSpacing = 8)
    ApprovalSubtitle(text = email, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(20.dp))
}