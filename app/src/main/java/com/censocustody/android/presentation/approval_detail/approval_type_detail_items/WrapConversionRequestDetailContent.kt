package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.presentation.approvals.ApprovalRowContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getUSDEquivalentText
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData

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
            RowData(title = stringResource(R.string.wallet_title), value = wallet),
            RowData(title = stringResource(R.string.swap_for), value = swapFor)
        )
    )
    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}