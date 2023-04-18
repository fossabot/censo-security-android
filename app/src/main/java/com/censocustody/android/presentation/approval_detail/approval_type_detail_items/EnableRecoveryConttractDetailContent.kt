package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.common.maskAddress
import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData

@Composable
fun EnableRecoveryContractDetailContent(
    details: ApprovalRequestDetailsV2.EnableRecoveryContract
) {
    val header = details.getHeader(LocalContext.current)
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    Spacer(modifier = Modifier.height(24.dp))
    val factsData = FactsData(
        facts = listOf(
            RowData.KeyValueRow(
                key = LocalContext.current.getString(R.string.recovery_threshold),
                value = details.recoveryThreshold.toString(),
            )
        ) + details.recoveryAddresses.mapIndexed { index, address ->
            RowData.KeyValueRow(
                key = "${LocalContext.current.getString(R.string.recovery_address)} #${index + 1}",
                value = address
            )
        }
    )
    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}