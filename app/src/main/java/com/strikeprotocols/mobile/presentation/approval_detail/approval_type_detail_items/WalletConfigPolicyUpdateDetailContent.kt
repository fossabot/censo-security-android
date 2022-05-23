package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun WalletConfigPolicyUpdateDetailContent(
    walletConfigPolicyUpdate: SolanaApprovalRequestType.WalletConfigPolicyUpdate?
) {
    Text(text = "Implement Wallet Config Policy Update Detail UI", color = StrikeWhite)
}