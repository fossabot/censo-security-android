package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun DAppBookUpdateRowContent(
    dAppBookUpdate: SolanaApprovalRequestType.DAppBookUpdate?
) {
    Text(text = "Implement DApp Book Update Row UI", color = StrikeWhite)
}