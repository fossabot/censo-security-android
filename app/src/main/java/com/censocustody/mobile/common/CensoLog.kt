package com.censocustody.mobile.common

import android.util.Log
import com.censocustody.mobile.BuildConfig

fun censoLog(tag: String = "CensoCustody", message: String) {
    if (BuildConfig.DEBUG) { Log.d(tag, message) }
}
