package com.censocustody.android.presentation.approval_detail.approval_type_detail_items

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.presentation.approvals.ApprovalContentHeader
import com.censocustody.android.presentation.components.FactRow
import com.censocustody.android.presentation.components.FactsData
import com.censocustody.android.presentation.components.RowData
import com.censocustody.android.ui.theme.GreyText

@Composable
fun LoginApprovalDetailContent(header: String, name: String, email: String) {
    ApprovalContentHeader(header = header, topSpacing = 24, bottomSpacing = 8)
    Text(
        "",
        color = GreyText,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
    Spacer(modifier = Modifier.height(20.dp))
    //todo: Ask backend to send user image here. Login approvals will always be associated with a user.
    //todo: Get UI for this where we have multiple rows for same user.
    val factsData = FactsData(
        facts = listOf(
            RowData.KeyValueRow(
                key = stringResource(R.string.login_name),
                value = name,
            ),
            RowData.KeyValueRow(
                key = stringResource(R.string.login_email),
                value = email,
            )
        )
    )

    FactRow(factsData = factsData)
    Spacer(modifier = Modifier.height(28.dp))
}