package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType

@Composable
fun ConversionRequestRowContent(conversionRequest: SolanaApprovalRequestType.ConversionRequest) {
    val header = conversionRequest.getHeader(LocalContext.current)
    val usdEquivalent = conversionRequest.symbolAndAmountInfo.getUSDEquivalentText(context = LocalContext.current, hideSymbol = true)
    val fromAccount = conversionRequest.account.name
    val toAccount = conversionRequest.destination.name

    TransferConversionContent(
        header = header,
        usdEquivalent = usdEquivalent,
        fromText = fromAccount,
        toText = toAccount
    )
}