package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.convertSecondsIntoReadableText
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.presentation.approvals.ApprovalContentHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getUITitle
import com.strikeprotocols.mobile.presentation.components.FactRow
import com.strikeprotocols.mobile.presentation.components.FactsData
import com.strikeprotocols.mobile.ui.theme.AccountTextGrey
import com.strikeprotocols.mobile.ui.theme.DetailInfoLightBackground
import com.strikeprotocols.mobile.ui.theme.GreyText

@Composable
fun BalanceAccountDetailContent(
    balanceAccountCreation: SolanaApprovalRequestType.BalanceAccountCreation
) {
    val header = balanceAccountCreation.getHeader(LocalContext.current)
    val accountName = balanceAccountCreation.accountInfo.name

    val approverRowInfoData = generateBalanceAccountDetailRows(
        balanceAccountCreation = balanceAccountCreation, context = LocalContext.current)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 32)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .background(color = DetailInfoLightBackground),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.wallet_name_title), color = GreyText, modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp))
            if (accountName.isNotEmpty()) {
                Text(text = accountName, color = AccountTextGrey, modifier = Modifier.padding(end = 12.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        for (approvalRow in approverRowInfoData) {
            FactRow(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                factsData = approvalRow
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

fun generateBalanceAccountDetailRows(balanceAccountCreation: SolanaApprovalRequestType.BalanceAccountCreation, context: Context) : List<FactsData>{
    val approverRowInfoData = mutableListOf<FactsData>()

    //region Approvers Row
    val approversList = mutableListOf<Pair<String, String>>()
    if (balanceAccountCreation.approvalPolicy.approvers.isNotEmpty()) {
        for (approver in balanceAccountCreation.approvalPolicy.approvers) {
            approversList.add(Pair(approver.value.name, approver.value.email))
        }
    } else {
        approversList.add(Pair(context.getString(R.string.no_approvers_text), ""))
    }

    val approverRow = FactsData(
        title = context.getString(R.string.wallet_approvers_title),
        facts = approversList
    )
    approverRowInfoData.add(approverRow)
    //endregion

    //region Approvals Required Rows
    val approvalsRequiredRow = FactsData(
        title = context.getString(R.string.approvals_required_title),
        facts = listOf(Pair(balanceAccountCreation.approvalPolicy.approvalsRequired.toInt().toString(), ""))
    )
    approverRowInfoData.add(approvalsRequiredRow)
    //endregion

    //region Approval Timeout Row
    val approvalTimeoutRow = FactsData(
        title = context.getString(R.string.approval_expiration),
        facts = listOf(
            Pair(convertSecondsIntoReadableText(balanceAccountCreation.approvalPolicy.approvalTimeout.toInt(), context), "")
        )
    )
    approverRowInfoData.add(approvalTimeoutRow)
    //endregion

    return approverRowInfoData
}