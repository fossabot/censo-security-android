package com.censocustody.android.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.censocustody.android.common.CensoButton
import com.censocustody.android.common.Resource
import com.censocustody.android.common.CensoError
import com.censocustody.android.R
import com.censocustody.android.ui.theme.*

@Composable
fun CensoErrorScreen(
    errorResource: Resource.Error<Any>,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current

    val maintenanceMode = errorResource.censoError is CensoError.MaintenanceError

    val displayMessage = errorResource.censoError?.getErrorMessage(context)
        ?: stringResource(id = R.string.something_went_wrong)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = displayMessage,
                color = TextBlack,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(36.dp))
            CensoButton(
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                onClick = onRetry
            ) {
                Text(
                    text = stringResource(id = R.string.retry),
                    fontWeight = FontWeight.SemiBold,
                    color = CensoWhite,
                    fontSize = 16.sp
                )
            }
            if (!maintenanceMode) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        val getHelpIntent = Intent(Intent.ACTION_VIEW).apply {
                            data =
                                Uri.parse("https://help.censocustody.com")
                        }
                        startActivity(context, getHelpIntent, null)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.get_help),
                        color = CensoTextBlue,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}