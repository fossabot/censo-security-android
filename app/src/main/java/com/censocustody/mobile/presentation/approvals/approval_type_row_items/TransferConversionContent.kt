package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.data.models.approval.SymbolAndAmountInfo
import com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items.ApprovalSubtitle
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.components.StrikeTagRow
import com.strikeprotocols.mobile.ui.theme.MoneyGreen
import com.strikeprotocols.mobile.ui.theme.MoneyRed
import com.strikeprotocols.mobile.ui.theme.SubtitleGrey

@Composable
fun TransferConversionContent(
    header: String,
    subtitle: String,
    fromText: String,
    toText: String
) {
    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    ApprovalSubtitle(text = subtitle)
    Spacer(modifier = Modifier.height(32.dp))
    StrikeTagRow(
        text1 = fromText,
        text2 = toText,
        arrowForward = true
    )
}


@Composable
fun TransactionContent(
    header: String,
    amountText: String,
    usdEquivalent: String,
    fromText: String,
    toText: String,
    positiveWithdrawal: Boolean
) {
    ApprovalContentHeader(header = header)
    Text(
        text = amountText,
        color = if (positiveWithdrawal) MoneyGreen else MoneyRed,
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
        letterSpacing = 0.23.sp
    )
    Text(
        text = usdEquivalent,
        color = SubtitleGrey,
        textAlign = TextAlign.Center,
        fontSize = 14.sp,
        letterSpacing = 0.23.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    StrikeTagRow(
        text1 = fromText,
        text2 = toText,
        arrowForward = positiveWithdrawal
    )
}

@Composable
fun BalanceChangeRowItem(
    dappName: String,
    accountName: String,
    symbolAndAmountInfo: SymbolAndAmountInfo
) {
    return TransactionContent(
        header = symbolAndAmountInfo.symbolInfo.symbol,
        amountText = symbolAndAmountInfo.amount,
        usdEquivalent = symbolAndAmountInfo.formattedUSDEquivalent(),
        fromText = dappName,
        toText = accountName,
        positiveWithdrawal = symbolAndAmountInfo.isAmountPositive()
    )
}