package com.strikeprotocols.mobile.presentation.backup_check

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.clickable
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun BackupCheckScreen(
    navController: NavController,
    packageManager: PackageManager
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable {
                navController.navigate(Screen.ApprovalListRoute.route) {
                    popUpTo(Screen.BackupCheckRoute.route) {
                        inclusive = true
                    }
                }
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.enabling_backup_settings),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6,
            color = StrikeWhite
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.head_to_your_device_backup_settings),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6,
            color = StrikeWhite
        )
        Spacer(modifier = Modifier.height(24.dp))

        var intent = Intent()
        if (isBackupSettingsActivityInstalled(packageManager)) {
            intent.component = ComponentName(
                BackupSettingsActivity.PACKAGE.value,
                BackupSettingsActivity.CLASS.value
            )
        } else {
            intent = Intent(Settings.ACTION_SETTINGS)
        }

        Button(onClick = {
            ContextCompat.startActivity(context, intent, null)
        }) {
            Text(text = stringResource(R.string.backup_deeplink_device_settings))
        }
    }
}

fun isBackupSettingsActivityInstalled(packageManager: PackageManager) : Boolean {
    val activityComponentName = ComponentName(
        BackupSettingsActivity.PACKAGE.value,
        BackupSettingsActivity.CLASS.value
    )

    return try {
        packageManager.getActivityInfo(activityComponentName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

enum class BackupSettingsActivity(val value: String) {
    PACKAGE("com.android.settings"),
    CLASS("com.android.settings.backup.BackupSettingsActivity")
}