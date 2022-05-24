package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun AddressBookUpdateDetailContent(
    addressBookUpdate: SolanaApprovalRequestType.AddressBookUpdate
) {
    val header = addressBookUpdate.getHeader(LocalContext.current)
    Text(text = "Implement Address Book Update Detail UI", color = StrikeWhite)
}