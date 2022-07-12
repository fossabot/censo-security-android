package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.ui.theme.DetailInfoLightBackground

@Composable
fun SignersUpdateRowContent(signersUpdate: SolanaApprovalRequestType.SignersUpdate) {
    val header = signersUpdate.getHeader(LocalContext.current)
    val value = signersUpdate.signer.value

    Column(
        modifier = Modifier.background(DetailInfoLightBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
        ApprovalSubtitle(text = value.name, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(20.dp))
    }
}