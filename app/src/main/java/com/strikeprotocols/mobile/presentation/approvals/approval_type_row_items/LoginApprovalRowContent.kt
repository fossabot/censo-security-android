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
import com.strikeprotocols.mobile.presentation.approvals.ApprovalRowContentHeader
import com.strikeprotocols.mobile.ui.theme.GreyText
import java.util.*

@Composable
fun LoginApprovalRowContent(loginApproval: SolanaApprovalRequestType.LoginApprovalRequest) {
    val header = loginApproval.getHeader(LocalContext.current)
    ApprovalRowContentHeader(header = header, bottomSpacing = 8)

    //TODO: Use the user data here once the AuthFlow PR is merged.
    val userEmail : String? = if(Random().nextBoolean()) "" else null

    if (!userEmail.isNullOrEmpty()) {
        Text(
            "",
            color = GreyText,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            letterSpacing = 0.23.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
    } else {
        Spacer(modifier = Modifier.height(16.dp))
    }
}