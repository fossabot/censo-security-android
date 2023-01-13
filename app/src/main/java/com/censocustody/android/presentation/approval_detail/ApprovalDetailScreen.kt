package com.censocustody.android.presentation.approval_detail

import android.annotation.SuppressLint
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.*
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approval.ApprovalDispositionRequest
import com.censocustody.android.data.models.approval.InitiationRequest
import com.censocustody.android.data.models.approval.ApprovalRequestDetails
import com.censocustody.android.data.models.approval.ApprovalRequest
import com.censocustody.android.presentation.approvals.ApprovalsViewModel
import com.censocustody.android.ui.theme.*
import com.censocustody.android.data.models.approval.ApprovalRequestDetails.*
import com.censocustody.android.presentation.approvals.ApprovalDetailContent
import com.censocustody.android.presentation.approvals.approval_type_row_items.getApprovalTimerText
import com.censocustody.android.presentation.approvals.approval_type_row_items.getDialogMessages
import com.censocustody.android.presentation.components.*
import com.censocustody.android.presentation.durable_nonce.DurableNonceViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ApprovalDetailsScreen(
    navController: NavController,
    approvalDetailsViewModel: ApprovalDetailsViewModel = hiltViewModel(),
    durableNonceViewModel: DurableNonceViewModel = hiltViewModel(),
    approval: ApprovalRequest?
) {
    val approvalDetailsState = approvalDetailsViewModel.state
    val durableNonceState = durableNonceViewModel.state

    fun resetDataAfterErrorDismissed() {
        approvalDetailsViewModel.dismissApprovalDispositionError()
        durableNonceViewModel.resetMultipleAccountsResource()
        approvalDetailsViewModel.resetMultipleAccounts()
    }

    val context = LocalContext.current as FragmentActivity

    val promptInfo = BioCryptoUtil.createPromptInfo(context = context)

    fun launchNonceWork() {
        val nonceAddresses = approvalDetailsState.selectedApproval?.retrieveAccountAddresses()
        val minimumSlotAddress = approvalDetailsState.selectedApproval?.retrieveAccountAddressesSlot()
        if (nonceAddresses != null && minimumSlotAddress != null) {
            durableNonceViewModel.setInitialData(
                nonceAccountAddresses = nonceAddresses,
                minimumNonceAccountAddressesSlot = minimumSlotAddress
            )
        } else {
            approvalDetailsViewModel.wipeDataAndKickUserOutToApprovalsScreen()
        }
    }

    fun retryApprovalDisposition(isApproving: Boolean, isInitiationRequest: Boolean) {
        approvalDetailsViewModel.dismissApprovalDispositionError()
        durableNonceViewModel.resetMultipleAccountsResource()
        approvalDetailsViewModel.resetMultipleAccounts()
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = isApproving,
            isInitiationRequest = isInitiationRequest,
            dialogMessages = approval?.getApprovalRequestType()?.getDialogMessages(
                context = context,
                approvalDisposition = if (isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
                isInitiationRequest = isInitiationRequest
            )
                ?: UnknownApprovalType.getDialogMessages(
                    context = context,
                    approvalDisposition = if (isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
                    isInitiationRequest = isInitiationRequest
                )
        )
    }

    DisposableEffect(key1 = approvalDetailsViewModel) {
        approvalDetailsViewModel.onStart(approval)
        onDispose { approvalDetailsViewModel.onStop() }
    }

    LaunchedEffect(key1 = approvalDetailsState, key2 = durableNonceState) {
        if (durableNonceState.multipleAccountsResult is Resource.Success) {
            approvalDetailsViewModel.setMultipleAccounts(durableNonceState.multipleAccounts)
            durableNonceViewModel.resetState()
        }

        if (approvalDetailsState.bioPromptTrigger is Resource.Success) {
            approvalDetailsState.bioPromptTrigger.data?.let {
                val bioPrompt = BioCryptoUtil.createBioPrompt(
                    fragmentActivity = context,
                    onSuccess = {
                        if (it != null) {
                            approvalDetailsViewModel.biometryApproved(cryptoObject = it)
                        } else {
                            BioCryptoUtil.handleBioPromptOnFail(
                                context = context,
                                errorCode = BioCryptoUtil.NO_CIPHER_CODE
                            ) {
                                resetDataAfterErrorDismissed()
                            }
                        }
                    },
                    onFail = {
                        BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                            resetDataAfterErrorDismissed()
                        }
                    }
                )

                bioPrompt.authenticate(promptInfo, approvalDetailsState.bioPromptTrigger.data)
            }
            approvalDetailsViewModel.resetPromptTrigger()
        }

        if (approvalDetailsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success
            || approvalDetailsState.approvalDispositionState?.initiationDispositionResult is Resource.Success
        ) {
            approvalDetailsViewModel.wipeDataAndKickUserOutToApprovalsScreen()
        }
        if (approvalDetailsState.shouldKickOutUserToApprovalsScreen) {
            approvalDetailsViewModel.resetShouldKickOutUser()

            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(ApprovalsViewModel.Companion.KEY_SHOULD_REFRESH_DATA, true)
            navController.navigateUp()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_START
            -> {
                approvalDetailsViewModel.handleScreenForegrounded()
            }
            Lifecycle.Event.ON_PAUSE -> {
                approvalDetailsViewModel.handleScreenBackgrounded()
            }
            else -> Unit
        }
    }

    //region Screen Content
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ApprovalDetailsTopAppBar(
                onAppBarIconClick = { navController.navigateUp() },
                navigationIcon = Icons.Rounded.ArrowBack,
                navigationIconContentDes = stringResource(id = R.string.content_des_back_icon)
            )
        },
        content = {
            val isInitiationRequest = approval?.isInitiationRequest() == true
            ApprovalDetails(
                onApproveClicked = {
                    approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
                        isApproving = true,
                        isInitiationRequest = isInitiationRequest,
                        dialogMessages = approval?.getApprovalRequestType()?.getDialogMessages(
                            context = context,
                            approvalDisposition = ApprovalDisposition.APPROVE,
                            isInitiationRequest = isInitiationRequest
                        )
                            ?: UnknownApprovalType.getDialogMessages(
                                context = context,
                                approvalDisposition = ApprovalDisposition.APPROVE,
                                isInitiationRequest = isInitiationRequest
                            )
                    )
                },
                onDenyClicked = {
                    approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
                        isApproving = false,
                        isInitiationRequest = isInitiationRequest,
                        dialogMessages = approval?.getApprovalRequestType()?.getDialogMessages(
                            context = context,
                            approvalDisposition = ApprovalDisposition.DENY,
                            isInitiationRequest = isInitiationRequest
                        )
                            ?: UnknownApprovalType.getDialogMessages(
                                context = context,
                                approvalDisposition = ApprovalDisposition.DENY,
                                isInitiationRequest = isInitiationRequest
                            )
                    )
                },
                isLoading = approvalDetailsState.loadingData || durableNonceState.isLoading,
                shouldRefreshTimer = approvalDetailsState.shouldRefreshTimers,
                getSecondsLeftUntilCountdownIsOver = { submitDate: String?, totalTimeInSeconds: Int? ->
                    calculateSecondsLeftUntilCountdownIsOver(
                        submitDate = submitDate,
                        totalTimeInSeconds = totalTimeInSeconds
                    )
                },
                approval = approvalDetailsState.selectedApproval,
                initiationRequest = approvalDetailsState.selectedApproval?.isInitiationRequest() == true
            )

            if (approvalDetailsState.shouldDisplayConfirmDisposition != null) {
                if (approvalDetailsState.selectedApproval?.getApprovalRequestType() is LoginApprovalRequest) {
                    approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                    launchNonceWork()
                } else {
                    CensoConfirmDispositionAlertDialog(
                        dialogMessages = approvalDetailsState.shouldDisplayConfirmDisposition.dialogMessages,
                        onConfirm = {
                            approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                            launchNonceWork()
                        },
                        onDismiss = {
                            approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                        }
                    )
                }
            }

            //region Error handling
            if (approvalDetailsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Error) {
                val retryData = approvalDetailsState.approvalDispositionState.approvalRetryData

                CensoErrorScreen(
                    errorResource = approvalDetailsState.approvalDispositionState.registerApprovalDispositionResult as Resource.Error<ApprovalDispositionRequest.RegisterApprovalDispositionBody>,
                    onDismiss = {
                        resetDataAfterErrorDismissed()
                    },
                    onRetry = {
                        retryApprovalDisposition(
                            isApproving = retryData.isApproving,
                            isInitiationRequest = retryData.isInitiationRequest
                        )
                    }
                )
            }

            if (approvalDetailsState.approvalDispositionState?.initiationDispositionResult is Resource.Error) {
                val retryData = approvalDetailsState.approvalDispositionState.approvalRetryData

                CensoErrorScreen(
                    errorResource = approvalDetailsState.approvalDispositionState.initiationDispositionResult as Resource.Error<InitiationRequest.InitiateRequestBody>,
                    onDismiss = {
                        resetDataAfterErrorDismissed()
                    },
                    onRetry = {
                        retryApprovalDisposition(
                            isApproving = retryData.isApproving,
                            isInitiationRequest = retryData.isInitiationRequest
                        )
                    }
                )
            }

            if (durableNonceState.multipleAccountsResult is Resource.Error) {
                val retryData = approvalDetailsState.approvalDispositionState?.approvalRetryData

                CensoErrorScreen(
                    errorResource = durableNonceState.multipleAccountsResult,
                    onDismiss = {
                        resetDataAfterErrorDismissed()
                    },
                    onRetry = {
                        if (retryData != null) {
                            retryApprovalDisposition(
                                isApproving = retryData.isApproving,
                                isInitiationRequest = retryData.isInitiationRequest
                            )
                        }
                    }
                )
            }
            //endregion
        }
    )
    //endregion
}

