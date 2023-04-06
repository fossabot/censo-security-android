package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData

@Composable
fun SuspendUserDetailContent(
    details: ApprovalRequestDetailsV2.SuspendUser,
) {
    val header = details.getHeader(LocalContext.current)
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)

    Spacer(modifier = Modifier.height(24.dp))
    FactRow(
        factsData = FactsData(
            title = LocalContext.current.getString(R.string.user_info),
            facts = listOf(
                RowData(
                    title = details.name,
                    value = details.email,
                    userImage = details.jpegThumbnail,
                    userRow = true
                ),
            )
        )
    )
    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
@Preview
fun SuspendUserDetailsContentPreview() {
    SuspendUserDetailContent(ApprovalRequestDetailsV2.SuspendUser("User 1", "user1@org.com", null))
}