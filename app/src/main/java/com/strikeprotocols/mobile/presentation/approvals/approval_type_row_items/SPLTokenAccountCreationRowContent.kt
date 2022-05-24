package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun SPLTokenAccountCreationRowContent(
    splTokenAccountCreation: SolanaApprovalRequestType.SPLTokenAccountCreation
) {
    val header = splTokenAccountCreation.getHeader(LocalContext.current)
    Text(text = "Implement SPL Token Account Creation Row UI", color = StrikeWhite)
}