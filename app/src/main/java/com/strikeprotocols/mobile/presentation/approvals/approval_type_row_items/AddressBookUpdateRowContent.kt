package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun AddressBookUpdateRowContent(
    addressBookUpdate: SolanaApprovalRequestType.AddressBookUpdate
) {
    val name = addressBookUpdate.entry.value.name
    val header = addressBookUpdate.getHeader(LocalContext.current)

    ApprovalContentHeader(header = header, topSpacing = 12, bottomSpacing = 6)
    Text(
        name,
        color = GreyText,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
}