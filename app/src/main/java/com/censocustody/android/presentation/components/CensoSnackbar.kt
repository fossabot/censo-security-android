package com.censocustody.android.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.censocustody.android.ui.theme.SnackbarBackground
import com.censocustody.android.ui.theme.CensoWhite

@Composable
fun CensoSnackbar(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(Alignment.Bottom),
        hostState = snackbarHostState,
        snackbar = { data ->
            Snackbar(
                modifier = Modifier
                    .padding(16.dp),
                backgroundColor = SnackbarBackground,
                contentColor = CensoWhite,
                content = {
                    Text(
                        text = data.message,
                        style = MaterialTheme.typography.body2
                    )
                }
            )
        }
    )
}