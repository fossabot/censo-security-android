package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.components.ApprovalRowTitleText
import com.strikeprotocols.mobile.presentation.components.StrikeTagRow
import com.strikeprotocols.mobile.ui.theme.*
import java.util.*


@Composable
fun DAppRowContent(dAppWalletApproval: SolanaApprovalRequestType.DAppTransactionRequest) {
    Column(
        modifier = Modifier.background(DetailInfoLightBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ApprovalRowTitleText(title = dAppWalletApproval.dappInfo.name)

        if (!dAppWalletApproval.balanceChanges.isNullOrEmpty()) {
            dAppWalletApproval.balanceChanges.forEachIndexed { index, symbolAndAmountInfo ->
                Spacer(modifier = Modifier.height(16.dp))
                //todo: check the values grabbed here are correct.
                BalanceChange(
                    symbol = symbolAndAmountInfo.symbolInfo.symbol,
                    amount = symbolAndAmountInfo.formattedAmount(),
                    usdEquivalent = symbolAndAmountInfo.formattedUSDEquivalent(false),
                    fromDestination = dAppWalletApproval.account.name,
                    toDestination = dAppWalletApproval.dappInfo.name,
                    positiveChange = symbolAndAmountInfo.isAmountPositive()
                )

                //If we are not at the last index position
                // then we want to add extra space to the bottom of the BalanceChange composable
                if (index != dAppWalletApproval.balanceChanges.size - 1 ) {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun BalanceChange(
    symbol: String,
    amount: String,
    usdEquivalent: String?,
    fromDestination: String,
    toDestination: String,
    positiveChange: Boolean
) {
    CryptoSymbolAndAmount(
        symbol = symbol,
        amount = amount,
        positiveChange = positiveChange,
        usdEquivalent = usdEquivalent
    )
    Spacer(modifier = Modifier.height(16.dp))
    StrikeTagRow(
        text1 = fromDestination,
        text2 = toDestination,
        arrowForward = positiveChange
    )
}

@Composable
fun CryptoSymbolAndAmount(
    symbol: String,
    amount: String,
    positiveChange: Boolean,
    usdEquivalent: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = symbol.uppercase(Locale.getDefault()),
            color = StrikeWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        //Red or Green depending on if the amount is Pos or Neg
        val amountColor = if (positiveChange) MoneyGreen else MoneyRed

        Text(
            text = amount,
            color = amountColor,
            fontSize = 16.sp
        )

        Text(
            text = usdEquivalent ?: stringResource(R.string.usd_equivalent_na),
            color = GreyText,
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CryptoSymbolAndAmountPreview() {
    CryptoSymbolAndAmount(
        symbol = "SOL",
        amount = "1.45",
        usdEquivalent = "$14.5",
        positiveChange = false
    )
}