package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader

@Composable
fun BalanceAccountSettingsUpdateDetailContent(
    accountSettingsUpdate: SolanaApprovalRequestType.BalanceAccountSettingsUpdate
) {
    val header = accountSettingsUpdate.getHeader(LocalContext.current)
    val accountName = accountSettingsUpdate.account.name

    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = accountName)
    Spacer(modifier = Modifier.height(28.dp))
}