package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.censocustody.android.data.models.approval.ApprovalRequestDetails

@Composable
fun ConversionRequestRowContent(conversionRequest: ApprovalRequestDetails.ConversionRequest) {
    val header = conversionRequest.getHeader(LocalContext.current)
    val usdEquivalent = conversionRequest.symbolAndAmountInfo.getUSDEquivalentText(context = LocalContext.current, hideSymbol = true)
    val fromAccount = conversionRequest.account.name
    val toAccount = conversionRequest.destination.name

    TransferConversionContent(
        header = header,
        subtitle = usdEquivalent,
        fromText = fromAccount,
        toText = toAccount
    )
    Spacer(modifier = Modifier.height(20.dp))
}