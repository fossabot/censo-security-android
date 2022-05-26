package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalDispositionsRequired
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.components.StrikeTag
import com.strikeprotocols.mobile.ui.theme.SectionBlack

@Composable
fun BalanceAccountPolicyUpdateRowContent(
    accountPolicyUpdate: SolanaApprovalRequestType.BalanceAccountPolicyUpdate,
    approvalsNeeded: Int
) {
    val header = accountPolicyUpdate.getHeader(LocalContext.current)
    val tagText = accountPolicyUpdate.accountInfo.name

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    ApprovalDispositionsRequired(approvalsNeeded = approvalsNeeded)
    Spacer(modifier = Modifier.height(24.dp))
    val tagPaddingValues = PaddingValues(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
    StrikeTag(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = SectionBlack),
        text = tagText,
        paddingValues = tagPaddingValues
    )
}