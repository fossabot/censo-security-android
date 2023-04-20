package com.censocustody.android.presentation.scan_qr

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.censocustody.android.common.censoLog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScanQRViewModel @Inject constructor() : ViewModel() {

    var state by mutableStateOf(ScanQRState())
        private set

    fun receivedWalletConnectUri(uri: String) {
        censoLog(message = "The URI from Wallet Connect: $uri")
    }
}