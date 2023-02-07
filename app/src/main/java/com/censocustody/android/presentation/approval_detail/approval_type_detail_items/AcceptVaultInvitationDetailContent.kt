package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.censocustody.android.presentation.approvals.ApprovalRowContentHeader

@Composable
fun AcceptVaultInvitationDetailContent(header: String) {
    ApprovalRowContentHeader(header = header, bottomSpacing = 36)
    Spacer(modifier = Modifier.height(24.dp))
}