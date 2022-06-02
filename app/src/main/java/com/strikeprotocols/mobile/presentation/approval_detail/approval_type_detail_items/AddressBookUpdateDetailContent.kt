package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.maskAddress
import com.strikeprotocols.mobile.data.models.approval.SlotDestinationInfo
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun AddressBookUpdateDetailContent(
    addressBookUpdate: SolanaApprovalRequestType.AddressBookUpdate
) {
    val header = addressBookUpdate.getHeader(LocalContext.current)
    val entryMetaData : Pair<SolanaApprovalRequestType.AddRemoveChange, SlotDestinationInfo>? = addressBookUpdate.getEntryMetaData()

    val name = entryMetaData?.second?.value?.name ?: stringResource(id = R.string.not_applicable)
    val address = entryMetaData?.second?.value?.address?.maskAddress() ?: stringResource(id = R.string.not_applicable)

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    Text(
        name,
        color = GreyText,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
    Spacer(modifier = Modifier.height(24.dp))
    val factsData = FactsData(
        facts = listOf(
            Pair(stringResource(R.string.name), name),
            Pair(stringResource(R.string.address), address)
        )
    )
    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}