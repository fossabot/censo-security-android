package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.maskAddress
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequestDetails
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.*
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData

@Composable
fun ConversionDetailContent(
    conversionRequest: ApprovalRequestDetails.ConversionRequest
) {
    val header = conversionRequest.getHeader(LocalContext.current)
    val usdEquivalent = conversionRequest.symbolAndAmountInfo.getUSDEquivalentText(
        context = LocalContext.current,
        hideSymbol = true
    )
    val fromAccount = conversionRequest.account.name
    val toAccount = conversionRequest.destination.name
    val address = conversionRequest.destination.address

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    ApprovalSubtitle(text = usdEquivalent)
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