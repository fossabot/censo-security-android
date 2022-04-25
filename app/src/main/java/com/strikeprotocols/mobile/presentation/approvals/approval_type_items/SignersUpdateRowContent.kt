package com.strikeprotocols.mobile.presentation.approvals.approval_type_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.approval.SlotUpdateType
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.components.AccountChangeItem
import com.strikeprotocols.mobile.ui.theme.DetailInfoLightBackground

@Composable
fun SignersUpdateRowContent(signersUpdate: SolanaApprovalRequestType.SignersUpdate) {
    val mainTitle =
        if (signersUpdate.slotUpdateType == SlotUpdateType.Clear) {
            stringResource(R.string.removing_signer)
        } else {
            stringResource(R.string.new_signer)
        }
    val value = signersUpdate.signer.value
    val name = value.name
    val email = value.email

    Column(
        modifier = Modifier.background(DetailInfoLightBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AccountChangeItem(mainTitle = mainTitle, title = name, subtitle = email)
    }
}