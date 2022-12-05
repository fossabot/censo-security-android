package com.censocustody.mobile.common

import android.util.Log
import com.censocustody.mobile.BuildConfig

fun strikeLog(tag: String = "StrikeMobile", message: String) {
    if (BuildConfig.DEBUG) { Log.d(tag, message) }
}
