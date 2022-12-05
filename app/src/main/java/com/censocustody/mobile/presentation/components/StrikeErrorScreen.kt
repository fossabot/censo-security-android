package com.strikeprotocols.mobile.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeError
import com.strikeprotocols.mobile.presentation.key_management.PurpleGradientBackgroundUI
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun StrikeErrorScreen(
    errorResource: Resource.Error<Any>,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current

    val maintenanceMode = errorResource.strikeError is StrikeError.MaintenanceError

    val displayMessage = errorResource.strikeError?.getErrorMessage(context)
        ?: stringResource(id = R.string.something_went_wrong)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        PurpleGradientBackgroundUI()

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = displayMessage,
                color = StrikeWhite,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(36.dp))
            Button(
                modifier = Modifier
                    .width(width = 124.dp),
                onClick = onRetry
            ) {
                Text(
                    text = stringResource(id = R.string.retry),
                    fontWeight = FontWeight.SemiBold,
                    color = StrikeWhite,
                    fontSize = 16.sp
                )
            }
            if (!maintenanceMode) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        val getHelpIntent = Intent(Intent.ACTION_VIEW).apply {
                            data =
                                Uri.parse("https://help.strikeprotocols.com")
                        }
                        startActivity(context, getHelpIntent, null)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.get_help),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}