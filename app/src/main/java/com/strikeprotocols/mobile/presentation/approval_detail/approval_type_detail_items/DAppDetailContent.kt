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
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.BalanceChange
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack

@Composable
fun DAppTransactionDetailContent(
    dAppWalletApproval: SolanaApprovalRequestType.DAppTransactionRequest,
    approvalsNeeded: Int
) {
    Column(
        modifier = Modifier.background(BackgroundBlack),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val header = dAppWalletApproval.getHeader(LocalContext.current)
        ApprovalContentHeader(header = header, topSpacing = 24)
        Spacer(modifier = Modifier.height(36.dp))
        ApprovalDispositionsRequired(approvalsNeeded = approvalsNeeded)
        Spacer(modifier = Modifier.height(36.dp))

        if (!dAppWalletApproval.balanceChanges.isNullOrEmpty()) {
            dAppWalletApproval.balanceChanges.forEachIndexed { index, symbolAndAmountInfo ->

                BalanceChange(
                    symbol = symbolAndAmountInfo.symbolInfo.symbol,
                    amount = symbolAndAmountInfo.formattedAmount(),
                    usdEquivalent = symbolAndAmountInfo.formattedUSDEquivalent(false),
                    fromDestination = dAppWalletApproval.dappInfo.name,
                    toDestination = dAppWalletApproval.account.name,
                    positiveChange = symbolAndAmountInfo.isAmountPositive()
                )

                if (index != dAppWalletApproval.balanceChanges.size - 1) {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }

    }
}