package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.convertApprovalsNeededToDisplayMessage
import com.strikeprotocols.mobile.presentation.components.StrikeTag
import com.strikeprotocols.mobile.presentation.components.StrikeTransactionCurrency
import com.strikeprotocols.mobile.ui.theme.*

@Composable
fun ApprovalDetailsTransferContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StrikeTransactionCurrency(
            cryptoValue = "2.000000000 SOL",
            currencyEquivalentValue = "177.54 USD",
            contentSpacerHeight = 12,
            fontSize = 32
        )
        Spacer(modifier = Modifier.height(36.dp))

        ApprovalTransferInfo()
        Spacer(modifier = Modifier.height(36.dp))

        ApprovalDispositionsRequired(approvalsNeeded = 2)
        Spacer(modifier = Modifier.height(12.dp))

        ApprovalInfoRow(
            backgroundColor = DetailInfoLightBackground,
            title = stringResource(R.string.requested_by),
            value = "test@tester.org"
        )
        Divider(modifier = Modifier.height(0.5.dp), color = DividerGrey)
        ApprovalInfoRow(
            backgroundColor = DetailInfoDarkBackground,
            title = stringResource(R.string.requested_date),
            value = "Mar 16, 2022 at 3:08:38pm"
        )
        Divider(modifier = Modifier.height(0.5.dp), color = DividerGrey)
        ApprovalInfoRow(
            backgroundColor = DetailInfoLightBackground,
            title = stringResource(R.string.address),
            //Keep this character encoding for the bullet symbol
            value = "\u2022•••••••••••CXT65D7aw9"
        )
    }
}

@Composable
fun ApprovalTransferInfo() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundBlack),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TagColumn(
            title = "From",
            tag = "Transfers",
            paddingValues = PaddingValues(start = 16.dp)
        )
        Icon(
            modifier = Modifier
                .padding(top = 36.dp)
                .size(16.dp),
            imageVector = Icons.Rounded.ArrowForward,
            contentDescription = stringResource(id = R.string.content_des_transfer_icon),
            tint = GreyText
        )
        TagColumn(
            title = "To",
            tag = "Coinbase",
            paddingValues = PaddingValues(end = 16.dp)
        )
    }
}

@Composable
fun TagColumn(
    title: String,
    tag: String,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier.padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, textAlign = TextAlign.Center, color = StrikeWhite, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(14.dp))
        StrikeTag(
            text = tag,
            paddingValues = PaddingValues(top = 24.dp, bottom = 24.dp, start = 50.dp, end = 50.dp),
            backgroundColor = DetailInfoDarkBackground
        )
    }
}

@Composable
fun ApprovalDispositionsRequired(approvalsNeeded: Int) {
    Text(
        text = approvalsNeeded.convertApprovalsNeededToDisplayMessage(context = LocalContext.current),
        color = GreyText,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
}

@Composable
fun ApprovalInfoRow(
    backgroundColor: Color,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = title,
            color = GreyText,
            fontSize = 16.sp
        )

        Text(
            modifier = Modifier.padding(end = 16.dp),
            text = value,
            color = StrikeWhite,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ApprovalInfoRowPreview() {
    Column {
        ApprovalInfoRow(
            backgroundColor = DetailInfoLightBackground,
            title = stringResource(R.string.requested_by),
            value = "test@tester.org"
        )
        Divider(modifier = Modifier.height(1.dp), color = DividerGrey)
        ApprovalInfoRow(
            backgroundColor = DetailInfoDarkBackground,
            title = stringResource(R.string.requested_date),
            value = "Mar 16, 2022 at 3:08:38pm"
        )
        Divider(modifier = Modifier.height(1.dp), color = DividerGrey)
        ApprovalInfoRow(
            backgroundColor = DetailInfoLightBackground,
            title = stringResource(R.string.address),
            //Keep this character encoding for the bullet symbol
            value = "\u2022•••••••••••CXT65D7aw9"
        )
    }
}

//region Preview
@Preview(showBackground = true)
@Composable
fun ApprovalDetailsTransferContentPreview() {
    ApprovalDetailsTransferContent()
}

//@Preview(showBackground = true)
@Composable
fun TagColumnPreview() {
    TagColumn("", "", PaddingValues(16.dp))
}
//endregion