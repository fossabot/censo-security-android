package com.strikeprotocols.mobile.presentation.approvals.approval_type_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.generateWalletApprovalsDummyData
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approvals.ApprovalButtonRow
import com.strikeprotocols.mobile.presentation.approvals.ApprovalItemHeader
import com.strikeprotocols.mobile.presentation.components.StrikeTagRow
import com.strikeprotocols.mobile.ui.theme.*
import java.util.*

@Composable
fun ApprovalRowItem(
    timeRemainingInSeconds: Int,
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
        Spacer(modifier = Modifier.height(8.dp))
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