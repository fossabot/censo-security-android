package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData

@Composable
fun AcceptVaultInvitationDetailContent(
    acceptVaultInvitation: SolanaApprovalRequestType.AcceptVaultInvitation
) {
    val header = acceptVaultInvitation.getHeader(LocalContext.current)
    ApprovalRowContentHeader(header = header, bottomSpacing = 36)
    Spacer(modifier = Modifier.height(24.dp))
}