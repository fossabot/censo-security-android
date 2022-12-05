package com.censocustody.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.mobile.common.toWalletName
import com.censocustody.mobile.data.models.approval.ApprovalRequestDetails
import com.censocustody.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.censocustody.mobile.presentation.approvals.ApprovalContentHeader

@Composable
fun WalletCreationRowContent(
    walletCreation: ApprovalRequestDetails.WalletCreation
) {
    val header = walletCreation.getHeader(LocalContext.current)
    val accountName = walletCreation.accountInfo.name

    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = accountName.toWalletName(), fontSize = 20.sp)
    Spacer(modifier = Modifier.height(20.dp))
}