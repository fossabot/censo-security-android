package com.censocustody.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.censocustody.mobile.R
import com.censocustody.mobile.data.models.approval.ApprovalRequestDetails
import com.censocustody.mobile.presentation.approval_detail.approval_type_detail_items.WithdrawalRequestDetailParameterProvider

@Composable
fun WithdrawalRequestRowContent(withdrawalRequest: ApprovalRequestDetails.WithdrawalRequest) {
    val symbolAndAmountInfo = withdrawalRequest.symbolAndAmountInfo

    val header = withdrawalRequest.getHeader(LocalContext.current)
    val subtitle = if (symbolAndAmountInfo.replacementFee == null) {
        symbolAndAmountInfo.getUSDEquivalentText(context = LocalContext.current, hideSymbol = true)
    } else {
        LocalContext.current.getString(R.string.bump_fee_request_approval_subtitle, symbolAndAmountInfo.amount, symbolAndAmountInfo.symbolInfo.symbol)
    }
    val fromAccount = withdrawalRequest.account.name
    val toAccount = withdrawalRequest.destination.name

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
    WithdrawalRequestRowContent(request)
}
