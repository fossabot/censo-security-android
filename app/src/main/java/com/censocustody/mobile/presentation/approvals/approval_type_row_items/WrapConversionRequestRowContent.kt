package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequestDetails
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader

@Composable
fun WrapConversionRequestRowContent(
    wrapConversionRequest: ApprovalRequestDetails.WrapConversionRequest
) {
    val header = wrapConversionRequest.getHeader(LocalContext.current)
    val usdEquivalent = wrapConversionRequest.symbolAndAmountInfo.getUSDEquivalentText(context = LocalContext.current, hideSymbol = true)

    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = usdEquivalent)
    Spacer(modifier = Modifier.height(20.dp))
}