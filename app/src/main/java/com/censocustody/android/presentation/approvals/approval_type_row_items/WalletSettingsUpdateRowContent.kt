package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.common.toWalletName
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.censocustody.android.presentation.approvals.ApprovalRowContentHeader

@Composable
fun WalletSettingsUpdateRowContent(
    header: String, name: String
) {
    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = name.toWalletName(LocalContext.current), fontSize = 20.sp)
    Spacer(modifier = Modifier.height(20.dp))
}