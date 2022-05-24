package com.strikeprotocols.mobile.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.convertPublicKeyToDisplayText
import com.strikeprotocols.mobile.common.formatISO8601IntoDisplayText
import com.strikeprotocols.mobile.data.models.approval.SlotUpdateType
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getHeader
import com.strikeprotocols.mobile.presentation.components.AccountChangeItem
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.DetailInfoDarkBackground
import com.strikeprotocols.mobile.ui.theme.DetailInfoLightBackground
import com.strikeprotocols.mobile.ui.theme.DividerGrey

@Composable
fun SignersUpdateDetailContent(
    approval: WalletApproval,
    signersUpdate: SolanaApprovalRequestType.SignersUpdate,
    approvalsNeeded: Int
) {
    val header = signersUpdate.getHeader(LocalContext.current)
    val value = signersUpdate.signer.value
    val name = value.name
    val email = value.email

    val publicKey = value.publicKey
    val requestedByEmail = approval.submitterEmail ?: stringResource(id = R.string.requested_by_email_na)
    val requestedDate = approval.submitDate ?: stringResource(id = R.string.requested_by_date_na)

    Column(
        modifier = Modifier.background(BackgroundBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AccountChangeItem(header = header, title = name, subtitle = email)
        Spacer(modifier = Modifier.height(18.dp))//+6.dp from the bottom of the AccountChangeItem
        ApprovalDispositionsRequired(approvalsNeeded = approvalsNeeded)
        Spacer(modifier = Modifier.height(18.dp))

        ApprovalInfoRow(
            backgroundColor = DetailInfoLightBackground,
            title = stringResource(R.string.public_key),
            value = publicKey.convertPublicKeyToDisplayText()
        )
        Divider(modifier = Modifier.height(0.5.dp), color = DividerGrey)
        ApprovalInfoRow(
            backgroundColor = DetailInfoDarkBackground,
            title = stringResource(R.string.requested_by),
            value = requestedByEmail
        )
        Divider(modifier = Modifier.height(0.5.dp), color = DividerGrey)
        ApprovalInfoRow(
            backgroundColor = DetailInfoLightBackground,
            title = stringResource(R.string.requested_date),
            value = requestedDate.formatISO8601IntoDisplayText(LocalContext.current)
        )
        Spacer(modifier = Modifier.height(36.dp))
    }
}