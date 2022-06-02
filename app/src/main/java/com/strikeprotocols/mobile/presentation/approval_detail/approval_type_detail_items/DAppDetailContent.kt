package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun DAppTransactionDetailContent(
    dAppWalletApproval: SolanaApprovalRequestType.DAppTransactionRequest
) {
    Column(
        modifier = Modifier.background(BackgroundBlack),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val header = dAppWalletApproval.getHeader(LocalContext.current)
        val dappName = dAppWalletApproval.dappInfo.name
        val walletName = dAppWalletApproval.account.name
        ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
        Text(
            dappName,
            color = GreyText,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        val factsData = FactsData(
            facts =
            listOf(
                Pair(stringResource(R.string.wallet_name_title), walletName),
                Pair(stringResource(R.string.dapp_name_title), dappName)
            )
        )
        FactRow(factsData = factsData)
    }
    Spacer(modifier = Modifier.height(28.dp))
}