package com.strikeprotocols.mobile.presentation.approval_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
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
import com.strikeprotocols.mobile.presentation.blockhash.BlockHashViewModel
import com.strikeprotocols.mobile.presentation.components.OnLifecycleEvent
import com.strikeprotocols.mobile.presentation.components.StrikeApprovalDispositionErrorAlertDialog
import com.strikeprotocols.mobile.presentation.components.StrikeConfirmDispositionAlertDialog
import com.strikeprotocols.mobile.presentation.components.StrikeTopAppBar
import com.strikeprotocols.mobile.ui.theme.*
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.*
import com.strikeprotocols.mobile.presentation.approvals.ApprovalDetailContent
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.getApprovalRowMetaData

@Composable
fun ApprovalDetailsScreen(
    navController: NavController,
    approvalDetailsViewModel: ApprovalDetailsViewModel = hiltViewModel(),
    blockHashViewModel: BlockHashViewModel = hiltViewModel(),
    approval: WalletApproval?
) {
    val approvalDetailsState = approvalDetailsViewModel.state
    val blockHashState = blockHashViewModel.state

    val context = LocalContext.current as FragmentActivity

    val promptInfo = getBasicBiometricPromptBuilder(context).build()

    val bioPrompt = createBioPrompt(
        fragmentActivity = context,
        onSuccess = {
            blockHashViewModel.setUserBiometricVerified(isVerified = true)
        },
        onFail = {
            blockHashViewModel.setUserBiometricVerified(isVerified = false)
        }
    )

    DisposableEffect(key1 = approvalDetailsViewModel) {
        approvalDetailsViewModel.onStart(approval)
        onDispose { approvalDetailsViewModel.onStop() }
    }

    LaunchedEffect(key1 = approvalDetailsState, key2 = blockHashState) {
        if (blockHashState.triggerBioPrompt) {
            blockHashViewModel.resetPromptTrigger()
            bioPrompt.authenticate(promptInfo)
        }
        if (blockHashState.recentBlockhashResult is Resource.Success) {
            if (blockHashState.blockHash != null) {
                approvalDetailsViewModel.setBlockHash(blockHashState.blockHash)
                blockHashViewModel.resetState()
            }
        }
        if (approvalDetailsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success
            || approvalDetailsState.approvalDispositionState?.initiationDispositionResult is Resource.Success) {
            approvalDetailsViewModel.wipeDataAndKickUserOutToApprovalsScreen()
        }
        if (approvalDetailsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Error
            || approvalDetailsState.approvalDispositionState?.initiationDispositionResult is Resource.Error) {
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


    val approvalRowMetadata = approvalDetailsState.approval?.getSolanaApprovalRequestType()
        ?.getApprovalRowMetaData(context = context)
    val title = approvalRowMetadata?.approvalTypeTitle
        ?: stringResource(id = R.string.generic_approval_title)

    //region Screen Content
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ApprovalDetailsTopAppBar(
                title = title,
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
                        dialogTitle = approval?.getSolanaApprovalRequestType()?.getApprovalTypeDialogTitle(context)
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
                        dialogTitle = approval?.getSolanaApprovalRequestType()?.getApprovalTypeDialogTitle(context)
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
                timeRemainingInSeconds = approvalDetailsState.remainingTimeInSeconds,
                isLoading = approvalDetailsState.loadingData || blockHashState.isLoading,
                approval = approvalDetailsState.approval,
                initiationRequest = approvalDetailsState.approval?.isInitiationRequest() == true
            )

            if (approvalDetailsState.shouldDisplayConfirmDisposition != null) {
                approvalDetailsState.shouldDisplayConfirmDisposition.let { safeDialogDetails ->
                    StrikeConfirmDispositionAlertDialog(
                        dialogTitle = safeDialogDetails.dialogTitle,
                        dialogText = safeDialogDetails.dialogText,
                        onConfirm = {
                            approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                            blockHashViewModel.setPromptTrigger()
                        },
                        onDismiss = {
                            approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                        }
                    )
                }
            }

            if (approvalDetailsState.shouldDisplayApprovalDispositionError) {
                val approvalDispositionError = approvalDetailsState.approvalDispositionState?.approvalDispositionError
                approvalDispositionError?.let { safeApprovalDispositionError ->
                    val dialogErrorText = retrieveApprovalDispositionDialogErrorText(safeApprovalDispositionError, context)
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
    title: String,
    onAppBarIconClick: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String
) {
    StrikeTopAppBar(
        title = title,
        onAppBarIconClick = { onAppBarIconClick() },
        navigationIcon = navigationIcon,
        navigationIconContentDes = navigationIconContentDes
    )
}

@Composable
fun ApprovalDetails(
    onApproveClicked: () -> Unit,
    onDenyClicked: () -> Unit,
    timeRemainingInSeconds: Long,
    isLoading: Boolean,
    approval: WalletApproval?,
    initiationRequest: Boolean
    ) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ApprovalDetailsTimer(
            timeRemainingInSeconds = timeRemainingInSeconds
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlack)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            approval?.let { safeApproval ->
                val type = safeApproval.getSolanaApprovalRequestType()
                val approvalsNeeded = safeApproval.numberOfDispositionsRequired ?: 0

                //Defensive coding,
                // we should never have an unknown approval in the details screen
                if (type != UnknownApprovalType) {
                    ApprovalDetailContent(approval = safeApproval, type = type, approvalsNeeded = approvalsNeeded)
                }
            }

            ApprovalDetailsButtons(
                onApproveClicked = { onApproveClicked() },
                onDenyClicked = { onDenyClicked() },
                isLoading = isLoading,
                initiationRequest = initiationRequest
            )
        }
    }

    if(isLoading) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = StrikeWhite
            )
        }
    }
}

@Composable
fun ApprovalDetailsTimer(
    timeRemainingInSeconds: Long
) {
    val timerFinished = timeRemainingInSeconds <= 0

    val background = if (timerFinished) DenyRed else SectionBlack
    val text = if (timerFinished) stringResource(R.string.approval_expired) else
        "${stringResource(id = R.string.expires_in)} ${convertSecondsIntoCountdownText(LocalContext.current, timeRemainingInSeconds)}"

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(vertical = 6.dp),
        text = text,
        textAlign = TextAlign.Center,
        color = StrikeWhite,
        fontSize = 18.sp
    )
}

@Composable
fun ApprovalDetailsButtons(
    onApproveClicked: () -> Unit,
    onDenyClicked: () -> Unit,
    isLoading: Boolean,
    initiationRequest: Boolean = false
) {
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        onClick = { onDenyClicked() },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(backgroundColor = DenyRedBackground),
        enabled = !isLoading
    ) {
        Text(
            modifier = Modifier.padding(all = 4.dp),
            text = if (initiationRequest) stringResource(id = R.string.cancel) else stringResource(id = R.string.deny),
            color = DenyRed,
            fontSize = 16.sp
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        onClick = { onApproveClicked() },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(backgroundColor = ApprovalGreenBackground),
        enabled = !isLoading
    ) {
        Text(
            modifier = Modifier.padding(all = 6.dp),
            text = stringResource(id = R.string.approve),
            color = ApprovalGreen,
            fontSize = 24.sp
        )
    }
}
//endregion

//region Preview

//This stateless composable can be useful for quick iteration on UI
// and using the preview to test
@Composable
fun StatelessApprovalDetailsScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ApprovalDetailsTopAppBar(
                title = stringResource(id = R.string.transfer_details),
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
                approval = generateWalletApprovalsDummyData(),
                initiationRequest = generateWalletApprovalsDummyData().isInitiationRequest()
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