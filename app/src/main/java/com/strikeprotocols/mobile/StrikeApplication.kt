package com.strikeprotocols.mobile

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.raygun.raygun4android.RaygunClient
import com.strikeprotocols.mobile.common.CrashReportingUtil
import com.strikeprotocols.mobile.data.SharedPrefsHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StrikeApplication : Application() {

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