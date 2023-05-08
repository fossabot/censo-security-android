package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.*
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData

@Composable
fun DAppEthSendTransactionDetailContent(header: String, fromAccount: String, fee: ApprovalRequestDetailsV2.Amount, dAppInfo: ApprovalRequestDetailsV2.DAppInfo, simulationResults: List<ApprovalRequestDetailsV2.EvmSimulatedChange>) {
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    ApprovalSubtitle(text = dAppInfo.name)
    Spacer(modifier = Modifier.height(24.dp))

    val facts = listOf(
        RowData.KeyValueRow(
            key = stringResource(R.string.from_wallet),
            value = fromAccount,
        ),
        RowData.KeyValueRow(
            key = stringResource(R.string.dapp_name),
            value = dAppInfo.name,
        ),
        RowData.KeyValueRow(
            key = stringResource(R.string.dapp_url),
            value = dAppInfo.url,
        ),
        RowData.KeyValueRow(
            key = stringResource(R.string.dapp_description),
            value = dAppInfo.description,
        ),
    )

    val feeFacts = listOf(
        RowData.KeyValueRow(
            key = stringResource(R.string.fee_estimate),
            value = fee.formattedUsdEquivalentWithSymbol()
        )
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FactRow(factsData = FactsData(facts = facts + feeFacts))

        if (simulationResults.isEmpty()) {
            Text(stringResource(R.string.no_simulation_results))
        } else {
            FactRow(factsData =
            FactsData(
                title = stringResource(R.string.simulation_results),
                facts = simulationResults.map { change ->
                    RowData.KeyValueRow(
                        key = change.symbolInfo.symbol,
                        value = formattedAmount(change.amount.value)
                    )
                }
            )
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
@Preview
fun DAppEthSendTransactionDetailContentPreview() {
    DAppEthSendTransactionDetailContent(
        header = "DApp Transaction",
        fromAccount = "from wallet",
        fee = ApprovalRequestDetailsV2.Amount("0.00123", "0.0012300", "0.24"),
        dAppInfo = ApprovalRequestDetailsV2.DAppInfo("dApp Name", "dApp.url", "dApp Description", emptyList()),
        simulationResults = listOf(
            ApprovalRequestDetailsV2.EvmSimulatedChange(
                ApprovalRequestDetailsV2.Amount("1234000.000000", "1234000.000", "10.12"),
                ApprovalRequestDetailsV2.EvmSymbolInfo("PEPE", "Pepe Token")
            ),
            ApprovalRequestDetailsV2.EvmSimulatedChange(
                ApprovalRequestDetailsV2.Amount("-9.87", "-9.87", "-9.86"),
                ApprovalRequestDetailsV2.EvmSymbolInfo("USDC", "USDC")
            ),
        )
    )
}