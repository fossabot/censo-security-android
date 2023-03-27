package com.censocustody.android.common

import android.util.Log
import com.censocustody.android.BuildConfig

fun censoLog(tag: String = "WaterBottle", message: String) {
    if (BuildConfig.DEBUG) { Log.d(tag, message) }
}
