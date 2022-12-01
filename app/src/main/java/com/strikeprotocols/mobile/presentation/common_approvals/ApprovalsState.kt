package com.strikeprotocols.mobile.presentation.common_approvals

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequest
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import javax.crypto.Cipher

data class ApprovalsState(

    //list specific state
    val shouldShowErrorSnackbar: Boolean = false,
    val showPushNotificationsDialog: Resource<Unit> = Resource.Uninitialized,
    val approvalsResultRequest: Resource<List<ApprovalRequest?>> = Resource.Uninitialized,
    val approvals: List<ApprovalRequest?> = emptyList(),

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