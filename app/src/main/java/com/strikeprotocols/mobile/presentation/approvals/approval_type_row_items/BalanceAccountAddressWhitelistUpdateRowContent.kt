package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.common.toWalletName
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader

@Composable
fun BalanceAccountAddressWhitelistUpdateRowContent(
    accountAddressWhitelistUpdate: SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate
) {
    val header = accountAddressWhitelistUpdate.getHeader(LocalContext.current)

    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = accountAddressWhitelistUpdate.accountInfo.name.toWalletName(), fontSize = 20.sp)
    Spacer(modifier = Modifier.height(20.dp))
}