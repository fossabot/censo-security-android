package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getUITitle
import com.strikeprotocols.mobile.presentation.components.AccountChangeItem
import com.strikeprotocols.mobile.ui.theme.AccountTextGrey
import com.strikeprotocols.mobile.ui.theme.BackgroundLight
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun SPLTokenAccountCreationDetailContent(
    splTokenAccountCreation: SolanaApprovalRequestType.SPLTokenAccountCreation
) {
    val header = splTokenAccountCreation.getHeader(LocalContext.current)
    val accountName = splTokenAccountCreation.payerBalanceAccount.name
    val accountType = splTokenAccountCreation.payerBalanceAccount.accountType.getUITitle(LocalContext.current)
    val symbol = splTokenAccountCreation.tokenSymbolInfo.symbol

    AccountChangeItem(header = header, title = accountName, subtitle = accountType, headerTopSpacing = 24)
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .background(color = BackgroundLight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.symbol_title), color = GreyText, modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp))
        if (symbol.isNotEmpty()) {
            Text(text = symbol, color = AccountTextGrey, modifier = Modifier.padding(end = 12.dp))
        }
    }
}