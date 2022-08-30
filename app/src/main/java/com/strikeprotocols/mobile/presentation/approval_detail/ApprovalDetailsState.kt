package com.strikeprotocols.mobile.presentation.approval_detail

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import javax.crypto.Cipher

data class ApprovalDetailsState(
    val shouldKickOutUserToApprovalsScreen: Boolean = false,
    val shouldDisplayConfirmDisposition: ConfirmDispositionDialogDetails? = null,
    val screenWasBackgrounded: Boolean = false,
    val approval: WalletApproval? = null,
    val multipleAccounts: DurableNonceViewModel.MultipleAccounts? = null,
    val bioPromptTrigger: Resource<Cipher> = Resource.Uninitialized,
    val remainingTimeInSeconds: Long? = null,
    val approvalDispositionState: ApprovalDispositionState? = ApprovalDispositionState()
) {
    val loadingData = approvalDispositionState?.loadingData == true

    val approvalTimeoutInSeconds = approval?.approvalTimeoutInSeconds
    val submitDate = approval?.submitDate
}

data class ConfirmDispositionDialogDetails(
    val shouldDisplay: Boolean = false,
    val isApproving: Boolean,
    val dialogTitle: String,
    val dialogText: String
)