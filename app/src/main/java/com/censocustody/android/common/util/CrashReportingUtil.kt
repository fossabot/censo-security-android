package com.censocustody.android.common.util

import com.censocustody.android.common.util.CrashReportingUtil.MANUALLY_REPORTED_TAG
import com.raygun.raygun4android.RaygunClient

object CrashReportingUtil {
    const val BROADCAST_RECEIVER_TAG = "BroadcastReceiver"
    const val PUSH_NOTIFICATION_TAG = "PushNotification"
    const val PUSH_NOTIFICATION_PERMISSION_TAG = "PushNotificationPermission"
    const val MANUALLY_REPORTED_TAG = "ManualReport"
    const val FORCE_UPGRADE_TAG = "ForceUpgrade"
    const val IMAGE = "Image"
    const val JWT_TAG = "JWT"
    const val KEY_INVALIDATED = "KeyInvalidated"
    const val APPROVAL_DISPOSITION = "ApprovalDisposition"
    const val RETRIEVE_SHARDS = "RetrieveShards"
    const val RECOVER_KEY = "RecoverKey"
    const val KEY_CREATION = "KeyCreation"
    const val DEVICE_REGISTRATION = "DeviceRegistration"
    const val QR_CODE = "QR Code"
    const val RETRIEVE_NAME_MODEL = "Retrieve Name Model"
}

fun Exception.sendError(reason: String) {
    RaygunClient.send(this, listOf(MANUALLY_REPORTED_TAG, reason))
}