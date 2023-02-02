package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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