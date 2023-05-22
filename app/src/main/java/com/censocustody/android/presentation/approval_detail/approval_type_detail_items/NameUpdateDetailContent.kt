package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.RenameType
import com.censocustody.android.presentation.approvals.approval_type_row_items.buildFromToDisplayText
import com.censocustody.android.presentation.approvals.approval_type_row_items.formattedUsdEquivalentWithSymbol
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData

@Composable
fun NameUpdateDetailContent(header: String, oldName: String, newName: String, renameType: RenameType,
                            chainFees: List<ApprovalRequestDetailsV2.ChainFee>? = null) {
    val fromToText = buildFromToDisplayText(from = oldName, to = newName, LocalContext.current, renameType)

    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 36)
    ApprovalSubtitle(text = fromToText, fontSize = 20.sp)
    if (chainFees != null && chainFees.isNotEmpty()) {
        FactsData(
            title = LocalContext.current.getString(R.string.fees),
            facts = chainFees.map {
                RowData.KeyValueRow(
                    key = "${it.chain.label()} ${LocalContext.current.getString(R.string.fee_estimate)}",
                    value = it.fee.formattedUsdEquivalentWithSymbol()
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(28.dp))
}