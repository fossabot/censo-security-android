package com.censocustody.android.presentation.scan_qr

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.Resource
import com.censocustody.android.data.ApprovalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanQRViewModel @Inject constructor(
    private val approvalsRepository: ApprovalsRepository
) : ViewModel() {

    var state by mutableStateOf(ScanQRState())
        private set

    fun startScanning() {
        state = state.copy(
            scanQRCodeResult = Resource.Loading()
        )
    }

    fun receivedWalletConnectUri(uri: String?) {
        if (state.scanQRCodeResult is Resource.Loading && uri != null) {
            state = state.copy(scanQRCodeResult = Resource.Success(uri))
            sendUriToBackend(uri = uri)
        }
    }

    private fun sendUriToBackend(uri: String) {
        viewModelScope.launch {
            delay(500)
            val walletPairingResource = approvalsRepository.sendWcUri(uri = uri)

            state = state.copy(
                uploadWcUri = walletPairingResource
            )
        }
    }

    fun failedToScan(exception: Exception) {
        state = state.copy(scanQRCodeResult = Resource.Error(exception = exception))
    }

    fun retryScan() {
        state = state.copy(
            scanQRCodeResult = Resource.Loading(),
            uploadWcUri = Resource.Uninitialized,
        )
    }

    fun userFinished() {
        state = state.copy(exitScreen = true)
    }

    fun exitScreen() {
        state = ScanQRState()
    }
}