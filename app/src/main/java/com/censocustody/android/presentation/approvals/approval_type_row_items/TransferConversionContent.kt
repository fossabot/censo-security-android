package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.DAppEthSignDetailContent
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.components.CensoTagRow

@Composable
fun TransferConversionContent(
    header: String,
    subtitle: String,
    fromText: String,
    toText: String
) {
    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = subtitle)
    Spacer(modifier = Modifier.height(32.dp))
    CensoTagRow(
        text1 = fromText,
        text2 = toText,
        arrowForward = true
    )
}

@Composable
@Preview
fun TransferConversionContentPreview() {
    Column() {
        TransferConversionContent(
            header = "Header",
            subtitle = "Subtitle",
            fromText = "From This DApp",
            toText = "To This Dapp"
        )
    }
}