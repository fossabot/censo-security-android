package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.censocustody.android.R
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.approvals.approval_type_row_items.getHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData
import com.censocustody.android.ui.theme.BackgroundBlack

@Composable
fun SignersUpdateDetailContent(signersUpdate: ApprovalRequestDetails.SignersUpdate) {
    val header = signersUpdate.getHeader(LocalContext.current)
    val value = signersUpdate.signer.value
    val name = value.name
    val email = value.email

    Column(
        modifier = Modifier.background(BackgroundBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ApprovalContentHeader(header = header, topSpacing = 24)
        Spacer(modifier = Modifier.height(24.dp))
        //todo: Get UI for this where we have multiple rows for same user
        val factsData = FactsData(
            facts =
            listOf(
                RowData(
                    title = stringResource(R.string.signer_name),
                    value = name,
                    userImage = value.jpegThumbnail,
                    userRow = false
                ),
                RowData(
                    title = stringResource(R.string.signer_email),
                    value = email,
                    userImage = value.jpegThumbnail,
                    userRow = false
                )
            )
        )
        FactRow(factsData = factsData)
    }
    Spacer(modifier = Modifier.height(28.dp))
}