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
import com.strikeprotocols.mobile.data.models.approval.BooleanSetting
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getUITitle
import com.strikeprotocols.mobile.presentation.components.AccountChangeItem
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData

@Composable
fun BalanceAccountSettingsUpdateDetailContent(
    accountSettingsUpdate: SolanaApprovalRequestType.BalanceAccountSettingsUpdate
) {
    val header = accountSettingsUpdate.getHeader(LocalContext.current)
    val accountName = accountSettingsUpdate.account.name
    val accountType = accountSettingsUpdate.account.accountType.getUITitle(LocalContext.current)

    AccountChangeItem(header = header, title = accountName, subtitle = accountType, headerTopSpacing = 24)
    Spacer(Modifier.height(24.dp))

    val settingsRowInfoData =
        generateBalanceAccountSettingsUpdateDetailRows(accountSettingsUpdate, LocalContext.current)

    FactRow(
        factsData = settingsRowInfoData[0]
    )
}

fun generateBalanceAccountSettingsUpdateDetailRows(
    accountSettingsUpdate: SolanaApprovalRequestType.BalanceAccountSettingsUpdate,
    context: Context
): List<FactsData> {
    val settingsUpdateRowInfoData = mutableListOf<FactsData>()
    var settingsRowTitle = ""

    val dappsEnabled = accountSettingsUpdate.dappsEnabled
    val whitelistEnabled = accountSettingsUpdate.whitelistEnabled

    val settingsUpdateList = mutableListOf<Pair<String, String>>()
    if (dappsEnabled != null) {
        settingsRowTitle = context.getString(R.string.dapps_enabled_title)
        if (dappsEnabled.value == BooleanSetting.On.value) {
            settingsUpdateList.add(Pair(context.getString(R.string.yes), ""))
        } else {
            settingsUpdateList.add(Pair(context.getString(R.string.no), ""))
        }
    } else if (whitelistEnabled != null) {
        settingsRowTitle = context.getString(R.string.whitelisting_enabled_title)
        if (whitelistEnabled.value == BooleanSetting.On.value) {
            settingsUpdateList.add(Pair(context.getString(R.string.yes), ""))
        } else {
            settingsUpdateList.add(Pair(context.getString(R.string.no), ""))
        }
    }

    val settingsRow = FactsData(
        title = settingsRowTitle,
        facts = settingsUpdateList
    )
    settingsUpdateRowInfoData.add(settingsRow)

    return settingsUpdateRowInfoData
}