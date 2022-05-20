package com.strikeprotocols.mobile.presentation.backup_check

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.backup_check.BackupSettingsActivity.Companion.BACKUP_CLASS
import com.strikeprotocols.mobile.presentation.backup_check.BackupSettingsActivity.Companion.PACKAGE
import com.strikeprotocols.mobile.presentation.backup_check.BackupSettingsActivity.Companion.USER_BACKUP_CLASS
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
            .padding(vertical = 16.dp, horizontal = 24.dp)
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
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = StrikeWhite
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.head_to_your_device_backup_settings),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = StrikeWhite
        )
        Spacer(modifier = Modifier.height(24.dp))

        var intent = Intent()
        if (isBackupSettingsActivityInstalled(packageManager, BACKUP_CLASS)) {
            intent.component = ComponentName(
                PACKAGE,
                BACKUP_CLASS
            )
        } else if (isBackupSettingsActivityInstalled(packageManager, USER_BACKUP_CLASS)) {
            intent.component = ComponentName(
                PACKAGE,
                USER_BACKUP_CLASS
            )
        } else {
            intent = Intent(Settings.ACTION_SETTINGS)
        }

        Button(
            modifier = Modifier.padding(16.dp),
            onClick = {
            ContextCompat.startActivity(context, intent, null)
        }) {
            Text(
                text = stringResource(R.string.backup_deeplink_device_settings),
                color = StrikeWhite,
                fontSize = 20.sp
            )
        }
    }
}

fun isBackupSettingsActivityInstalled(packageManager: PackageManager, className: String): Boolean {
    val activityComponentName = ComponentName(
        PACKAGE,
        className
    )

    return try {
        packageManager.getActivityInfo(activityComponentName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

class BackupSettingsActivity(val value: String) {
    companion object {
        const val PACKAGE = "com.android.settings"
        const val BACKUP_CLASS = "com.android.settings.backup.BackupSettingsActivity"
        const val USER_BACKUP_CLASS = "com.android.settings.backup.UserBackupSettingsActivity"
    }
}