package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.runtime.Composable
import com.censocustody.android.presentation.approvals.ApprovalRowContentHeader

@Composable
fun WalletConfigPolicyUpdateRowContent(
    header: String
) {
    ApprovalRowContentHeader(header = header, topSpacing = 16)
}

