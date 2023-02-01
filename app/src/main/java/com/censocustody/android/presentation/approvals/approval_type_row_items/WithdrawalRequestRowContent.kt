package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.presentation.approval_detail.approval_type_detail_items.WithdrawalRequestDetailParameterProvider

@Composable
fun WithdrawalRequestRowContent(
    header: String, subtitle: String, fromAccount: String, toAccount: String
) {
    TransferConversionContent(
        header = header,
        subtitle = subtitle,
        fromText = fromAccount,
        toText = toAccount
    )
    Spacer(modifier = Modifier.height(20.dp))
}

@Preview
@Composable
fun WithdrawalRequestRowContentPreview(@PreviewParameter(WithdrawalRequestDetailParameterProvider::class) request: ApprovalRequestDetails.WithdrawalRequest) {
    WithdrawalRequestRowContent(
        header = "Header",
        subtitle = "Subtitle",
        fromAccount = "Main Account",
        toAccount = "Other Account"
    )
}
