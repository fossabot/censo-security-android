package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.maskAddress
import com.strikeprotocols.mobile.data.models.approval.SlotDestinationInfo
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.ui.theme.DetailInfoDarkBackground
import com.strikeprotocols.mobile.ui.theme.DetailInfoLightBackground
import com.strikeprotocols.mobile.ui.theme.DividerGrey

@Composable
fun AddressBookUpdateDetailContent(
    addressBookUpdate: SolanaApprovalRequestType.AddressBookUpdate
) {
    val header = addressBookUpdate.getHeader(LocalContext.current)
    val entryMetaData : Pair<SolanaApprovalRequestType.AddRemoveChange, SlotDestinationInfo>? = addressBookUpdate.getEntryMetaData()

    val name = entryMetaData?.second?.value?.name ?: stringResource(id = R.string.not_applicable)
    val address = entryMetaData?.second?.value?.address?.maskAddress() ?: stringResource(id = R.string.not_applicable)

    ApprovalContentHeader(header = header, topSpacing = 12, bottomSpacing = 36)
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        ApprovalInfoRow(
            backgroundColor = DetailInfoLightBackground,
            title = stringResource(R.string.name),
            value = name
        )
        Divider(modifier = Modifier.height(0.5.dp), color = DividerGrey)
        ApprovalInfoRow(
            backgroundColor = DetailInfoDarkBackground,
            title = stringResource(R.string.address),
            value = address
        )
    }

}