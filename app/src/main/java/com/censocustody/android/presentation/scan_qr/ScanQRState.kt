package com.censocustody.android.presentation.scan_qr

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.AvailableDAppVaults
import com.censocustody.android.data.models.AvailableDAppWallet
import com.censocustody.android.data.models.WalletConnectPairingResponse
import com.censocustody.android.data.models.WalletConnectTopic
import okhttp3.ResponseBody

data class ScanQRState(
    val scanQRCodeResult: Resource<String> = Resource.Loading(),
    val uploadWcUri: Resource<WalletConnectPairingResponse> = Resource.Uninitialized,
    val checkSessionsOnConnection: Resource<List<WalletConnectTopic>> = Resource.Uninitialized,
    val uri: String = "",
    val exitScreen: Boolean = false,
    val topic: String = "",
    val timesPolled: Int = 0,
    val availableDAppVaultsResult: Resource<AvailableDAppVaults> = Resource.Uninitialized,
    val selectedWallet: AvailableDAppWallet? = null
) {
    companion object {
        const val MAX_POLL = 5
    }
}