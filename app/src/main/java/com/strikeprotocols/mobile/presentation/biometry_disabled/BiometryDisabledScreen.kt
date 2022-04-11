package com.strikeprotocols.mobile.presentation.biometry_disabled

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun BiometryDisabledScreen(message: String, biometryAvailable: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6,
            color = StrikeWhite
        )
        if (biometryAvailable) {
            val context = LocalContext.current

            val intent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                else Intent(Settings.ACTION_SECURITY_SETTINGS)

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                startActivity(context, intent, null)
            }) {
                Text(text = stringResource(R.string.biometry_deeplink_device_settings))
            }
        }
    }
}