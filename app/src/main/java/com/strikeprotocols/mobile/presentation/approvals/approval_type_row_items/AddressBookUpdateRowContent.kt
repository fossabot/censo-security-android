package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun AddressBookUpdateRowContent(
    addressBookUpdate: SolanaApprovalRequestType.AddressBookUpdate?
) {
    Text(text = "Implement Address Book Update Row UI", color = StrikeWhite)
}