package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun BalanceAccountAddressWhitelistUpdateDetailContent(
    addressWhitelistUpdate: SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate
) {
    val header = addressWhitelistUpdate.getHeader(LocalContext.current)
    Text(text = "Implement Balance Account Address Whitelist Update Detail UI", color = StrikeWhite)
}