package com.censocustody.android

import android.app.Application
import com.google.firebase.FirebaseApp
import com.raygun.raygun4android.RaygunClient
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.util.sendError
import com.censocustody.android.data.storage.SharedPrefsHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CensoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPrefsHelper.setup(this)
        setupFirebase()
    }

    private fun setupFirebase() {
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.sendError(CrashReportingUtil.PUSH_NOTIFICATION_TAG)
        }
    }
}