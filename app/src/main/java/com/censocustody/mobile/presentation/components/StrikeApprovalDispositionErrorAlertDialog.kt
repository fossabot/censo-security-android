package com.censocustody.mobile.presentation.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.censocustody.mobile.R
import com.censocustody.mobile.ui.theme.CensoWhite
import com.censocustody.mobile.ui.theme.UnfocusedGrey

@Composable
fun CensoApprovalDispositionErrorAlertDialog(
    dialogTitle: String,
    dialogText: String,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        backgroundColor = UnfocusedGrey,
        onDismissRequest = onConfirm,
        title = {
            Text(
                text = dialogTitle,
                color = CensoWhite,
                fontSize = 24.sp
            )
        },
        text = {
            Text(
                text = dialogText,
                color = CensoWhite,
                fontSize = 18.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.ok))
            }
        }
    )
}