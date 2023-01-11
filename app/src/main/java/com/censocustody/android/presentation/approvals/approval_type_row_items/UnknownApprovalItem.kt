package com.censocustody.android.presentation.approvals.approval_type_row_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.presentation.approvals.ApprovalItemHeader
import com.censocustody.android.ui.theme.DividerGrey
import com.censocustody.android.ui.theme.HeaderBlack
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.R

@Composable
fun UnknownApprovalItem(
    timeRemainingInSeconds: Long?,
    onUpdateAppClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .background(color = HeaderBlack),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ApprovalItemHeader(
            timeRemainingInSeconds = timeRemainingInSeconds,
            vaultName = null
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            text = stringResource(R.string.unknown_approval_tip),
            textAlign = TextAlign.Center,
            color = CensoWhite,
            fontSize = 16.sp
        )

        UnknownApprovalButtonRow(onUpdateAppClicked)
    }
}

@Composable
fun UnknownApprovalButtonRow(
    onUpdateAppClicked: () -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = DividerGrey, modifier = Modifier.height(0.5.dp))
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onUpdateAppClicked
            ) {
                Text(
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    text = stringResource(R.string.update_censo),
                    textAlign = TextAlign.Center,
                    color = CensoWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}