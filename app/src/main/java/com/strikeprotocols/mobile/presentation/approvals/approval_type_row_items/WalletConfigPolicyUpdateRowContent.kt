package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun WalletConfigPolicyUpdateRowContent(
    walletConfigPolicyUpdate: SolanaApprovalRequestType.WalletConfigPolicyUpdate
) {
    val header = walletConfigPolicyUpdate.getHeader(LocalContext.current)
    Text(text = "Implement Wallet Config Policy Update Row UI", color = StrikeWhite)
}

