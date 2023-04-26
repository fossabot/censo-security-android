package com.censocustody.android.presentation.scan_qr

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.Resource
import com.censocustody.android.common.wrapper.CensoCountDownTimer
import com.censocustody.android.common.wrapper.CensoCountDownTimerImpl
import com.censocustody.android.data.repository.ApprovalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanQRViewModel @Inject constructor(
    private val approvalsRepository: ApprovalsRepository,
    private val timer: CensoCountDownTimer
) : ViewModel() {

    var state by mutableStateOf(ScanQRState())
        private set

    fun onStop() {
        timer.stopCountDownTimer()
    }

    fun receivedWalletConnectUri(uri: String?) {
        if (state.scanQRCodeResult is Resource.Loading && !uri.isNullOrEmpty()) {
            state = state.copy(scanQRCodeResult = Resource.Success(uri))
            sendUriToBackend(uri = uri)
        }
    }

    private fun sendUriToBackend(uri: String) {
        viewModelScope.launch {
            val walletPairingResource = approvalsRepository.sendWcUri(uri = uri)

            if (walletPairingResource is Resource.Success) {
                startPolling()
            }

            state = state.copy(
                uploadWcUri = walletPairingResource
            )
        }
    }

    private fun startPolling() {
        state = state.copy(checkSessionsOnConnection = Resource.Loading())
        timer.startCountDownTimer(CensoCountDownTimerImpl.Companion.UPDATE_COUNTDOWN) {
            checkSessions()
        }
    }

    private fun checkSessions() {
        viewModelScope.launch {
            val checkSessionsResource = approvalsRepository.checkIfConnectionHasSessions()

            if (checkSessionsResource is Resource.Success) {
                timer.stopCountDownTimer()

                state = state.copy(
                    checkSessionsOnConnection = checkSessionsResource
                )
            }
        }
    }

    fun failedToScan(exception: Exception) {
        exception.printStackTrace()
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