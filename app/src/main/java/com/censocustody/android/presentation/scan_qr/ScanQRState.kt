package com.censocustody.android.presentation.scan_qr

import com.censocustody.android.common.Resource
import okhttp3.ResponseBody

data class ScanQRState(
    val scanQRCodeResult: Resource<String> = Resource.Loading(),
    val uploadWcUri: Resource<ResponseBody> = Resource.Uninitialized,
    val checkSessionsOnConnection: Resource<ResponseBody> = Resource.Uninitialized,
    val exitScreen: Boolean = false
)