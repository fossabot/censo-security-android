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
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.ui.theme.GreyText
import java.util.*

@Composable
fun LoginApprovalRowContent(loginApproval: SolanaApprovalRequestType.LoginApprovalRequest) {
    val header = loginApproval.getHeader(LocalContext.current)
    ApprovalRowContentHeader(header = header, bottomSpacing = 8)

    val userEmail : String? = if(loginApproval.email.isNullOrEmpty()) null else loginApproval.email

    if (!userEmail.isNullOrEmpty()) {
        ApprovalSubtitle(text = userEmail)
        Spacer(modifier = Modifier.height(20.dp))
    } else {
        Spacer(modifier = Modifier.height(16.dp))
    }
}