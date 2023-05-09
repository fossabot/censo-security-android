package com.censocustody.android.data.validator

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.util.sendError
import com.censocustody.android.data.models.NameAndModel

interface NameAndModelProvider {
    fun retrieveNameAndModel(): NameAndModel
}

class AndroidNameAndModelProvider(private val applicationContext: Context) :
    NameAndModelProvider {

    override fun retrieveNameAndModel(): NameAndModel {
        return try {
            NameAndModel(
                name = Settings.Global.getString(
                    applicationContext.contentResolver,
                    Settings.Global.DEVICE_NAME
                ),
                model = "${Build.MANUFACTURER} - ${Build.DEVICE} (${Build.MODEL})"
            )
        } catch (e: Exception) {
            e.sendError(CrashReportingUtil.RETRIEVE_NAME_MODEL)
            NameAndModel("", "")
        }
    }
}