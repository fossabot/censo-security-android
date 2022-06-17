package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.BalanceChangeRowItem
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack

@Composable
fun DAppTransactionDetailContent(
    dAppTransactionRequest: SolanaApprovalRequestType.DAppTransactionRequest
) {
    Column(
        modifier = Modifier.background(BackgroundBlack),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val header = dAppTransactionRequest.getHeader(LocalContext.current)
        val dappName = dAppTransactionRequest.dappInfo.name
        val walletName = dAppTransactionRequest.account.name
        ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 16)

        for ((index, balanceChange) in dAppTransactionRequest.balanceChanges.withIndex()) {
            BalanceChangeRowItem(
                accountName = walletName,
                dappName = dappName,
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