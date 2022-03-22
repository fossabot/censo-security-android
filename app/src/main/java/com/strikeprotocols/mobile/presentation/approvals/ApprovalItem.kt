package com.strikeprotocols.mobile.presentation.approvals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.presentation.components.StrikeTag
import com.strikeprotocols.mobile.ui.theme.*
import java.util.*

@Composable
fun ApprovalItem(
    onApproveClicked: () -> Unit,
    onMoreInfoClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .background(color = HeaderBlack),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ApprovalItemHeader()
        ApprovalContent()
        ApprovalButtonRow(
            onApproveClicked = { onApproveClicked() },
            onMoreInfoClicked = { onMoreInfoClicked() }
        )
    }
}

@Composable
fun ApprovalItemHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBlack)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ApprovalType(approvalImageVector = Icons.Filled.SyncAlt, approvalType = "Transfer")
        Text(
            text = "23:59:54",
            color = GreyText
        )
    }
}

@Composable
fun ApprovalContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(16.dp))
        Text(
            "2.000000000 SOL",
            color = StrikeWhite, fontSize = 24.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "177.54 USD ${stringResource(id = R.string.equivalent)}",
            textAlign = TextAlign.Center,
            color = GreyText,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StrikeTag(text = "Transfers")
            Icon(
                modifier = Modifier
                    .padding(end = 2.dp, start = 2.dp)
                    .size(20.dp),
                imageVector = Icons.Filled.ArrowForward,
                tint = StrikeWhite,
                contentDescription = stringResource(id = R.string.content_des_transfer_icon)
            )
            StrikeTag(text = "Coinbase")
        }
    }
}

@Composable
fun ApprovalButtonRow(
    onApproveClicked: () -> Unit,
    onMoreInfoClicked: () -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = DividerGrey, modifier = Modifier.height(0.5.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp, bottom = 4.dp),
                onClick = { onApproveClicked() }) {
                Text(
                    stringResource(R.string.approve),
                    color = ApprovalGreen,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
            }
            Divider(
                color = DividerGrey,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(0.5.dp)
            )
            TextButton(
                modifier = Modifier
                    .weight(1f),
                onClick = { onMoreInfoClicked() }) {
                Text(
                    stringResource(R.string.more_info),
                    color = StrikeWhite,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
            }
        }
    }
}


@Composable
fun ApprovalType(
    approvalImageVector: ImageVector,
    approvalType: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(end = 2.dp),
            imageVector = approvalImageVector,
            tint = StrikeWhite,
            contentDescription = stringResource(id = R.string.content_des_transfer_icon)
        )
        Text(
            modifier = Modifier.padding(start = 2.dp),
            text = approvalType.uppercase(Locale.getDefault()),
            color = StrikeWhite
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ApprovalItemPreview() {
    ApprovalItem(
        onApproveClicked = { strikeLog(message = "Approve clicked")},
        onMoreInfoClicked = { strikeLog(message = "More info clicked") }
    )
}