//region Screen Composables
@Composable
fun ApprovalDetailsTopAppBar(
    onAppBarIconClick: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String
) {
    CenteredTopAppBar(
        title = stringResource(R.string.details_title),
        onAppBarIconClick = { onAppBarIconClick() },
        navigationIcon = navigationIcon,
        navigationIconContentDes = navigationIconContentDes
    )
}

@Composable
fun ApprovalDetails(
    onApproveClicked: () -> Unit,
    onDenyClicked: () -> Unit,
    shouldRefreshTimer: Boolean,
    getSecondsLeftUntilCountdownIsOver: (String?, Int?) -> Long?,
    isLoading: Boolean,
    approval: ApprovalRequest?,
    initiationRequest: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .background(BackgroundBlack)
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val requestType = approval?.let { safeApproval ->
                val type = safeApproval.getApprovalRequestType()

                //Defensive coding,
                // we should never have an unknown approval in the details screen
                if (type != UnknownApprovalType) {
                    ApprovalDetailContent(approval = safeApproval, type = type)
                }
                type
            }

            val calculatedTimerSecondsLeft = getSecondsLeftUntilCountdownIsOver(approval?.submitDate, approval?.approvalTimeoutInSeconds)
            val timeRemainingInSeconds = if (shouldRefreshTimer) calculatedTimerSecondsLeft else calculatedTimerSecondsLeft

            val expiresInText = getApprovalTimerText(
                context = LocalContext.current,
                timeRemainingInSeconds = timeRemainingInSeconds
            )

            ApprovalStatus(
                requestedBy = approval?.submitterEmail ?: "",
                requestedByName = approval?.submitterName ?: "",
                approvalsReceived = approval?.numberOfApprovalsReceived ?: 0,
                totalApprovals = approval?.numberOfDispositionsRequired ?: 0,
                denialsReceived = approval?.numberOfDeniesReceived ?: 0,
                expiresIn = expiresInText,
                vaultName = approval?.vaultName,
                requestType = requestType,
                isInitiationRequest = initiationRequest
            )
            Spacer(modifier = Modifier.height(28.dp))

        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ApprovalDetailsButtons(
                onApproveClicked = { onApproveClicked() },
                onDenyClicked = { onDenyClicked() },
                isLoading = isLoading,
                initiationRequest = initiationRequest,
                positiveText = approval?.approveButtonCaption(context = LocalContext.current) ?: stringResource(R.string.approve)
            )
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = CensoWhite
            )
        }
    }
}

