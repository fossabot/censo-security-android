package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.*
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
fun SPLTokenAccountCreationDetailContent(
    splTokenAccountCreation: SolanaApprovalRequestType.SPLTokenAccountCreation
) {
    val header = splTokenAccountCreation.getHeader(LocalContext.current)
    val accountName = splTokenAccountCreation.payerBalanceAccount.name
    val symbol = splTokenAccountCreation.tokenSymbolInfo.symbol

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    ApprovalSubtitle(text = symbol)
    Spacer(modifier = Modifier.height(24.dp))
    val factsData = FactsData(
        facts = listOf(
            Pair(stringResource(R.string.wallet_title), accountName),
            Pair(stringResource(R.string.token_title), symbol)
        )
    )
    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}