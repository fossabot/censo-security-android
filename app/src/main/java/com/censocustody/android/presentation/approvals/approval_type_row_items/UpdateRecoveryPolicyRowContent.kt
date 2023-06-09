package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.censocustody.android.presentation.approvals.ApprovalContentHeader

@Composable
fun UpdateRecoveryPolicyRowContent(
    header: String
) {
    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    Spacer(modifier = Modifier.height(20.dp))
}