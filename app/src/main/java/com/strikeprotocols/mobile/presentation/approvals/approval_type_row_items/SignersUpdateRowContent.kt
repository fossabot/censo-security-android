package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.ui.theme.DetailInfoLightBackground
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun SignersUpdateRowContent(signersUpdate: SolanaApprovalRequestType.SignersUpdate) {
    val header = signersUpdate.getHeader(LocalContext.current)
    val value = signersUpdate.signer.value
    val email = value.email

    Column(
        modifier = Modifier.background(DetailInfoLightBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
        Text(
            email,
            color = GreyText,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}