package com.strikeprotocols.mobile.presentation.approval_detail

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.models.*
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetailsViewModel.Companion.UPDATE_COUNTDOWN
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.blockhash.BlockHashViewModel.BlockHash
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalDetailsViewModel @Inject constructor(
    private val approvalsRepository: ApprovalsRepository
) : ViewModel() {

    private var timer: CountDownTimer? = null

    var state by mutableStateOf(ApprovalDetailsState())
        private set

    fun onStart(approval: WalletApproval?) {
        if (state.approval == null) {
            approval?.let { setArgsToState(it) }
        }

        startCountDown()
    }

    fun onStop() {
        timer?.cancel()
    }

    private fun setArgsToState(approval: WalletApproval) {
        state = state.copy(approval = approval)
    }

    fun setShouldDisplayConfirmDispositionDialog(
        isApproving: Boolean,
        dialogTitle: String,
        dialogText: String
    ) {
        val dialogDetails = ConfirmDispositionDialogDetails(
            shouldDisplay = true,
            isApproving = isApproving,
            dialogTitle = dialogTitle,
            dialogText = dialogText
        )

        val approvalDisposition =
            if(isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY

        state = state.copy(
            shouldDisplayConfirmDispositionDialog = dialogDetails,
            approvalDispositionState = state.approvalDispositionState?.copy(
                approvalDisposition = Resource.Success(approvalDisposition)
            )
        )
    }

    fun resetShouldDisplayConfirmDispositionDialog() {
        state = state.copy(shouldDisplayConfirmDispositionDialog = null)
    }

    private fun startCountDown() {
        timer = object : CountDownTimer(Long.MAX_VALUE, UPDATE_COUNTDOWN) {
            override fun onTick(millisecs: Long) {
                countdownApprovalOneSecond()
            }

            override fun onFinish() {}
        }
        timer?.start()
    }

    fun countdownApprovalOneSecond() {
        val approval = state.approval
        approval?.approvalTimeoutInSeconds?.let { safeApprovalTimeoutInSeconds ->
            if (safeApprovalTimeoutInSeconds > 0) {
                val countdownApproval =
                    approval.copy(approvalTimeoutInSeconds = approval.approvalTimeoutInSeconds - 1)
                state = state.copy(approval = countdownApproval)
            } else if (safeApprovalTimeoutInSeconds == 0) {
                onStop()
            }
        }
    }

    fun resetShouldKickOutUser() {
        state = state.copy(
            shouldKickOutUserToApprovalsScreen = false
        )
    }

    fun wipeDataAndKickUserOutToApprovalsScreen() {
        resetDispositionState()
        state = state.copy(
            shouldKickOutUserToApprovalsScreen = true
        )
    }

    fun resetDispositionState() {
        state = state.copy(
            approvalDispositionState = ApprovalDispositionState()
        )
    }

    fun setBlockHash(blockHash: BlockHash) {
        state = state.copy(blockHash = blockHash)
        if (state.blockHash != null) {
            registerApprovalDisposition()
        }
    }

    fun resetBlockHash() {
        state = state.copy(blockHash = null)
    }

    //region API calls
    private suspend fun signData() {
        state = state.copy(
            approvalDispositionState = state.approvalDispositionState?.copy(
                signingDataResult = Resource.Loading()
            )
        )
        delay(250)
        state = state.copy(
            approvalDispositionState = state.approvalDispositionState?.copy(
                signingDataResult = Resource.Success("I am signed data")
            )
        )
    }

    fun registerApprovalDisposition() {
        viewModelScope.launch {
            state = state.copy(
                approvalDispositionState = state.approvalDispositionState?.copy(
                    registerApprovalDispositionResult = Resource.Loading()
                )
            )
            try {
                //Data retrieval and checks
                val recentBlockHash = state.blockHash?.blockHashString
                if (recentBlockHash == null) {
                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            approvalDispositionError = ApprovalDispositionError.BLOCKHASH_FAILURE,
                            registerApprovalDispositionResult = Resource.Error(ApprovalDispositionError.BLOCKHASH_FAILURE.error)
                        )
                    )
                    return@launch
                }

                signData()
                val signResult = state.approvalDispositionState?.signingDataResult
                val signature = if (signResult is Resource.Success) signResult.data else null
                if (signature == null) {
                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            approvalDispositionError = ApprovalDispositionError.SIGNING_DATA_FAILURE,
                            registerApprovalDispositionResult = Resource.Error(ApprovalDispositionError.SIGNING_DATA_FAILURE.error)
                        )
                    )
                    return@launch
                }

                val recentApprovalDisposition = state.approvalDispositionState?.approvalDisposition
                val approvalDisposition = if (recentApprovalDisposition is Resource.Success) recentApprovalDisposition.data else null
                if (approvalDisposition == null) {
                    state = state.copy(
                        approvalDispositionState = state.approvalDispositionState?.copy(
                            approvalDispositionError = ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE,
                            registerApprovalDispositionResult = Resource.Error(ApprovalDispositionError.APPROVAL_DISPOSITION_FAILURE.error)
                        )
                    )
                    return@launch
                }

                val registerApprovalDisposition = RegisterApprovalDisposition(
                    approvalDisposition = approvalDisposition,
                    signature = signature,
                    recentBlockhash = recentBlockHash
                )

                val approvalDispositionResponse =
                    approvalsRepository.approveOrDenyDisposition(
                        state.approval?.id, registerApprovalDisposition
                    )
                state = state.copy(
                    approvalDispositionState = state.approvalDispositionState?.copy(
                        registerApprovalDispositionResult = Resource.Success(
                            approvalDispositionResponse
                        )
                    )
                )
            } catch (e: java.lang.Exception) {
                state = state.copy(
                    approvalDispositionState = state.approvalDispositionState?.copy(
                        approvalDispositionError = ApprovalDispositionError.SUBMIT_FAILURE,
                        registerApprovalDispositionResult = Resource.Error(e.message ?: "")
                    )
                )
            }
        }
    }
    //endregion

    object Companion {
        const val UPDATE_COUNTDOWN = 1000L
    }
}

