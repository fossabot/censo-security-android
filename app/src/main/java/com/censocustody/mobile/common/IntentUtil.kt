package com.censocustody.mobile.common

import android.content.Intent
import android.content.pm.PackageManager

const val LAST_PASS = "com.lastpass.lpandroid"
const val ONE_PASSWORD = "com.agilebits.onepassword"

fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun getIntentForPackage(packageManager: PackageManager, packageName: String) : Intent? {
    return packageManager.getLaunchIntentForPackage(
        packageName
    )
}