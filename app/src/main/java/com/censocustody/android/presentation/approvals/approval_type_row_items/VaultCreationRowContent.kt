package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.censocustody.android.common.toVaultName
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.censocustody.android.presentation.approvals.ApprovalRowContentHeader

@Composable
fun VaultCreationRowContent(
    header: String,
    vaultName: String
) {
    ApprovalRowContentHeader(header = header, topSpacing = 16)
    ApprovalSubtitle(text = vaultName.toVaultName(LocalContext.current), fontSize = 20.sp)
}

