package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.common.maskAddress
import com.censocustody.android.data.models.Chain
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData

@Composable
fun CreateOrDeleteAddressBookEntryDetailContent(
    header: String,
    chain: Chain,
    entryName: String,
    entryAddress: String,
) {
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    Spacer(modifier = Modifier.height(24.dp))
    val factsData = FactsData(
        facts = listOf(
            RowData.KeyValueRow(
                key = stringResource(R.string.name),
                value = entryName,
            ),
            RowData.KeyValueRow(
                key = stringResource(R.string.address),
                value = entryAddress.maskAddress(),
            ),
            RowData.KeyValueRow(
                key = stringResource(R.string.chain),
                value = chain.label(),
            ),
        )
    )
    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}