//todo: Need user image here and would need to be a unique row also to show image for Requested/Initiated By
@Composable
fun ApprovalStatus(
    requestedBy: String,
    requestedByName: String,
    approvalsReceived: Int,
    totalApprovals: Int,
    denialsReceived: Int,
    expiresIn: String?,
    vaultName: String?,
    requestType: ApprovalRequestDetails?,
    isInitiationRequest: Boolean
) {
    val facts = mutableListOf<RowData>()
    vaultName?.let { facts.add(RowData(title = stringResource(R.string.vault_name), value = vaultName)) }
    val sectionTitle = if (requestType is AcceptVaultInvitation) {
        facts.add(RowData(title = stringResource(R.string.invited_by), value = requestedByName))
        facts.add(RowData(title = stringResource(R.string.invited_by_email), value = requestedBy))
        ""
    } else {
        facts.add(
            RowData(
                title = if (isInitiationRequest) {
                    stringResource(R.string.initiated_by)
                } else {
                    stringResource(R.string.requested_by)
                },
                value = requestedBy
            )
        )
        facts.add(
            RowData(
            title = stringResource(R.string.approvals_received),
            value = "$approvalsReceived ${stringResource(id = R.string.of)} $totalApprovals",
        ))
        facts.add(RowData(
            title = stringResource(R.string.denials_received),
            value = "$denialsReceived ${stringResource(id = R.string.of)} $totalApprovals",
        ))
        stringResource(R.string.status)
    }
    expiresIn?.let { facts.add(
         RowData(title = stringResource(R.string.expires_in), value = expiresIn))  }
    
    val factsData = FactsData(title = sectionTitle, facts = facts.toList())
    
    FactRow(factsData = factsData)
}

@Composable
fun ApprovalDetailsButtons(
    onApproveClicked: () -> Unit,
    onDenyClicked: () -> Unit,
    isLoading: Boolean,
    positiveText: String,
    initiationRequest: Boolean = false
) {
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        onClick = { onDenyClicked() },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = DetailDenyRedBackground),
        enabled = !isLoading
    ) {
        Text(
            modifier = Modifier.padding(all = 4.dp),
            text = if (initiationRequest) stringResource(id = R.string.cancel) else stringResource(
                id = R.string.deny
            ),
            color = DetailDenyRed,
            fontSize = 16.sp
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            .fillMaxWidth(),
        onClick = { onApproveClicked() },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = DetailApprovalGreenBackground),
        enabled = !isLoading
    ) {
        Text(
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 10.dp),
            text = positiveText,
            color = DetailApprovalGreen,
            fontSize = 26.sp
        )
    }
}
//endregion