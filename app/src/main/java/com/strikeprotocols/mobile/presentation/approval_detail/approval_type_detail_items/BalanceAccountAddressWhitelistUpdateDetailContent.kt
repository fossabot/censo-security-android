package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun BalanceAccountAddressWhitelistUpdateDetailContent(
    addressWhitelistUpdate: SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate?
) {
    Text(text = "Implement Balance Account Address Whitelist Update Detail UI", color = StrikeWhite)
}