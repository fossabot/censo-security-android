package com.censocustody.android.presentation.scan_qr

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.WalletConnectTopic
import okhttp3.ResponseBody

data class ScanQRState(
    val scanQRCodeResult: Resource<String> = Resource.Loading(),
    val uploadWcUri: Resource<ResponseBody> = Resource.Uninitialized,
    val checkSessionsOnConnection: Resource<List<WalletConnectTopic>> = Resource.Uninitialized,
    val exitScreen: Boolean = false,
    val topic: String = "",
    val timesPolled: Int = 0
) {
    companion object {
        const val MAX_POLL = 5
    }
}