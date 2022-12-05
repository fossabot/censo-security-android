package com.censocustody.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.censocustody.mobile.presentation.approvals.ApprovalButtonRow
import com.censocustody.mobile.presentation.approvals.ApprovalItemHeader
import com.censocustody.mobile.ui.theme.*

@Composable
fun ApprovalRowItem(
    timeRemainingInSeconds: Long?,
    onApproveClicked: () -> Unit,
    onMoreInfoClicked: () -> Unit,
    rowMetaData: ApprovalRowMetaData,
    positiveButtonText: String,
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
            vaultName = rowMetaData.vaultName
        )
        content()
        ApprovalButtonRow(
            onApproveClicked = onApproveClicked,
            onMoreInfoClicked = onMoreInfoClicked,
            positiveButtonText = positiveButtonText
        )
    }
}

data class ApprovalRowMetaData(
    val vaultName : String?
)