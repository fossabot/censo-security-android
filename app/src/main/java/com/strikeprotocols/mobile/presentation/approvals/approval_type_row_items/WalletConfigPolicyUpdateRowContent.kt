package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalDispositionsRequired
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader

@Composable
fun WalletConfigPolicyUpdateRowContent(
    walletConfigPolicyUpdate: SolanaApprovalRequestType.WalletConfigPolicyUpdate,
    approvalsNeeded: Int
) {
    val header = walletConfigPolicyUpdate.getHeader(LocalContext.current)
    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 6)
    ApprovalDispositionsRequired(approvalsNeeded = approvalsNeeded)
    Spacer(modifier = Modifier.height(24.dp))
}

