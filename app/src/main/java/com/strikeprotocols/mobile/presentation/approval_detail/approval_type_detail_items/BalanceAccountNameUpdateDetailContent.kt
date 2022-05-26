package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.components.StrikeTagLabeledRow

@Composable
fun BalanceAccountNameUpdateDetailContent(
    accountNameUpdate: SolanaApprovalRequestType.BalanceAccountNameUpdate
) {
    val header = accountNameUpdate.getHeader(LocalContext.current)
    val fromAccount = accountNameUpdate.accountInfo.name
    val toAccount = accountNameUpdate.newAccountName

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)
    StrikeTagLabeledRow(
        text1 = fromAccount,
        text2 = toAccount,
        label1 = stringResource(id = R.string.from),
        label2 = stringResource(id = R.string.to),
        arrowForward = true
    )
}