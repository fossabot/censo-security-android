package com.censocustody.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.mobile.data.models.approval.ApprovalRequestDetails
import com.censocustody.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.censocustody.mobile.presentation.approvals.ApprovalContentHeader

@Composable
fun BalanceAccountNameUpdateRowContent(
    accountNameUpdate: ApprovalRequestDetails.BalanceAccountNameUpdate
) {
    val header = accountNameUpdate.getHeader(LocalContext.current)
    val fromAccount = accountNameUpdate.accountInfo.name
    val toAccount = accountNameUpdate.newAccountName

    val fromToText = buildFromToDisplayText(from = fromAccount, to = toAccount, LocalContext.current)

    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = fromToText, fontSize = 20.sp)
    Spacer(modifier = Modifier.height(20.dp))
}