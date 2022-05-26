package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.components.StrikeTagRow

@Composable
fun BalanceAccountNameUpdateRowContent(
    accountNameUpdate: SolanaApprovalRequestType.BalanceAccountNameUpdate
) {
    val header = accountNameUpdate.getHeader(LocalContext.current)
    val fromAccount = accountNameUpdate.accountInfo.name
    val toAccount = accountNameUpdate.newAccountName

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 24)
    StrikeTagRow(
        text1 = fromAccount,
        text2 = toAccount,
        arrowForward = true
    )
}