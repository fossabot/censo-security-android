package com.censocustody.android.presentation.common_approvals

import androidx.biometric.BiometricPrompt
import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.approval.ApprovalRequest
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.presentation.approval_disposition.ApprovalDispositionState
import com.censocustody.android.presentation.durable_nonce.DurableNonceViewModel

data class ApprovalsState(

    //list specific state
    val shouldShowErrorSnackbar: Boolean = false,
    val approvalsResultRequest: Resource<List<ApprovalRequestV2?>> = Resource.Uninitialized,
    val approvals: List<ApprovalRequestV2?> = emptyList(),
    val showPushNotificationsDialog: Resource<Unit> = Resource.Uninitialized,

    //detail specific state
    val screenWasBackgrounded: Boolean = false,
    val shouldKickOutUserToApprovalsScreen: Boolean = false,

    //common state
    val shouldRefreshTimers: Boolean = false,
    val approvalDispositionState: ApprovalDispositionState? = ApprovalDispositionState(),
    val multipleAccounts: DurableNonceViewModel.MultipleAccounts? = null,
    val shouldDisplayConfirmDisposition: ConfirmDispositionDialogDetails? = null,
    val bioPromptTrigger: Resource<BiometricPrompt.CryptoObject> = Resource.Uninitialized,
    val selectedApproval: ApprovalRequestV2? = null,
) {
    val loadingData = approvalsResultRequest is Resource.Loading ||
            approvalDispositionState?.loadingData == true
}

data class ConfirmDispositionDialogDetails(
    val shouldDisplay: Boolean = false,
    val isApproving: Boolean,
    val dialogMessages: Pair<String, String>
)