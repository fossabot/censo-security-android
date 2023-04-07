package com.censocustody.android.presentation.common_approvals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.*
import com.censocustody.android.data.ApprovalsRepository
import com.censocustody.android.data.Shards
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.RegisterApprovalDisposition
import com.censocustody.android.data.models.Shard
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.presentation.approval_disposition.ApprovalDispositionState
import com.raygun.raygun4android.RaygunClient
import kotlinx.coroutines.launch

abstract class  CommonApprovalsViewModel(
    private val approvalsRepository: ApprovalsRepository,
    private val timer: CensoCountDownTimer
) : ViewModel() {

    //region abstract methods
    abstract fun setShouldDisplayConfirmDispositionDialog(
        approval: ApprovalRequestV2? = null,
        isApproving: Boolean,
        dialogMessages: Pair<String, String>
    )

    open fun handleInitialData(approval: ApprovalRequestV2) {}
    open fun handleEmptyInitialData() {}

    open fun handleScreenBackgrounded() {}
    open fun handleScreenForegrounded() {}
    //endregion

    var state by mutableStateOf(ApprovalsState())
        protected set

    fun onStart(approval: ApprovalRequestV2? = null) {
        if (state.selectedApproval == null) {
            approval?.let { handleInitialData(it) } ?: handleEmptyInitialData()
        }

        timer.startCountDownTimer(CensoCountDownTimerImpl.Companion.UPDATE_COUNTDOWN) {
            updateTimers()
        }
    }

    fun onStop() {
        timer.stopCountDownTimer()
    }

    private fun updateTimers() {
        state = state.copy(shouldRefreshTimers = !state.shouldRefreshTimers)
    }

    fun triggerBioPrompt() {
        viewModelScope.launch {
            val selectedApproval = state.selectedApproval

            if (selectedApproval == null) {
                state = state.copy(
                    approvalDispositionState =
                    errorRegisteringResult(state.approvalDispositionState)
                )
                return@launch
            }

            state = state.copy(bioPromptTrigger = Resource.Success(Unit))
        }
    }

    fun biometryApproved() {
        val selectedApproval = state.selectedApproval

        if (selectedApproval == null) {
            state = state.copy(
                approvalDispositionState =
                errorRegisteringResult(state.approvalDispositionState)
            )
            return
        }

        registerApprovalDisposition()
    }

    private fun registerApprovalDisposition() {
        viewModelScope.launch {
            state = state.copy(
                approvalDispositionState = state.approvalDispositionState?.copy(
                    registerApprovalDispositionResult = Resource.Loading()
                )
            )

            val approvalDispositionState = retrieveApprovalDispositionFromAPI(
                approvalDispositionState = state.approvalDispositionState,
                approval = state.selectedApproval,
                approvalsRepository = approvalsRepository,
            )

            state = state.copy(approvalDispositionState = approvalDispositionState)
        }
    }

    private suspend fun retrieveApprovalDispositionFromAPI(
        approvalDispositionState: ApprovalDispositionState?,
        approval: ApprovalRequestV2?,
        approvalsRepository: ApprovalsRepository,
    ): ApprovalDispositionState? {

        val approvalId = approval?.id ?: ""
        val approvalRequestDetails = approval?.details
            ?: return errorRegisteringResult(approvalDispositionState)

        val recentApprovalDisposition = approvalDispositionState?.approvalDisposition
        val approvalDisposition =
            if (recentApprovalDisposition is Resource.Success) recentApprovalDisposition.data else null
                ?: return errorRegisteringResult(approvalDispositionState)


        val registerApprovalDisposition = RegisterApprovalDisposition(
            approvalDisposition = approvalDisposition,
            approvalRequestType = approvalRequestDetails,
        )

        val shards = try {
            retrieveNecessaryShards(
                requestDetails = approvalRequestDetails
            )
        } catch (e: Exception) {
            RaygunClient.send(
                e,
                listOf(
                    CrashReportingUtil.APPROVAL_DISPOSITION,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG,
                    CrashReportingUtil.RETRIEVE_SHARDS
                )
            )
            return approvalDispositionState.copy(
                registerApprovalDispositionResult = Resource.Error(exception = e)
            )
        }

        val approvalDispositionResponseResource =
            approvalsRepository.approveOrDenyDisposition(
                requestId = approvalId,
                registerApprovalDisposition = registerApprovalDisposition,
                shards = Shards(shards)
            )

        if (approvalDispositionResponseResource is Resource.Error) {
            RaygunClient.send(
                approvalDispositionResponseResource.exception
                    ?: Exception("Approval Disposition Failed"),
                listOf(
                    CrashReportingUtil.APPROVAL_DISPOSITION,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG,
                )
            )
        }

        return approvalDispositionState.copy(
            registerApprovalDispositionResult = approvalDispositionResponseResource
        )

    }

    private suspend fun retrieveNecessaryShards(
        requestDetails: ApprovalRequestDetailsV2?
    ): List<Shard>? {
        when (requestDetails) {
            is ApprovalRequestDetailsV2.AddDevice -> {

                if (requestDetails.currentShardingPolicyRevisionGuid == null) return null

                val shardResponse = approvalsRepository.retrieveShards(
                    policyRevisionId = requestDetails.currentShardingPolicyRevisionGuid,
                    userId = requestDetails.email.toShareUserId()
                )

                if (shardResponse is Resource.Success) {
                    return shardResponse.data?.shards
                } else {
                    throw shardResponse.exception ?: Exception("Failed to retrieve add device shards")
                }
            }

            is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate -> {
                val shardResponse = approvalsRepository.retrieveShards(
                    policyRevisionId = requestDetails.shardingPolicyChangeInfo.currentPolicyRevisionGuid,
                )

                if (shardResponse is Resource.Success) {
                    return shardResponse.data?.shards
                } else {
                    throw shardResponse.exception ?: Exception("Failed to retrieve org policy shards")
                }
            }

            else -> {
                return null
            }
        }
    }

    private fun errorRegisteringResult(
        approvalDispositionState: ApprovalDispositionState?,
    ): ApprovalDispositionState? {
        return approvalDispositionState?.copy(
            registerApprovalDispositionResult = Resource.Error()
        )
    }

    fun dismissApprovalDispositionError() {
        resetApprovalDispositionState()
    }

    fun getDialogDetailsAndApprovalDispositionType(
        isApproving: Boolean,
        dialogMessages: Pair<String, String>
    ) : DialogDetailsAndDispositionType {
        val dialogDetails = getDialogDetails(
            isApproving = isApproving, dialogMessages = dialogMessages
        )

        val approvalDisposition = getApprovalDispositionType(isApproving)

        return DialogDetailsAndDispositionType(dialogDetails, approvalDisposition)
    }

    private fun getDialogDetails(isApproving: Boolean, dialogMessages: Pair<String, String>) =
        ConfirmDispositionDialogDetails(
            shouldDisplay = true,
            isApproving = isApproving,
            dialogMessages = dialogMessages
        )

    private fun getApprovalDispositionType(isApproving: Boolean) =
        if (isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY

    //region reset data
    fun resetApprovalDispositionState() {
        state = state.copy(
            approvalDispositionState = ApprovalDispositionState()
        )
    }

    fun resetPromptTrigger() {
        state = state.copy(bioPromptTrigger = Resource.Uninitialized)
    }

    fun resetShouldDisplayConfirmDisposition() {
        state = state.copy(shouldDisplayConfirmDisposition = null)
    }
    //endregion
}

data class DialogDetailsAndDispositionType(
    val dialogDetails: ConfirmDispositionDialogDetails,
    val approvalDisposition: ApprovalDisposition
)