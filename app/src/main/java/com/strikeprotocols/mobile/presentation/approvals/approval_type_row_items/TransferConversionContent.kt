package com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
    toText: String
) {
    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 8)
    Text(
        usdEquivalent,
        color = GreyText,
        textAlign = TextAlign.Center,
        fontSize = 12.sp,
        letterSpacing = 0.23.sp
    )
    Spacer(modifier = Modifier.height(32.dp))
    StrikeTagRow(
        text1 = fromText,
        text2 = toText,
        arrowForward = true
    )
}

data class TransferConversionLabelData(
    val showLabels: Boolean = false,
    val label1: String = "",
    val label2: String = "",
    val subText: String? = ""
)