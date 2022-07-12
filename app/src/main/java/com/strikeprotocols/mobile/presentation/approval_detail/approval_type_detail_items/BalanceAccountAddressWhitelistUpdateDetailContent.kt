package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.toWalletName
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.*
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData

@Composable
fun BalanceAccountAddressWhitelistUpdateDetailContent(
    addressWhitelistUpdate: SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate
) {
    val header = addressWhitelistUpdate.getHeader(LocalContext.current)

    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = addressWhitelistUpdate.accountInfo.name.toWalletName(), fontSize = 20.sp)
    Spacer(modifier = Modifier.height(16.dp))

    val destinationsRowInfoData =
        generateBalanceAccountAddressWhitelistUpdateDetailRows(
            addressWhitelistUpdate,
            LocalContext.current
        )

    FactRow(
        factsData = destinationsRowInfoData[0]
    )
    Spacer(modifier = Modifier.height(28.dp))
}

fun generateBalanceAccountAddressWhitelistUpdateDetailRows(addressWhitelistUpdate: SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate, context: Context) : List<FactsData> {
    val destinationsRowInfoData = mutableListOf<FactsData>()

    val destinationsList = addressWhitelistUpdate.destinations.retrieveDestinationsRowData()

    if (destinationsList.isEmpty()) {
        destinationsList.add(Pair(context.getString(R.string.no_whitelisted_addresses), ""))
    }

    val destinationsRow = FactsData(
        title = context.getString(R.string.whitelisted_addresses_title),
        facts = destinationsList
    )
    destinationsRowInfoData.add(destinationsRow)

    return destinationsRowInfoData
}