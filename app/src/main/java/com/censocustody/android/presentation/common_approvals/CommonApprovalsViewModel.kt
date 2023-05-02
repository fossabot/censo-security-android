package com.censocustody.android.presentation.common_approvals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.*
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.util.sendError
import com.censocustody.android.common.wrapper.toShareUserId
import com.censocustody.android.data.repository.ApprovalsRepository
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.RegisterApprovalDisposition
import com.censocustody.android.data.models.Shard
import com.censocustody.android.data.models.Shards
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.presentation.approval_disposition.ApprovalDispositionState
import com.raygun.raygun4android.RaygunClient
import kotlinx.coroutines.launch

abstract class  CommonApprovalsViewModel(
    private val approvalsRepository: ApprovalsRepository,
    private val keyRepository: KeyRepository,
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

        val recoveryShards = try {
            retrieveRecoveryShards(
                requestDetails = approvalRequestDetails
            )
        } catch (e: Exception) {
            e.sendError(CrashReportingUtil.RETRIEVE_SHARDS)
            return approvalDispositionState.copy(
                registerApprovalDispositionResult = Resource.Error(exception = e)
            )
        }

        val reshareShards = try {
            retrieveReshareShards(
                requestDetails = approvalRequestDetails
            )
        } catch (e: Exception) {
            e.sendError(CrashReportingUtil.RETRIEVE_SHARDS)
            return approvalDispositionState.copy(
                registerApprovalDispositionResult = Resource.Error(exception = e)
            )
        }

        val approvalDispositionResponseResource =
            approvalsRepository.approveOrDenyDisposition(
                requestId = approvalId,
                registerApprovalDisposition = registerApprovalDisposition,
                recoveryShards = Shards(recoveryShards),
                reshareShards = Shards(reshareShards)
            )

        if (approvalDispositionResponseResource is Resource.Error) {
            (approvalDispositionResponseResource.exception
                ?: Exception("Approval Disposition Failed"))
                .sendError(CrashReportingUtil.APPROVAL_DISPOSITION)
        }

        removeBootstrapDeviceIfNeeded(
            registerApproval = registerApprovalDisposition,
            approvalDispositionResponseResource = approvalDispositionResponseResource
        )

        return approvalDispositionState.copy(
            registerApprovalDispositionResult = approvalDispositionResponseResource
        )

    }

    private suspend fun removeBootstrapDeviceIfNeeded(
        registerApproval: RegisterApprovalDisposition,
        approvalDispositionResponseResource: Resource<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body>
    ) {
        if (approvalDispositionResponseResource is Resource.Success &&
            registerApproval.approvalRequestType is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate
        ) {
            keyRepository.removeBootstrapDeviceData()
        }
    }
    private suspend fun retrieveRecoveryShards(
        requestDetails: ApprovalRequestDetailsV2?
    ): List<Shard>? {
        when (requestDetails) {
            is ApprovalRequestDetailsV2.EnableDevice -> {

                if (requestDetails.currentShardingPolicyRevisionGuid == null) return null

                val shardResponse = approvalsRepository.retrieveShards(
                    policyRevisionId = requestDetails.currentShardingPolicyRevisionGuid,
                    userId = requestDetails.email.toShareUserId()
                )

                if (shardResponse is Resource.Success) {
                    return shardResponse.data?.shards
                } else {
                    throw shardResponse.exception ?: Exception("Failed to retrieve recovery shards")
                }
            }
            else -> {
                return null
            }
        }
    }

    private suspend fun retrieveReshareShards(
        requestDetails: ApprovalRequestDetailsV2?
    ): List<Shard>? {
        val policyId = when (requestDetails) {
            is ApprovalRequestDetailsV2.EnableDevice ->
                requestDetails.targetShardingPolicy?.let {
                    requestDetails.currentShardingPolicyRevisionGuid
                }

            is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate ->
                requestDetails.shardingPolicyChangeInfo.currentPolicyRevisionGuid

            else -> null
        } ?: return null

        val shardResponse = approvalsRepository.retrieveShards(
            policyRevisionId = policyId,
        )

        if (shardResponse is Resource.Success) {
            return shardResponse.data?.shards
        } else {
            throw shardResponse.exception ?: Exception("Failed to retrieve re-share shards")
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