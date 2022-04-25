package com.strikeprotocols.mobile.presentation.approvals.approval_type_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.presentation.components.ApprovalRowTitleText
import com.strikeprotocols.mobile.presentation.components.StrikeTagRow
import com.strikeprotocols.mobile.ui.theme.GreyText
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun TransferConversionContent(
    mainValue: String,
    usdEquivalent: String,
    fromText: String,
    toText: String
) {
    Spacer(modifier = Modifier.height(8.dp))
    ApprovalRowTitleText(title = mainValue)
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = usdEquivalent,
        color = GreyText,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(32.dp))
    StrikeTagRow(
        text1 = fromText,
        text2 = toText,
        arrowForward = true
    )
}