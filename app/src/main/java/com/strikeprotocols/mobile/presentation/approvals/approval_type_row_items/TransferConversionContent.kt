package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.components.ApprovalRowTitleText
import com.strikeprotocols.mobile.presentation.components.StrikeTagLabeledRow
import com.strikeprotocols.mobile.presentation.components.StrikeTagRow
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun TransferConversionContent(
    header: String,
    usdEquivalent: String,
    fromText: String,
    toText: String,
    transferConversionLabelData: TransferConversionLabelData = TransferConversionLabelData()
) {
    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 16)
    Text(
        text = usdEquivalent,
        color = GreyText,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(32.dp))
    if (transferConversionLabelData.showLabels) {
        StrikeTagLabeledRow(
            text1 = fromText,
            text2 = toText,
            label1 = transferConversionLabelData.label1,
            label2 = transferConversionLabelData.label2,
            subText2 = transferConversionLabelData.subText ?: "",
            arrowForward = true
        )
    } else {
        StrikeTagRow(
            text1 = fromText,
            text2 = toText,
            arrowForward = true
        )
    }
}

data class TransferConversionLabelData(
    val showLabels: Boolean = false,
    val label1: String = "",
    val label2: String = "",
    val subText: String? = ""
)