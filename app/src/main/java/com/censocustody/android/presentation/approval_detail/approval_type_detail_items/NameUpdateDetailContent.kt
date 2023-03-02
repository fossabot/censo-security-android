package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.RenameType
import com.censocustody.android.presentation.approvals.approval_type_row_items.buildFromToDisplayText

@Composable
fun NameUpdateDetailContent(header: String, oldName: String, newName: String, renameType: RenameType) {
    val fromToText = buildFromToDisplayText(from = oldName, to = newName, LocalContext.current, renameType)

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)
    ApprovalSubtitle(text = fromToText, fontSize = 20.sp)
    Spacer(modifier = Modifier.height(16.dp))
}