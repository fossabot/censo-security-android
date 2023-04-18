package com.censocustody.android.presentation.approval_detail

import android.annotation.SuppressLint
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.*
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.presentation.approvals.ApprovalsViewModel
import com.censocustody.android.ui.theme.*
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.presentation.approvals.ApprovalDetailContent
import com.censocustody.android.presentation.approvals.approval_type_row_items.getApprovalTimerText
import com.censocustody.android.presentation.approvals.approval_type_row_items.getDialogMessages
import com.censocustody.android.presentation.components.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ApprovalDetailsScreen(
    navController: NavController,
    approvalDetailsViewModel: ApprovalDetailsViewModel = hiltViewModel(),
    approval: ApprovalRequestV2?
) {
    val approvalDetailsState = approvalDetailsViewModel.state

    fun resetDataAfterErrorDismissed() {
        approvalDetailsViewModel.dismissApprovalDispositionError()
    }

    val context = LocalContext.current as FragmentActivity

    val promptInfo = BioCryptoUtil.createPromptInfo(context = context)

    fun retryApprovalDisposition(isApproving: Boolean) {
        approvalDetailsViewModel.dismissApprovalDispositionError()
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = isApproving,
            dialogMessages = approval?.details?.getDialogMessages(
                context = context,
                approvalDisposition = if (isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            )
                ?: ApprovalRequestDetailsV2.UnknownApprovalType.getDialogMessages(
                    context = context,
                    approvalDisposition = if (isApproving) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
                )
        )
    }

    DisposableEffect(key1 = approvalDetailsViewModel) {
        approvalDetailsViewModel.onStart(approval)
        onDispose { approvalDetailsViewModel.onStop() }
    }

    LaunchedEffect(key1 = approvalDetailsState) {
        if (approvalDetailsState.bioPromptTrigger is Resource.Success) {
            val bioPrompt = BioCryptoUtil.createBioPrompt(
                fragmentActivity = context,
                onSuccess = { approvalDetailsViewModel.biometryApproved() },
                onFail = {
                    BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                        resetDataAfterErrorDismissed()
                    }
                }
            )

            bioPrompt.authenticate(promptInfo)

            approvalDetailsViewModel.resetPromptTrigger()
        }

        if (approvalDetailsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success) {
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
            ApprovalDetails(
                onApproveClicked = {
                    approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
                        isApproving = true,
                        dialogMessages = approval?.details?.getDialogMessages(
                            context = context,
                            approvalDisposition = ApprovalDisposition.APPROVE,
                        )
                            ?: ApprovalRequestDetailsV2.UnknownApprovalType.getDialogMessages(
                                context = context,
                                approvalDisposition = ApprovalDisposition.APPROVE,
                            )
                    )
                },
                onDenyClicked = {
                    approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
                        isApproving = false,
                        dialogMessages = approval?.details?.getDialogMessages(
                            context = context,
                            approvalDisposition = ApprovalDisposition.DENY,
                        )
                            ?: ApprovalRequestDetailsV2.UnknownApprovalType.getDialogMessages(
                                context = context,
                                approvalDisposition = ApprovalDisposition.DENY,
                            )
                    )
                },
                isLoading = approvalDetailsState.loadingData,
                shouldRefreshTimer = approvalDetailsState.shouldRefreshTimers,
                getSecondsLeftUntilCountdownIsOver = { submitDate: String?, totalTimeInSeconds: Int? ->
                    calculateSecondsLeftUntilCountdownIsOver(
                        submitDate = submitDate,
                        totalTimeInSeconds = totalTimeInSeconds
                    )
                },
                approval = approvalDetailsState.selectedApproval,
                initiationRequest = false
            )

            if (approvalDetailsState.shouldDisplayConfirmDisposition != null) {
                if (approvalDetailsState.selectedApproval?.details is ApprovalRequestDetailsV2.Login) {
                    approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                    approvalDetailsViewModel.triggerBioPrompt()
                } else {
                    CensoConfirmDispositionAlertDialog(
                        dialogMessages = approvalDetailsState.shouldDisplayConfirmDisposition.dialogMessages,
                        onConfirm = {
                            approvalDetailsViewModel.resetShouldDisplayConfirmDisposition()
                            approvalDetailsViewModel.triggerBioPrompt()
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
                    errorResource = approvalDetailsState.approvalDispositionState.registerApprovalDispositionResult as Resource.Error<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body>,
                    onDismiss = {
                        resetDataAfterErrorDismissed()
                    },
                    onRetry = {
                        retryApprovalDisposition(isApproving = retryData.isApproving)
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
        backgroundColor = BackgroundGrey,
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
    approval: ApprovalRequestV2?,
    initiationRequest: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .background(BackgroundGrey)
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val calculatedTimerSecondsLeft = getSecondsLeftUntilCountdownIsOver(approval?.submitDate, approval?.approvalTimeoutInSeconds?.toInt())
            val timeRemainingInSeconds = if (shouldRefreshTimer) calculatedTimerSecondsLeft else calculatedTimerSecondsLeft

            val expiresInText = getApprovalTimerText(
                context = LocalContext.current,
                timeRemainingInSeconds = timeRemainingInSeconds
            )

            approval?.let {
                ApprovalDetailContent(approval = it, type = it.details)
            }

            ApprovalStatus(
                requestedBy = approval?.submitterEmail ?: "",
                approvalsReceived = approval?.numberOfApprovalsReceived ?: 0,
                totalApprovals = approval?.numberOfDispositionsRequired ?: 0,
                denialsReceived = approval?.numberOfDeniesReceived ?: 0,
                expiresIn = expiresInText,
                vaultName = approval?.vaultName,
                isInitiationRequest = initiationRequest
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent)
                .shadow(5.dp)
                .background(color = BackgroundGrey)
                .fillMaxWidth()
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
                color = ButtonRed
            )
        }
    }
}

//todo: Need user image here and would need to be a unique row also to show image for Requested/Initiated By
@Composable
fun ApprovalStatus(
    requestedBy: String,
    approvalsReceived: Int,
    totalApprovals: Int,
    denialsReceived: Int,
    expiresIn: String?,
    vaultName: String?,
    isInitiationRequest: Boolean
) {
    val facts = mutableListOf<RowData>()
    vaultName?.let {
        facts.add(RowData.KeyValueRow(
            key = stringResource(R.string.vault_name),
            value = vaultName
        ))
    }
    val sectionTitle = stringResource(R.string.status)
    facts.add(RowData.KeyValueRow(
        key = if (isInitiationRequest) {
            stringResource(R.string.initiated_by)
        } else {
            stringResource(R.string.requested_by)
        },
        value = requestedBy
    ))
    facts.add(RowData.KeyValueRow(
        key = stringResource(R.string.approvals_received),
        value = "$approvalsReceived ${stringResource(id = R.string.of)} $totalApprovals",
    ))
    facts.add(RowData.KeyValueRow(
        key = stringResource(R.string.denials_received),
        value = "$denialsReceived ${stringResource(id = R.string.of)} $totalApprovals",
    ))
    expiresIn?.let {
        facts.add(RowData.KeyValueRow(
            key = stringResource(R.string.expires_in),
            value = expiresIn
        ))
    }

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