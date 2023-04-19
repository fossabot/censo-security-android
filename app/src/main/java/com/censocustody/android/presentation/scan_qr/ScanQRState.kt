package com.censocustody.android.presentation.scan_qr

import com.censocustody.android.common.Resource

data class ScanQRState(
    val scanQRCodeResult: Resource<Unit> = Resource.Uninitialized,
)