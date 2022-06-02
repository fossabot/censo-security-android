package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun BalanceAccountNameUpdateRowContent(
    accountNameUpdate: SolanaApprovalRequestType.BalanceAccountNameUpdate
) {
    val header = accountNameUpdate.getHeader(LocalContext.current)
    val fromAccount = accountNameUpdate.accountInfo.name
    val toAccount = accountNameUpdate.newAccountName

    val fromToText = buildFromToDisplayText(from = fromAccount, to = toAccount, LocalContext.current)

    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    Text(
        fromToText,
        color = GreyText,
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        letterSpacing = 0.23.sp
    )
    Spacer(modifier = Modifier.height(20.dp))
}