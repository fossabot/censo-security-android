package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.censocustody.android.presentation.approvals.ApprovalRowContentHeader

@Composable
fun LoginApprovalRowContent(header: String, email: String?) {
    ApprovalRowContentHeader(header = header, bottomSpacing = 8)

    if (!email.isNullOrEmpty()) {
        ApprovalSubtitle(text = email)
        Spacer(modifier = Modifier.height(20.dp))
    } else {
        Spacer(modifier = Modifier.height(16.dp))
    }
}
