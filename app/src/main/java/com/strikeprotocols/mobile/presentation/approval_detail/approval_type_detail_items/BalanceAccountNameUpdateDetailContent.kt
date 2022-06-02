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
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData

@Composable
fun BalanceAccountNameUpdateDetailContent(
    accountNameUpdate: SolanaApprovalRequestType.BalanceAccountNameUpdate
) {
    val header = accountNameUpdate.getHeader(LocalContext.current)
    val fromAccount = accountNameUpdate.accountInfo.name
    val toAccount = accountNameUpdate.newAccountName

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)
    val factsData = FactsData(
        facts = listOf(
            Pair(stringResource(R.string.old_wallet_name_title), fromAccount),
            Pair(stringResource(R.string.new_wallet_name_title), toAccount)
        )
    )
    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}