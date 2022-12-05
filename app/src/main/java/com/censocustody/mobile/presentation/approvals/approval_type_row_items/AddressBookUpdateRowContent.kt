package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader

@Composable
fun CreateOrUpdateAddressBookEntryRowContent(
    header: String,
    entryName: String
) {
    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 6)
    ApprovalSubtitle(text = entryName, fontSize = 20.sp)
    Spacer(modifier = Modifier.height(20.dp))
}