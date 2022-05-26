package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.components.StrikeTag
import com.strikeprotocols.mobile.ui.theme.GreyText
import com.strikeprotocols.mobile.ui.theme.SectionBlack

@Composable
fun SPLTokenAccountCreationRowContent(
    splTokenAccountCreation: SolanaApprovalRequestType.SPLTokenAccountCreation
) {
    val header = splTokenAccountCreation.getHeader(LocalContext.current)
    val accountName = splTokenAccountCreation.payerBalanceAccount.name
    val symbol = splTokenAccountCreation.tokenSymbolInfo.symbol

    ApprovalContentHeader(header = header, topSpacing = 24)
    Spacer(modifier = Modifier.height(8.dp))
    Text(symbol, color = GreyText, fontSize = 13.sp)
    Spacer(modifier = Modifier.height(24.dp))

    val tagPaddingValues = PaddingValues(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
    StrikeTag(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = SectionBlack),
        text = accountName,
        paddingValues = tagPaddingValues
    )
}