package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.maskAddress
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.*
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun WithdrawalRequestDetailContent(
    withdrawalRequest: SolanaApprovalRequestType.WithdrawalRequest
) {
    val header = withdrawalRequest.getHeader(LocalContext.current)
    val usdEquivalent = withdrawalRequest.symbolAndAmountInfo.getUSDEquivalentText(
        context = LocalContext.current,
        hideSymbol = true
    )
    val fromAccount = withdrawalRequest.account.name
    val toAccount = withdrawalRequest.destination.name
    val address = withdrawalRequest.destination.address

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    Text(
        usdEquivalent,
        color = GreyText,
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        letterSpacing = 0.23.sp
    )
    Spacer(modifier = Modifier.height(24.dp))

    val factsData = FactsData(
        facts = listOf(
            Pair(
                stringResource(R.string.from_wallet),
                fromAccount
            ),
            Pair(
                stringResource(R.string.destination),
                toAccount
            ),
            Pair(
                stringResource(R.string.destination_address),
                address.maskAddress()
            )
        )
    )
    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}