package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.censocustody.android.ui.theme.BackgroundLight
import com.censocustody.android.ui.theme.DarkGreyText

@Composable
fun DAppEthSignDetailContent(header: String, fromAccount: String, fee: ApprovalRequestDetailsV2.Amount, dAppInfo: ApprovalRequestDetailsV2.DAppInfo, message: String) {
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
        )
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

        Row {
            Text(
                text = stringResource(R.string.message_to_sign),
                color = DarkGreyText,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
            )
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundLight)
            .padding(vertical = 14.dp),
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
@Preview
fun DAppEthSignDetailContentPreview() {
    DAppEthSignDetailContent(
        header = "DApp Transaction",
        fromAccount = "from wallet",
        fee = ApprovalRequestDetailsV2.Amount("0.00123", "0.0012300", "0.24"),
        dAppInfo = ApprovalRequestDetailsV2.DAppInfo("dApp Name", "dApp.url", "dApp Description", emptyList()),
        message = "0x68656c6c6f20776f726c64"
    )
}