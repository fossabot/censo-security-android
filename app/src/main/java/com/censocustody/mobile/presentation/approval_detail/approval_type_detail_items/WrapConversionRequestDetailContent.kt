package com.censocustody.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.censocustody.mobile.R
import com.censocustody.mobile.data.models.approval.ApprovalRequestDetails
import com.censocustody.mobile.presentation.approvals.ApprovalRowContentHeader
import com.censocustody.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.mobile.presentation.approvals.approval_type_row_items.getUSDEquivalentText
import com.censocustody.mobile.presentation.components.FactRow
import com.censocustody.mobile.presentation.components.FactsData

@Composable
fun WrapConversionRequestDetailContent(
    wrapConversionRequest: ApprovalRequestDetails.WrapConversionRequest
) {
    val header = wrapConversionRequest.getHeader(LocalContext.current)
    val usdEquivalent = wrapConversionRequest.symbolAndAmountInfo.getUSDEquivalentText(context = LocalContext.current, hideSymbol = true)

    val wallet = wrapConversionRequest.account.name
    val swapFor = wrapConversionRequest.destinationSymbolInfo.symbol

    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = usdEquivalent)
    Spacer(modifier = Modifier.height(20.dp))
    val factsData = FactsData(
        facts =
        listOf(
            Pair(stringResource(R.string.wallet_title), wallet),
            Pair(stringResource(R.string.swap_for), swapFor)
        )
    )
    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}