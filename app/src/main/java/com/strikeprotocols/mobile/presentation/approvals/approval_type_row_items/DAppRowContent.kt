package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequestDetails
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.ui.theme.*

@Composable
fun DAppTransactionRowContent(dAppTransactionRequest: ApprovalRequestDetails.DAppTransactionRequest) {
    Column(
        modifier = Modifier.background(DetailInfoLightBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val header = dAppTransactionRequest.getHeader(LocalContext.current)
        ApprovalRowContentHeader(header = header, topSpacing = 16)

        for ((index, balanceChange) in dAppTransactionRequest.balanceChanges.withIndex()) {
            BalanceChangeRowItem(
                accountName = dAppTransactionRequest.account.name,
                dappName = dAppTransactionRequest.dappInfo.name,
                symbolAndAmountInfo = balanceChange
            )
            if (index == dAppTransactionRequest.balanceChanges.size - 1) {
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}