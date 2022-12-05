package com.censocustody.mobile

import android.app.Application
import com.google.firebase.FirebaseApp
import com.raygun.raygun4android.RaygunClient
import com.censocustody.mobile.common.CrashReportingUtil
import com.censocustody.mobile.data.SharedPrefsHelper
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
            RaygunClient.send(
                e,
                listOf(
                    CrashReportingUtil.MANUALLY_REPORTED_TAG,
                    CrashReportingUtil.PUSH_NOTIFICATION_TAG
                )
            )
        }
    }
}