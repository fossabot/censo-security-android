package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.censocustody.android.presentation.approvals.ApprovalButtonRow
import com.censocustody.android.presentation.approvals.ApprovalItemHeader
import com.censocustody.android.ui.theme.*

@Composable
fun ApprovalRowItem(
    timeRemainingInSeconds: Long?,
    onApproveClicked: () -> Unit,
    onMoreInfoClicked: () -> Unit,
    rowMetaData: ApprovalRowMetaData,
    positiveButtonText: String,
    showApprovalButton: Boolean,
    content: @Composable() () -> Unit
) {
    Column(
        modifier = Modifier
            .shadow(elevation = 5.dp)
            .clip(RoundedCornerShape(4.dp))
            .fillMaxWidth()
            .background(color = Color.White),
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
            positiveButtonText = positiveButtonText,
            displayApproveButton = showApprovalButton
        )
    }
}

data class ApprovalRowMetaData(
    val vaultName : String?
)