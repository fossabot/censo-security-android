package com.censocustody.android.presentation.biometry_disabled

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
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
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.R
import com.censocustody.android.ui.theme.TextBlack

@Composable
fun BiometryDisabledScreen(message: String, biometryAvailable: Boolean) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            text = message,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Center,
            color = TextBlack
        )
        if (biometryAvailable) {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)

            Spacer(modifier = Modifier.height(32.dp))
            CensoButton(
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 4.dp),
                onClick = {
                    try {
                        startActivity(context, intent, null)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.update_biometric_settings),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.biometry_deeplink_device_settings),
                    fontSize = 18.sp,
                    color = CensoWhite
                )
            }
        }
    }
}