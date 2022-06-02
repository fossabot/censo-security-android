package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader

@Composable
fun BalanceAccountPolicyUpdateRowContent(
    accountPolicyUpdate: SolanaApprovalRequestType.BalanceAccountPolicyUpdate
) {
    val header = accountPolicyUpdate.getHeader(LocalContext.current)

    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = accountPolicyUpdate.accountInfo.name)
    Spacer(modifier = Modifier.height(24.dp))
}