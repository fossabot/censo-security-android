package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.common.toWalletName
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.ApprovalRowContentHeader
import com.censocustody.android.presentation.components.FactRow

@Composable
fun WalletSettingsUpdateDetailContent(
    header: String, name: String, fee: ApprovalRequestDetailsV2.Amount
) {
    ApprovalRowContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = name.toWalletName(LocalContext.current), fontSize = 20.sp)
    getFeeEstimate(LocalContext.current, fee)?.let { factsData ->
        Spacer(modifier = Modifier.height(24.dp))
        FactRow(
            factsData = factsData
        )
    }
    Spacer(modifier = Modifier.height(28.dp))
}