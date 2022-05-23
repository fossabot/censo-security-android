package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun BalanceAccountPolicyUpdateRowContent(
    accountPolicyUpdate: SolanaApprovalRequestType.BalanceAccountPolicyUpdate?
) {
    Text(text = "Implement Balance Account Policy Update Row UI", color = StrikeWhite)
}