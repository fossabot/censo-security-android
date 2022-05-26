package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.maskAddress
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getUITitle
import com.strikeprotocols.mobile.presentation.components.AccountChangeItem
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData

@Composable
fun BalanceAccountAddressWhitelistUpdateDetailContent(
    addressWhitelistUpdate: SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate
) {
    val header = addressWhitelistUpdate.getHeader(LocalContext.current)
    val accountName = addressWhitelistUpdate.accountInfo.name
    val accountType = addressWhitelistUpdate.accountInfo.accountType.getUITitle(LocalContext.current)

    AccountChangeItem(header = header, title = accountName, subtitle = accountType, headerTopSpacing = 24, headerBottomSpacing = 24)
    Spacer(modifier = Modifier.height(16.dp))

    val destinationsRowInfoData =
        generateBalanceAccountAddressWhitelistUpdateDetailRows(
            addressWhitelistUpdate,
            LocalContext.current
        )

    FactRow(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        factsData = destinationsRowInfoData[0]
    )
}

fun generateBalanceAccountAddressWhitelistUpdateDetailRows(addressWhitelistUpdate: SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate, context: Context) : List<FactsData> {
    val destinationsRowInfoData = mutableListOf<FactsData>()

    val destinationsList = mutableListOf<Pair<String, String>>()
    if (!addressWhitelistUpdate.destinations.isNotEmpty()) {
        for (destination in addressWhitelistUpdate.destinations) {
            destinationsList.add(Pair(destination.value.name, destination.value.address.maskAddress()))
        }
    } else {
        destinationsList.add(Pair(context.getString(R.string.no_whitelisted_addresses), ""))
    }

    val destinationsRow = FactsData(
        title = context.getString(R.string.destinations_title),
        facts = destinationsList
    )
    destinationsRowInfoData.add(destinationsRow)

    return destinationsRowInfoData
}