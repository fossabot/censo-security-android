package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.common.toWalletName
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.ApprovalRowContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.*
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData

@Composable
fun WalletAddressWhitelistUpdateDetailContent(
    whitelistUpdate: WhitelistUpdateUI
) {
    ApprovalRowContentHeader(header = whitelistUpdate.header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = whitelistUpdate.name.toWalletName(LocalContext.current), fontSize = 20.sp)
    Spacer(modifier = Modifier.height(16.dp))

    val destinationsRowInfoData =
        generateWalletAddressWhitelistUpdateDetailRows(
            whitelistUpdate.destinations,
            LocalContext.current
        )

    FactRow(
        factsData = destinationsRowInfoData[0]
    )
    getFeeEstimate(LocalContext.current, whitelistUpdate.fee)?.let { factsData ->
        Spacer(modifier = Modifier.height(20.dp))
        FactRow(
            factsData = factsData
        )
    }

    Spacer(modifier = Modifier.height(28.dp))
}

fun generateWalletAddressWhitelistUpdateDetailRows(
    destinations: List<ApprovalRequestDetailsV2.DestinationAddress>,
    context: Context
): List<FactsData> {
    val destinationsRowInfoData = mutableListOf<FactsData>()

    val destinationsList = destinations.retrieveDestinationsRowData()

    if (destinationsList.isEmpty()) {
        destinationsList.add(
            RowData(
                title = context.getString(R.string.no_whitelisted_addresses),
                value = ""
            )
        )
    }

    val destinationsRow = FactsData(
        title = context.getString(R.string.whitelisted_addresses_title),
        facts = destinationsList
    )
    destinationsRowInfoData.add(destinationsRow)

    return destinationsRowInfoData
}

fun getFeeEstimate(context: Context, fee: ApprovalRequestDetailsV2.Amount): FactsData? {
    return fee.usdEquivalent?.let {
        FactsData(
            title = context.getString(R.string.fees),
            listOf(
                RowData(
                    title = context.getString(R.string.fee_estimate),
                    value = fee.formattedUsdEquivalentWithSymbol()
                )
            )
        )
    }
}

data class WhitelistUpdateUI(
    val header: String, val name: String,
    val destinations: List<ApprovalRequestDetailsV2.DestinationAddress>,
    val fee: ApprovalRequestDetailsV2.Amount
)