package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.censocustody.android.presentation.approvals.ApprovalContentHeader

@Composable
fun SuspendUserRowContent(
    header: String, name: String
) {
    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = name, fontSize = 20.sp)
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
@Preview
fun SuspendUserRowContentPreview() {
    val details = ApprovalRequestDetailsV2.SuspendUser("User 1", "user1@org.com", null)
    SuspendUserRowContent(
        details.getHeader(LocalContext.current),
        details.name
    )
}
