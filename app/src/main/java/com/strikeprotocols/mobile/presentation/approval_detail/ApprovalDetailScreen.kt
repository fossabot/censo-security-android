package com.strikeprotocols.mobile.presentation.approval_detail

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.strikeprotocols.mobile.common.BiometricUtil.createBioPrompt
import com.strikeprotocols.mobile.common.BiometricUtil.getBasicBiometricPromptBuilder
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
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

    val context = LocalContext.current as FragmentActivity

    val promptInfo = getBasicBiometricPromptBuilder(context).build()

    val bioPrompt = createBioPrompt(
        fragmentActivity = context,
        onSuccess = {
            val nonceAddresses = approvalDetailsState.approval?.retrieveAccountAddresses()
            nonceAddresses?.let {
                durableNonceViewModel.setNonceAccountAddresses(nonceAddresses)
            }
            durableNonceViewModel.setUserBiometricVerified(isVerified = true)
        },
        onFail = {
            durableNonceViewModel.setUserBiometricVerified(isVerified = false)
        }
    )

    DisposableEffect(key1 = approvalDetailsViewModel) {
        approvalDetailsViewModel.onStart(approval)
        onDispose { approvalDetailsViewModel.onStop() }
    }

    LaunchedEffect(key1 = approvalDetailsState, key2 = durableNonceState) {
        if (durableNonceState.triggerBioPrompt) {
            durableNonceViewModel.resetPromptTrigger()
            bioPrompt.authenticate(promptInfo)
        }
        if (durableNonceState.multipleAccountsResult is Resource.Success) {
            if (durableNonceState.multipleAccounts != null) {
                approvalDetailsViewModel.setMultipleAccounts(durableNonceState.multipleAccounts)
                durableNonceViewModel.resetState()
            }
        }
        if (approvalDetailsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success
            || approvalDetailsState.approvalDispositionState?.initiationDispositionResult is Resource.Success
        ) {
            approvalDetailsViewModel.wipeDataAndKickUserOutToApprovalsScreen()
        }
        if (approvalDetailsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Error
            || approvalDetailsState.approvalDispositionState?.initiationDispositionResult is Resource.Error
        ) {
            approvalDetailsViewModel.setShouldDisplayApprovalDispositionError()
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
                    durableNonceViewModel.setPromptTrigger()
                } else {
                    approvalDetailsState.shouldDisplayConfirmDisposition.let { safeDialogDetails ->
                        StrikeConfirmDispositionAlertDialog(
                            dialogTitle = safeDialogDetails.dialogTitle,
                            dialogText = safeDialogDetails.dialogText,
                            onConfirm = {
                                approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                                durableNonceViewModel.setPromptTrigger()
                            },
                            onDismiss = {
                                approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                            }
                        )
                    }
                }
            }

            if (approvalDetailsState.shouldDisplayApprovalDispositionError) {
                val approvalDispositionError =
                    approvalDetailsState.approvalDispositionState?.approvalDispositionError
                approvalDispositionError?.let { safeApprovalDispositionError ->
                    val dialogErrorText = retrieveApprovalDispositionDialogErrorText(
                        safeApprovalDispositionError,
                        context
                    )
                    StrikeApprovalDispositionErrorAlertDialog(
                        dialogTitle = stringResource(R.string.approval_disposition_error_title),
                        dialogText = dialogErrorText,
                        onConfirm = {
                            approvalDetailsViewModel.dismissApprovalDispositionError()
                        }
                    )
                }
            }
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
    StrikeTopAppBar(
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
            approval?.let { safeApproval ->
                val type = safeApproval.getSolanaApprovalRequestType()

                //Defensive coding,
                // we should never have an unknown approval in the details screen
                if (type != UnknownApprovalType) {
                    ApprovalDetailContent(approval = safeApproval, type = type)
                }
            }

            val expiresInText = getApprovalTimerText(
                context = LocalContext.current,
                timeRemainingInSeconds = timeRemainingInSeconds
            )

            ApprovalStatus(
                requestedBy = approval?.submitterEmail ?: "",
                approvalsReceived = approval?.numberOfApprovalsReceived ?: 0,
                totalApprovals = approval?.numberOfDispositionsRequired ?: 0,
                denialsReceived = approval?.numberOfDeniesReceived ?: 0,
                expiresIn = expiresInText,
                vaultName = approval?.vaultName
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
    approvalsReceived: Int,
    totalApprovals: Int,
    denialsReceived: Int,
    expiresIn: String?,
    vaultName: String?
) {

    val facts = listOfNotNull(
        vaultName?.let { Pair(stringResource(R.string.vault_name), vaultName) },
        Pair(stringResource(R.string.requested_by), requestedBy),
        Pair(stringResource(R.string.approvals_received), "$approvalsReceived ${stringResource(id = R.string.of)} $totalApprovals"),
        Pair(stringResource(R.string.denials_received), "$denialsReceived ${stringResource(id = R.string.of)} $totalApprovals"),
        expiresIn?.let { Pair(stringResource(R.string.expires_in), expiresIn) }
    )
    
    val factsData = FactsData(title = stringResource(R.string.status), facts = facts)
    
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

//region Preview

//This stateless composable can be useful for quick iteration on UI
// and using the preview to test
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun StatelessApprovalDetailsScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ApprovalDetailsTopAppBar(
                onAppBarIconClick = {},
                navigationIcon = Icons.Rounded.ArrowBackIos,
                navigationIconContentDes = stringResource(id = R.string.content_des_back_icon)
            )
        },
        content = {
            ApprovalDetails(
                onApproveClicked = { strikeLog(message = "Approve clicked") },
                onDenyClicked = { strikeLog(message = "Deny clicked") },
                timeRemainingInSeconds = 1000,
                isLoading = false,
                approval = GeneralDummyData().generateWalletApprovalsDummyData(),
                initiationRequest = GeneralDummyData().generateWalletApprovalsDummyData().isInitiationRequest()
            )
        }
    )
}

@Preview(showBackground = true, name = "StatelessApprovalDetailsScreen")
@Composable
fun ApprovalDetailsScreenPreview() {
    StatelessApprovalDetailsScreen()
}
//endregion