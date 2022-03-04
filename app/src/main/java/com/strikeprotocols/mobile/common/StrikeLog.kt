package com.strikeprotocols.mobile.common

import android.util.Log
import com.strikeprotocols.mobile.BuildConfig

fun strikeLog(tag: String = "StrikeMobile", message: String) {
    if (BuildConfig.DEBUG) { Log.d(tag, message) }
}
