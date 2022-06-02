package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun SignersUpdateDetailContent(signersUpdate: SolanaApprovalRequestType.SignersUpdate) {
    val header = signersUpdate.getHeader(LocalContext.current)
    val value = signersUpdate.signer.value
    val name = value.name
    val email = value.email

    Column(
        modifier = Modifier.background(BackgroundBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ApprovalContentHeader(header = header, topSpacing = 24)
        Text(
            email,
            color = GreyText,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        val factsData = FactsData(
            facts =
            listOf(
                Pair(stringResource(R.string.signer_name), name),
                Pair(stringResource(R.string.signer_email), email)
            )
        )
        FactRow(factsData = factsData)
    }
    Spacer(modifier = Modifier.height(28.dp))
}