package com.strikeprotocols.mobile

import android.app.Application
import com.strikeprotocols.mobile.data.SharedPrefsHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StrikeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPrefsHelper.setup(this)
    }
}