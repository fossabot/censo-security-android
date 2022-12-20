package com.censocustody.android.presentation.common_approvals

import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.approval.ApprovalRequest
import com.censocustody.android.presentation.approval_disposition.ApprovalDispositionState
import com.censocustody.android.presentation.durable_nonce.DurableNonceViewModel
import javax.crypto.Cipher

data class ApprovalsState(

    //list specific state
    val shouldShowErrorSnackbar: Boolean = false,
    val approvalsResultRequest: Resource<List<ApprovalRequest?>> = Resource.Uninitialized,
    val approvals: List<ApprovalRequest?> = emptyList(),
    val showPushNotificationsDialog: Resource<Unit> = Resource.Uninitialized,

    //detail specific state
    val screenWasBackgrounded: Boolean = false,
    val shouldKickOutUserToApprovalsScreen: Boolean = false,

    //common state
    val shouldRefreshTimers: Boolean = false,
    val approvalDispositionState: ApprovalDispositionState? = ApprovalDispositionState(),
    val multipleAccounts: DurableNonceViewModel.MultipleAccounts? = null,
    val shouldDisplayConfirmDisposition: ConfirmDispositionDialogDetails? = null,
    val bioPromptTrigger: Resource<Cipher> = Resource.Uninitialized,
    val selectedApproval: ApprovalRequest? = null,
) {
    val loadingData = approvalsResultRequest is Resource.Loading ||
            approvalDispositionState?.loadingData == true
}

data class ConfirmDispositionDialogDetails(
    val shouldDisplay: Boolean = false,
    val isApproving: Boolean,
    val dialogMessages: Pair<String, String>
)