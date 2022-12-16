package com.censocustody.android.presentation.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.censocustody.android.R
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.ui.theme.UnfocusedGrey

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