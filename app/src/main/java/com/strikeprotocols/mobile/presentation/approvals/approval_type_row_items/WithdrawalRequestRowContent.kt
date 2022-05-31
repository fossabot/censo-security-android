package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType

@Composable
fun WithdrawalRequestRowContent(withdrawalRequest: SolanaApprovalRequestType.WithdrawalRequest) {
    val header = withdrawalRequest.getHeader(LocalContext.current)
    val usdEquivalent = withdrawalRequest.symbolAndAmountInfo.getUSDEquivalentText(context = LocalContext.current, hideSymbol = true)
    val fromAccount = withdrawalRequest.account.name
    val toAccount = withdrawalRequest.destination.name

    TransferConversionContent(
        header = header,
        usdEquivalent = usdEquivalent,
        fromText = fromAccount,
        toText = toAccount
    )
    Spacer(modifier = Modifier.height(20.dp))
}