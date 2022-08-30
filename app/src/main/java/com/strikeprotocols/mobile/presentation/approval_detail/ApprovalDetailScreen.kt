package com.strikeprotocols.mobile.presentation.approval_detail

import android.annotation.SuppressLint
import android.widget.Toast
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
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.ApprovalDispositionRequest
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsViewModel
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getApprovalTypeDialogTitle
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getDialogFullMessage
import com.strikeprotocols.mobile.ui.theme.*
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.*
import com.strikeprotocols.mobile.presentation.approvals.ApprovalDetailContent
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getApprovalTimerText
import com.strikeprotocols.mobile.presentation.components.*
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ApprovalDetailsScreen(
    navController: NavController,
    approvalDetailsViewModel: ApprovalDetailsViewModel = hiltViewModel(),
    durableNonceViewModel: DurableNonceViewModel = hiltViewModel(),
    approval: WalletApproval?
) {
    val approvalDetailsState = approvalDetailsViewModel.state
    val durableNonceState = durableNonceViewModel.state

    fun resetDataAfterErrorDismissed() {
        approvalDetailsViewModel.dismissApprovalDispositionError()
        durableNonceViewModel.resetMultipleAccountsResource()
        approvalDetailsViewModel.resetMultipleAccounts()
    }

    val context = LocalContext.current as FragmentActivity

    val promptInfo = BioCryptoUtil.createPromptInfo(context, isSavingData = false)

    val bioPrompt = BioCryptoUtil.createBioPrompt(
        fragmentActivity = context,
        onSuccess = {
            approvalDetailsViewModel.biometryApproved(it!!)
        },
        onFail = {
            if (it == BioCryptoUtil.TOO_MANY_ATTEMPTS_CODE || it == BioCryptoUtil.FINGERPRINT_DISABLED_CODE) {
                Toast.makeText(
                    context,
                    context.getString(R.string.too_many_failed_attempts),
                    Toast.LENGTH_LONG
                ).show()
            }
            resetDataAfterErrorDismissed()
        }
    )

    fun launchNonceWork() {
        val nonceAddresses = approvalDetailsState.approval?.retrieveAccountAddresses()
        val minimumSlotAddress = approvalDetailsState.approval?.retrieveAccountAddressesSlot()
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
            dialogTitle = approval?.getSolanaApprovalRequestType()
                ?.getApprovalTypeDialogTitle(context)
                ?: UnknownApprovalType.getApprovalTypeDialogTitle(context),
            dialogText = approval?.getSolanaApprovalRequestType()?.getDialogFullMessage(
                context = context,
                approvalDisposition = if(isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
                initiationRequest = isInitiationRequest
            )
                ?: UnknownApprovalType.getDialogFullMessage(
                    context = context,
                    approvalDisposition = if(isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
                    initiationRequest = isInitiationRequest
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
            bioPrompt.authenticate(
                promptInfo,
                BiometricPrompt.CryptoObject(approvalDetailsState.bioPromptTrigger.data!!)
            )
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
                if (approvalDetailsState.screenWasBackgrounded) {
                    approvalDetailsViewModel.resetDetailsScreenWasBackgrounded()
                    approvalDetailsViewModel.wipeDataAndKickUserOutToApprovalsScreen()
                }
            }
            Lifecycle.Event.ON_PAUSE -> {
                approvalDetailsViewModel.setDetailsScreenWasBackgrounded()
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
                        dialogTitle = approval?.getSolanaApprovalRequestType()
                            ?.getApprovalTypeDialogTitle(context)
                            ?: UnknownApprovalType.getApprovalTypeDialogTitle(context),
                        dialogText = approval?.getSolanaApprovalRequestType()?.getDialogFullMessage(
                            context = context,
                            approvalDisposition = ApprovalDisposition.APPROVE,
                            initiationRequest = isInitiationRequest
                        )
                            ?: UnknownApprovalType.getDialogFullMessage(
                                context = context,
                                approvalDisposition = ApprovalDisposition.APPROVE,
                                initiationRequest = isInitiationRequest
                            )
                    )
                },
                onDenyClicked = {
                    approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
                        isApproving = false,
                        isInitiationRequest = isInitiationRequest,
                        dialogTitle = approval?.getSolanaApprovalRequestType()
                            ?.getApprovalTypeDialogTitle(context)
                            ?: UnknownApprovalType.getApprovalTypeDialogTitle(context),
                        dialogText = approval?.getSolanaApprovalRequestType()?.getDialogFullMessage(
                            context = context,
                            approvalDisposition = ApprovalDisposition.DENY,
                            initiationRequest = isInitiationRequest
                        )
                            ?: UnknownApprovalType.getDialogFullMessage(
                                context = context,
                                approvalDisposition = ApprovalDisposition.DENY,
                                initiationRequest = isInitiationRequest
                            )
                    )
                },
                isLoading = approvalDetailsState.loadingData || durableNonceState.isLoading,
                timeRemainingInSeconds = approvalDetailsState.remainingTimeInSeconds,
                approval = approvalDetailsState.approval,
                initiationRequest = approvalDetailsState.approval?.isInitiationRequest() == true
            )

            if (approvalDetailsState.shouldDisplayConfirmDisposition != null) {
                if (approvalDetailsState.approval?.getSolanaApprovalRequestType() is LoginApprovalRequest) {
                    approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                    launchNonceWork()
                } else {
                    approvalDetailsState.shouldDisplayConfirmDisposition.let { safeDialogDetails ->
                        StrikeConfirmDispositionAlertDialog(
                            dialogTitle = safeDialogDetails.dialogTitle,
                            dialogText = safeDialogDetails.dialogText,
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
            }

            //region Error handling
            if (approvalDetailsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Error) {
                val retryData = approvalDetailsState.approvalDispositionState.approvalRetryData

                StrikeErrorScreen(
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

                StrikeErrorScreen(
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

                StrikeErrorScreen(
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
    StrikeCenteredTopAppBar(
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
    timeRemainingInSeconds: Long?,
    isLoading: Boolean,
    approval: WalletApproval?,
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
                val type = safeApproval.getSolanaApprovalRequestType()

                //Defensive coding,
                // we should never have an unknown approval in the details screen
                if (type != UnknownApprovalType) {
                    ApprovalDetailContent(approval = safeApproval, type = type)
                }
                type
            }

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
                requestType = requestType
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
                color = StrikeWhite
            )
        }
    }
}

@Composable
fun ApprovalStatus(
    requestedBy: String,
    requestedByName: String,
    approvalsReceived: Int,
    totalApprovals: Int,
    denialsReceived: Int,
    expiresIn: String?,
    vaultName: String?,
    requestType: SolanaApprovalRequestType?
) {

    val facts = mutableListOf<Pair<String, String>>()
    vaultName?.let { facts.add(Pair(stringResource(R.string.vault_name), vaultName)) }
    val sectionTitle = if (requestType is AcceptVaultInvitation) {
        facts.add(Pair(stringResource(R.string.invited_by), requestedByName))
        facts.add(Pair(stringResource(R.string.invited_by_email), requestedBy))
        ""
    } else {
        facts.add(Pair(stringResource(R.string.requested_by), requestedBy))
        facts.add(Pair(
            stringResource(R.string.approvals_received),
            "$approvalsReceived ${stringResource(id = R.string.of)} $totalApprovals"
        ))
        facts.add(Pair(
            stringResource(R.string.denials_received),
            "$denialsReceived ${stringResource(id = R.string.of)} $totalApprovals"
        ))
        stringResource(R.string.status)
    }
    expiresIn?.let { facts.add(Pair(stringResource(R.string.expires_in), expiresIn))  }
    
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