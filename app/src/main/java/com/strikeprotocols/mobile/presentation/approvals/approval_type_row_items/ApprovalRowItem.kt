package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.presentation.approvals.ApprovalButtonRow
import com.strikeprotocols.mobile.presentation.approvals.ApprovalItemHeader
import com.strikeprotocols.mobile.ui.theme.*

@Composable
fun ApprovalRowItem(
    timeRemainingInSeconds: Long,
    onApproveClicked: () -> Unit,
    onMoreInfoClicked: () -> Unit,
    rowMetaData: ApprovalRowMetaData,
    content: @Composable() () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .background(color = DetailInfoLightBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ApprovalItemHeader(
            timeRemainingInSeconds = timeRemainingInSeconds,
            approvalImageVector = rowMetaData.approvalImageVector,
            approvalImageContentDescription = rowMetaData.approvalImageContentDescription,
            approvalType = rowMetaData.approvalTypeTitle
        )
        content()
        ApprovalButtonRow(
            onApproveClicked = onApproveClicked,
            onMoreInfoClicked = onMoreInfoClicked
        )
    }
}

data class ApprovalRowMetaData(
    val approvalImageVector : ImageVector,
    val approvalImageContentDescription : String,
    val approvalTypeTitle : String
)