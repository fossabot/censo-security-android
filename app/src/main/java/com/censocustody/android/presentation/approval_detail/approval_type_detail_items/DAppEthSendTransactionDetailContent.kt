package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.*
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData
import com.censocustody.android.ui.theme.DarkGreyText

@Composable
fun DAppEthSendTransactionDetailContent(
    header: String,
    fromAccount: String,
    fee: ApprovalRequestDetailsV2.Amount,
    dAppInfo: ApprovalRequestDetailsV2.DAppInfo,
    simulationResult: ApprovalRequestDetailsV2.EvmSimulationResult?
) {
//    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
//    ApprovalSubtitle(text = dAppInfo.name)
//    Spacer(modifier = Modifier.height(24.dp))

    val facts = listOf(
        RowData.KeyValueRow(
            key = stringResource(R.string.dapp_name),
            value = dAppInfo.name,
        ),
        RowData.KeyValueRow(
            key = stringResource(R.string.dapp_url),
            value = dAppInfo.url,
        ),
    )

    val feeFacts = listOf(
        RowData.KeyValueRow(
            key = stringResource(R.string.fee_estimate),
            value = fee.formattedUsdEquivalentWithSymbol()
        )
    )

    Column(
        modifier = Modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (simulationResult) {
            is ApprovalRequestDetailsV2.EvmSimulationResult.Failure -> {
                Text(
                    "${stringResource(R.string.simulation_failure)}: ${simulationResult.reason}",
                    color = DarkGreyText,
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                    letterSpacing = 0.25.sp
                )
            }
            is ApprovalRequestDetailsV2.EvmSimulationResult.Success -> {
                if (simulationResult.balanceChanges.isNotEmpty()) {
                    simulationResult.balanceChanges.forEach {
                        DAppTransferInfo(
                            header = if (it.amount.isNegative()) "Send ${it.symbolInfo.symbol}" else "Receive ${it.symbolInfo.symbol}",
                            subtitle = it.amount.absoluteValue(),
                            usdEquivalent = it.amount.formattedUsdEquivalentWithSymbol(),
                            fromText = fromAccount,
                            toText = dAppInfo.name,
                            directionIsForward = !it.amount.isNegative()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (simulationResult.tokenAllowances.isNotEmpty()) {
                    simulationResult.tokenAllowances.forEach {
                        DAppTransferInfo(
                            header = if (it.allowanceType == ApprovalRequestDetailsV2.TokenAllowanceType.REVOKE) "Revoke use of ${it.symbolInfo.symbol}" else "Allow use of ${it.symbolInfo.symbol}",
                            subtitle = it.displayAmount(),
                            usdEquivalent = if (it.allowanceType == ApprovalRequestDetailsV2.TokenAllowanceType.LIMITED) it.allowedAmount.formattedUsdEquivalentWithSymbol() else null,
                            fromText = fromAccount,
                            toText = dAppInfo.name,
                            directionIsForward = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            null -> {
                Text(
                    stringResource(R.string.no_simulation_results),
                    color = DarkGreyText,
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
                    letterSpacing = 0.25.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FactRow(
            factsData = FactsData(
                title = "DAPP INFO",
                facts = facts,
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        FactRow(
            factsData = FactsData(
                title = "FEES",
                facts = feeFacts,
            )
        )
    }

    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
fun DAppEthSendTransactionDetailContentPreview() {
    DAppEthSendTransactionDetailContent(
        header = "DApp Transaction",
        fromAccount = "from wallet",
        fee = ApprovalRequestDetailsV2.Amount("0.00123", "0.0012300", "0.24"),
        dAppInfo = ApprovalRequestDetailsV2.DAppInfo(
            "dApp Name",
            "dApp.url",
            "dApp Description",
            emptyList()
        ),
        simulationResult = ApprovalRequestDetailsV2.EvmSimulationResult.Success(
            listOf(
                ApprovalRequestDetailsV2.EvmSimulationResult.BalanceChange(
                    ApprovalRequestDetailsV2.Amount("1234000.000000", "1234000.000", "10.12"),
                    ApprovalRequestDetailsV2.EvmSymbolInfo("PEPE", "Pepe Token")
                ),
                ApprovalRequestDetailsV2.EvmSimulationResult.BalanceChange(
                    ApprovalRequestDetailsV2.Amount("-9.87", "-9.87", "-9.86"),
                    ApprovalRequestDetailsV2.EvmSymbolInfo("USDC", "USDC")
                ),
            ),
            listOf(
                ApprovalRequestDetailsV2.EvmSimulationResult.TokenAllowance(
                    ApprovalRequestDetailsV2.EvmSymbolInfo("USDC", "USDC"),
                    "allowed-address",
                    ApprovalRequestDetailsV2.Amount(
                        "123456789121314151617181920.00",
                        "1234567891011121314151617181920.00",
                        "1234567891011121314151617181920.00"
                    ),
                    ApprovalRequestDetailsV2.TokenAllowanceType.LIMITED
                )
            )
        )
    )
}

//@Composable
//@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
//fun DAppEthSendTransactionDetailContentPreview_SimFailure() {
//    DAppEthSendTransactionDetailContent(
//        header = "DApp Transaction",
//        fromAccount = "from wallet",
//        fee = ApprovalRequestDetailsV2.Amount("0.00123", "0.0012300", "0.24"),
//        dAppInfo = ApprovalRequestDetailsV2.DAppInfo("dApp Name", "dApp.url", "dApp Description", emptyList()),
//        simulationResult = ApprovalRequestDetailsV2.EvmSimulationResult.Failure(
//            reason = "execution reverted"
//        )
//    )
//}
