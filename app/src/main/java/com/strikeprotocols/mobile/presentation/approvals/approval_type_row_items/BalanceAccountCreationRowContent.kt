package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.components.AccountChangeItem
import com.strikeprotocols.mobile.ui.theme.DetailInfoLightBackground

@Composable
fun BalanceAccountCreationRowContent(
    balanceAccountCreation: SolanaApprovalRequestType.BalanceAccountCreation
) {
    val header = balanceAccountCreation.getHeader(LocalContext.current)
    val accountName = balanceAccountCreation.accountInfo.name
    val accountType =
        balanceAccountCreation.accountInfo.accountType.getUITitle(LocalContext.current)

    Column(
        modifier = Modifier.background(DetailInfoLightBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AccountChangeItem(header = header, title = accountName, subtitle = accountType)
        Spacer(modifier = Modifier.height(20.dp))
    }
}