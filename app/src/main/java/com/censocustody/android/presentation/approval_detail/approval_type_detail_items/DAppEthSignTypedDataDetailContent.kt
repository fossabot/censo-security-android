package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.evm.EIP712Data
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.*
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData
import com.censocustody.android.ui.theme.BackgroundLight
import com.censocustody.android.ui.theme.DarkGreyText
import com.google.gson.JsonArray
import com.google.gson.JsonObject

@Composable
fun DAppEthSignTypedDataDetailContent(header: String, fromAccount: String, fee: ApprovalRequestDetailsV2.Amount, dAppInfo: ApprovalRequestDetailsV2.DAppInfo, data: EIP712Data) {
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
        Row {
            Text(
                text = stringResource(R.string.message_to_sign),
                color = DarkGreyText,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
            )
        }

        FactRow(factsData = FactsData(facts = facts + feeFacts))

        Row(modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundLight)
            .padding(vertical = 14.dp),
        ) {
            EIP712View(eip712Data = data)
            Text(
                text = data.toString(),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))
}

@Composable
fun EIP712Entry(eip712Data: EIP712Data, entry: EIP712Data.Entry) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${entry.name}:",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
        Text(
            text = if (eip712Data.hasType(entry.baseType)) { entry.type } else { entry.value.toString().trim('"') },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.Start
        )
    }
    if (eip712Data.hasType(entry.baseType)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(
            PaddingValues(start = 16.dp)
        )) {
            Column {
                if (entry.isArray) {
                    for (value in entry.value as JsonArray) {
                        for (subEntry in eip712Data.getEntriesForType(
                            value as JsonObject,
                            entry.baseType
                        )) {
                            EIP712Entry(eip712Data = eip712Data, entry = subEntry)
                        }
                    }
                } else {
                    for (subEntry in eip712Data.getEntriesForType(
                        entry.value as JsonObject,
                        entry.type
                    )) {
                        EIP712Entry(eip712Data = eip712Data, entry = subEntry)
                    }
                }
            }
        }
    }
}

@Composable
fun EIP712View(eip712Data: EIP712Data) {
    if (eip712Data.isValidEIP712) {
        Column(modifier = Modifier.padding(16.dp)) {
            eip712Data.getDomainName()?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Domain:",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }
            eip712Data.getDomainVerifyingContract()?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Contract:",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }
            for (entry in eip712Data.getMessageEntries()) {
                EIP712Entry(eip712Data = eip712Data, entry = entry)
            }
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(eip712Data.json)
        }
    }
}

@Composable
@Preview
fun DAppEthSignTypedDataDetailContentPreview() {
    DAppEthSignTypedDataDetailContent(
        header = "DApp Transaction",
        fromAccount = "from wallet",
        fee = ApprovalRequestDetailsV2.Amount("0.00123", "0.0012300", "0.24"),
        dAppInfo = ApprovalRequestDetailsV2.DAppInfo("dApp Name", "dApp.url", "dApp Description", emptyList()),
        data = ApprovalRequestDetailsV2.DAppParams.EthSignTypedData(
            "{\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallet\",\"type\":\"address\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person\"},{\"name\":\"contents\",\"type\":\"string\"}]},\"primaryType\":\"Mail\",\"domain\":{\"name\":\"Ether Mail\",\"version\":\"1\",\"chainId\":1,\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\"},\"message\":{\"from\":{\"name\":\"Cow\",\"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"},\"to\":{\"name\":\"Bob\",\"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"},\"contents\":\"Hello, Bob!\"}}", ""
        ).eip712Data()
    )
}

@Composable
@Preview
fun DAppEthSignTypedDataArrayDetailContentPreview() {
    DAppEthSignTypedDataDetailContent(
        header = "DApp Transaction",
        fromAccount = "from wallet",
        fee = ApprovalRequestDetailsV2.Amount("0.00123", "0.0012300", "0.24"),
        dAppInfo = ApprovalRequestDetailsV2.DAppInfo("dApp Name", "dApp.url", "dApp Description", emptyList()),
        data = ApprovalRequestDetailsV2.DAppParams.EthSignTypedData(
            "{\"types\":{\"MetaTransactionDataV2\":[{\"name\":\"signer\",\"type\":\"address\"},{\"name\":\"sender\",\"type\":\"address\"},{\"name\":\"expirationTimeSeconds\",\"type\":\"uint256\"},{\"name\":\"salt\",\"type\":\"uint256\"},{\"name\":\"callData\",\"type\":\"bytes\"},{\"name\":\"feeToken\",\"type\":\"address\"},{\"name\":\"fees\",\"type\":\"MetaTransactionFeeData[]\"}],\"MetaTransactionFeeData\":[{\"name\":\"recipient\",\"type\":\"address\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}]},\"domain\":{\"name\":\"ZeroEx\",\"version\":\"1.0.0\",\"chainId\":\"1\",\"verifyingContract\":\"0xdef1c0ded9bec7f1a1670819833240f027b25eff\"},\"primaryType\":\"MetaTransactionDataV2\",\"message\":{\"signer\":\"0xa8120f2ca4b3495d460df02122e90833e928da86\",\"sender\":\"0x0000000000000000000000000000000000000000\",\"expirationTimeSeconds\":\"1685998373\",\"salt\":\"10172818397776878889035290569885733773193256001012696978400198892263007391591\",\"callData\":\"0x7a1eb1b9000000000000000000000000b50721bcf8d\",\"feeToken\":\"0xb50721bcf8d664c30412cfbc6cf7a15145234ad1\",\"fees\":[{\"recipient\":\"0x38f5e5b4da37531a6e85161e337e0238bb27aa90\",\"amount\":\"17280505867230653594\"}]}}", ""
        ).eip712Data()
    )
}