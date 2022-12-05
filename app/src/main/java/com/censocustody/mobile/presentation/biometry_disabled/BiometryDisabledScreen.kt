package com.censocustody.mobile.presentation.biometry_disabled

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
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
import com.censocustody.mobile.R
import com.censocustody.mobile.ui.theme.CensoWhite

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
            color = CensoWhite
        )
        if (biometryAvailable) {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)

            Spacer(modifier = Modifier.height(32.dp))
            Button(
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
                    fontSize = 20.sp,
                    color = CensoWhite
                )
            }
        }
    }
}