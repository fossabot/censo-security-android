package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.background
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
import com.strikeprotocols.mobile.common.formatISO8601IntoDisplayText
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.*
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData
import com.strikeprotocols.mobile.ui.theme.BackgroundLight

@Composable
fun ConversionDetailContent(
    conversionRequest: SolanaApprovalRequestType.ConversionRequest,
    submitterEmail: String,
    submitterDate: String,
    approvalsNeeded: Int
) {
    val header = conversionRequest.getHeader(LocalContext.current)
    val usdEquivalent = conversionRequest.symbolAndAmountInfo.getUSDEquivalentText(
        context = LocalContext.current,
        hideSymbol = true
    )
    val fromAccount = conversionRequest.account.name
    val toAccount = conversionRequest.destination.name

    val labelData = TransferConversionLabelData(
        showLabels = true,
        label1 = stringResource(id = R.string.from),
        label2 = stringResource(id = R.string.to),
        subText = conversionRequest.destination.subName
    )


    Column(
        modifier = Modifier.background(color = BackgroundLight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TransferConversionContent(
            header = header,
            usdEquivalent = usdEquivalent,
            fromText = fromAccount,
            toText = toAccount,
            transferConversionLabelData = labelData
        )
        Spacer(modifier = Modifier.height(36.dp))
        ApprovalDispositionsRequired(approvalsNeeded = approvalsNeeded)
        Spacer(modifier = Modifier.height(20.dp))
        
        val factsData = FactsData(
            facts = listOf(
                Pair(stringResource(R.string.requested_by), submitterEmail),
                Pair(stringResource(R.string.requested_date), submitterDate.formatISO8601IntoDisplayText(LocalContext.current)),
            )
        )
        
        FactRow(factsData = factsData)
    }
    Spacer(modifier = Modifier.height(28.dp))
}