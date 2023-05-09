package com.censocustody.android.data.validator

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.censocustody.android.data.models.NameAndModel

interface NameAndModelProvider {
    fun retrieveNameAndModel(): NameAndModel
}

class AndroidNameAndModelProvider(private val applicationContext: Context) :
    NameAndModelProvider {

    override fun retrieveNameAndModel(): NameAndModel {
        return NameAndModel(
            name = Settings.Global.getString(applicationContext.contentResolver, Settings.Global.DEVICE_NAME),
            model = "Android ${Build.MANUFACTURER} - ${Build.DEVICE} (${Build.MODEL})"
        )
    }
}