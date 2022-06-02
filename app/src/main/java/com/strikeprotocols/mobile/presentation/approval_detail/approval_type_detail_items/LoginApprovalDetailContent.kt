package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun LoginApprovalDetailContent(loginApproval: SolanaApprovalRequestType.LoginApprovalRequest) {
    ApprovalContentHeader(header = loginApproval.getHeader(LocalContext.current), topSpacing = 24, bottomSpacing = 8)
    //TODO: Use the user data here once the AuthFlow PR is merged.
    // It is needed for user email and name
    Text(
        "",
        color = GreyText,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
    Spacer(modifier = Modifier.height(20.dp))
    val factsData = FactsData(
        facts = listOf(
            Pair(stringResource(R.string.login_name), "Jon Doe"),
            Pair(stringResource(R.string.login_email), "jdoe@blue.rock")
        )
    )

    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}