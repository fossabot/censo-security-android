package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

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
import com.strikeprotocols.mobile.data.models.approval.SlotDestinationInfo
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun AddressBookUpdateRowContent(
    addressBookUpdate: SolanaApprovalRequestType.AddressBookUpdate
) {

    val entryMetaData : Pair<SolanaApprovalRequestType.AddRemoveChange, SlotDestinationInfo>?
        = addressBookUpdate.getEntryMetaData()

    val name = entryMetaData?.second?.value?.name ?: stringResource(id = R.string.not_applicable)
    val header = addressBookUpdate.getHeader(LocalContext.current)

    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 6)
    ApprovalSubtitle(text = name)
    Spacer(modifier = Modifier.height(20.dp))
}