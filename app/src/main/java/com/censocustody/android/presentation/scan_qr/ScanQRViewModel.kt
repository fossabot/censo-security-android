package com.censocustody.android.presentation.scan_qr

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.Resource
import com.censocustody.android.common.wrapper.CensoCountDownTimer
import com.censocustody.android.common.wrapper.CensoCountDownTimerImpl
import com.censocustody.android.data.models.AvailableDAppWallet
import com.censocustody.android.data.models.WalletConnectTopic
import com.censocustody.android.data.repository.ApprovalsRepository
import com.censocustody.android.presentation.scan_qr.ScanQRState.Companion.MAX_POLL
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
            state = state.copy(
                scanQRCodeResult = Resource.Success(uri),
                uri = uri
            )
            retrieveAvailableDAppVaults()
        }
    }

    private fun retrieveAvailableDAppVaults() {
        viewModelScope.launch {
            val availableDAppVaults = approvalsRepository.availableDAppVaults()
            val haveSingleWallet =
                availableDAppVaults.data?.vaults?.flatMap { it.wallets }?.size == 1

            if (haveSingleWallet) {
                val singleWallet = availableDAppVaults.data?.vaults?.flatMap { it.wallets }?.first()
                state = state.copy(selectedWallet = singleWallet)
                sendUriToBackend(
                    uri = state.uri,
                    walletAddress = singleWallet!!.walletAddress
                )
            } else {
                state = state.copy(
                    availableDAppVaultsResult = availableDAppVaults
                )
            }
        }
    }

    fun userSelectedWallet(wallet: AvailableDAppWallet) {
        state = state.copy(
            availableDAppVaultsResult = Resource.Uninitialized,
            selectedWallet = wallet
        )

        if (state.uri.isEmpty()) {
            missingURIData()
            return
        }

        viewModelScope.launch {
            sendUriToBackend(
                uri = state.uri,
                walletAddress = wallet.walletAddress
            )
        }
    }

    private fun missingURIData() {
        state = state.copy(scanQRCodeResult = Resource.Error())
    }

    private fun sendUriToBackend(uri: String, walletAddress: String) {
        viewModelScope.launch {
            val walletPairingResource = approvalsRepository.sendWcUri(uri = uri, walletAddress)

            if (walletPairingResource is Resource.Success) {
                state = state.copy(
                    topic = walletPairingResource.data!!.topic
                )
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
            if (state.timesPolled >= MAX_POLL) {
                state = state.copy(checkSessionsOnConnection = Resource.Error())
                timer.stopCountDownTimer()
                return@launch
            }

            if (state.topic.isEmpty()) {
                state = state.copy(scanQRCodeResult = Resource.Error())
                return@launch
            }

            val checkSessionsResource =
                approvalsRepository.checkIfConnectionHasSessions(state.topic)

            if (checkSessionsResource is Resource.Success) {

                if (dAppHasValidSession(checkSessionsResource.data)) {
                    timer.stopCountDownTimer()

                    state = state.copy(
                        checkSessionsOnConnection = checkSessionsResource
                    )
                } else {
                    state = state.copy(timesPolled = state.timesPolled + 1)
                }
            }
        }
    }

    private fun dAppHasValidSession(sessions: List<WalletConnectTopic>?) =
        sessions?.firstOrNull {
            it.status == WalletConnectTopic.ACTIVE
        } != null

    fun failedToScan(exception: Exception) {
        exception.printStackTrace()
        state = state.copy(scanQRCodeResult = Resource.Error(exception = exception))
    }

    fun retryScan() {
        state = state.copy(
            scanQRCodeResult = Resource.Loading(),
            uploadWcUri = Resource.Uninitialized,
            availableDAppVaultsResult = Resource.Uninitialized,
            checkSessionsOnConnection = Resource.Uninitialized,
            timesPolled = 0
        )
    }

    fun userFinished() {
        state = state.copy(exitScreen = true)
    }

    fun exitScreen() {
        state = ScanQRState()
    }
}