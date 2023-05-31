package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.components.CensoTagRow
import com.censocustody.android.ui.theme.DarkGreyText
import com.censocustody.android.ui.theme.TextBlack

@Composable
fun DAppTransferInfo(
    header: String,
    subtitle: String?,
    usdEquivalent: String?,
    fromText: String,
    toText: String,
    directionIsForward: Boolean
) {
    ApprovalContentHeader(header = header, topSpacing = 16, bottomSpacing = 0)
    if (subtitle != null) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = subtitle,
            color = TextBlack,
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            letterSpacing = 0.23.sp
        )
    }

    if (usdEquivalent != null) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = usdEquivalent,
            color = DarkGreyText,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            letterSpacing = 0.23.sp
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    CensoTagRow(
        text1 = fromText,
        text2 = toText,
        arrowForward = directionIsForward
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFF)
fun DAppTransferInfoPreview() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DAppTransferInfo(
            header = "Receive PEPE",
            subtitle = "1.23",
            usdEquivalent = "2.34 USD Equivalent",
            fromText = "Main",
            toText = "Sample DApp",
            directionIsForward = false
        )
    }
}