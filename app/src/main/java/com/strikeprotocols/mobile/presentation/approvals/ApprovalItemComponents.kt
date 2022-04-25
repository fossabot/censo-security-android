package com.strikeprotocols.mobile.presentation.approvals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.convertSecondsIntoCountdownText
import com.strikeprotocols.mobile.data.models.approval.AccountType
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.approval_type_items.*
import com.strikeprotocols.mobile.ui.theme.*
import java.util.*


@Composable
fun ApprovalItemHeader(
    timeRemainingInSeconds: Int,
    approvalImageVector: ImageVector,
    approvalImageContentDescription: String,
    approvalType: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBlack)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ApprovalType(
            modifier = Modifier.weight(7.5f),
            approvalImageVector = approvalImageVector,
            approvalImageContentDescription = approvalImageContentDescription,
            approvalType = approvalType
        )
        Text(
            modifier = Modifier.weight(2.5f),
            text = convertSecondsIntoCountdownText(timeRemainingInSeconds),
            textAlign = TextAlign.End,
            color = GreyText
        )
    }
}

@Composable
fun ApprovalButtonRow(
    onApproveClicked: () -> Unit,
    onMoreInfoClicked: () -> Unit
) {
    Column(modifier = Modifier.background(DetailInfoLightBackground)) {
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
                onClick = onApproveClicked) {
                Text(
                    stringResource(R.string.approve),
                    color = ApprovalGreen,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
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
                onClick = onMoreInfoClicked) {
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
    approvalImageContentDescription: String,
    approvalType: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(end = 2.dp),
            imageVector = approvalImageVector,
            tint = StrikeWhite,
            contentDescription = approvalImageContentDescription
        )
        Text(
            modifier = Modifier.padding(start = 2.dp),
            text = approvalType.uppercase(Locale.getDefault()),
            color = StrikeWhite
        )
    }
}

@Composable
fun ApprovalRowDetailContent(
    type: SolanaApprovalRequestType
) {
    when (type) {
        is SolanaApprovalRequestType.WithdrawalRequest ->
            WithdrawalRequestRowContent(withdrawalRequest = type)
        is SolanaApprovalRequestType.ConversionRequest ->
            ConversionRequestRowContent(conversionRequest = type)
        is SolanaApprovalRequestType.SignersUpdate ->
            SignersUpdateRowContent(signersUpdate = type)
        is SolanaApprovalRequestType.BalanceAccountCreation ->
            BalanceAccountCreationRowContent(balanceAccountCreation = type)
        is SolanaApprovalRequestType.DAppTransactionRequest ->
            DAppRowContent(dAppWalletApproval = type)
        is SolanaApprovalRequestType.LoginApprovalRequest ->
            LoginApprovalRowContent(loginApproval = type)
        else -> Text(text = stringResource(R.string.unknown_approval_item))
    }
}