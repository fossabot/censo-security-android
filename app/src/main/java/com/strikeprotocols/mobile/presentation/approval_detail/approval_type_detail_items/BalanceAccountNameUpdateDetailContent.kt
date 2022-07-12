package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.buildFromToDisplayText
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader

@Composable
fun BalanceAccountNameUpdateDetailContent(
    accountNameUpdate: SolanaApprovalRequestType.BalanceAccountNameUpdate
) {
    val header = accountNameUpdate.getHeader(LocalContext.current)
    val fromAccount = accountNameUpdate.accountInfo.name
    val toAccount = accountNameUpdate.newAccountName
    val fromToText = buildFromToDisplayText(from = fromAccount, to = toAccount, LocalContext.current)

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)
    ApprovalSubtitle(text = fromToText, fontSize = 20.sp)
    Spacer(modifier = Modifier.height(16.dp))

}