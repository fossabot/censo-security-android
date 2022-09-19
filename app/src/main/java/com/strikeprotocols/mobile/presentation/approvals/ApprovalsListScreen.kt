package com.strikeprotocols.mobile.presentation.approvals

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.common.BioCryptoUtil.NO_CIPHER_CODE
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.ApprovalDispositionRequest
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequestDetails.*
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequest
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.*
import com.strikeprotocols.mobile.presentation.components.*
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import com.strikeprotocols.mobile.ui.theme.*
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack

@Composable
fun ApprovalsListScreen(
    navController: NavController,
    approvalsViewModel: ApprovalsViewModel,
    durableNonceViewModel: DurableNonceViewModel = hiltViewModel()
) {
    val approvalsState = approvalsViewModel.state
    val durableNonceState = durableNonceViewModel.state

    fun resetDataAfterErrorDismissed() {
        approvalsViewModel.dismissApprovalDispositionError()
        durableNonceViewModel.resetMultipleAccountsResource()
        approvalsViewModel.resetMultipleAccounts()
    }

    val context = LocalContext.current as FragmentActivity

    val promptInfo = BioCryptoUtil.createPromptInfo(context, isSavingData = false)

    val scaffoldState = rememberScaffoldState()

    fun launchNonceWork() {
        val nonceAddresses = approvalsState.selectedApproval?.retrieveAccountAddresses()
        val minimumSlotAddress = approvalsState.selectedApproval?.retrieveAccountAddressesSlot()
        durableNonceViewModel.setInitialData(
            nonceAccountAddresses = nonceAddresses ?: emptyList(),
            minimumNonceAccountAddressesSlot = minimumSlotAddress ?: 0
        )
    }

    fun retryApprovalDisposition() {
        approvalsViewModel.dismissApprovalDispositionError()
        durableNonceViewModel.resetMultipleAccountsResource()
        approvalsViewModel.resetMultipleAccounts()
        approvalsState.selectedApproval?.let { approval ->
            approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
                approval = approval,
                isApproving = true,
                dialogMessages = approval.getApprovalRequestType()
                    .getDialogMessages(
                        context = context,
                        approvalDisposition = ApprovalDisposition.APPROVE,
                        isInitiationRequest = approval.isInitiationRequest()
                    )
            )
        }
    }

    checkForHardRefreshAfterBackNavigation(
        navController = navController,
        approvalsViewModel = approvalsViewModel
    )

    //region DisposableEffect
    DisposableEffect(key1 = approvalsViewModel) {
        approvalsViewModel.onStart()
        onDispose { approvalsViewModel.onStop() }
    }
    //endregion

    LaunchedEffect(key1 = approvalsState, key2 = durableNonceState) {

        if (approvalsState.bioPromptTrigger is Resource.Success) {
            val bioPrompt = BioCryptoUtil.createBioPrompt(
                fragmentActivity = context,
                onSuccess = {
                    strikeLog(message = "Getting success in the frikin aprovals list")
                    val cipher = it?.cipher
                    if (cipher != null) {
                        approvalsViewModel.biometryApproved(cipher)
                    } else {
                        BioCryptoUtil.handleBioPromptOnFail(
                            context = context,
                            errorCode = NO_CIPHER_CODE
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

            bioPrompt.authenticate(
                promptInfo,
                BiometricPrompt.CryptoObject(approvalsState.bioPromptTrigger.data!!)
            )
            approvalsViewModel.resetPromptTrigger()
        }

        if (durableNonceState.multipleAccountsResult is Resource.Success) {
            approvalsViewModel.setMultipleAccounts(durableNonceState.multipleAccounts)
            durableNonceViewModel.resetState()
        }

        if (approvalsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success ||
            approvalsState.approvalDispositionState?.initiationDispositionResult is Resource.Success
        ) {
            approvalsViewModel.wipeDataAfterDispositionSuccess()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_START
            -> {
                approvalsViewModel.handleScreenForegrounded()
            }
            else -> Unit
        }
    }

    //region Screen Content
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        snackbarHost = { scaffoldState.snackbarHostState },
        topBar = {
            ApprovalsListTopAppBar(
                title = stringResource(id = R.string.approvals),
                onAppBarIconClick = { navController.navigate(Screen.AccountRoute.route) },
                navigationIcon = Icons.Outlined.AccountCircle,
                navigationIconContentDes = stringResource(id = R.string.content_des_account_icon)
            )
        },
        content = { innerPadding ->
            ApprovalsList(
                isRefreshing = approvalsState.loadingData || durableNonceState.isLoading,
                onRefresh = approvalsViewModel::refreshData,
                onApproveClicked = { approval ->
                    approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
                        approval = approval,
                        isApproving = true,
                        dialogMessages = approval?.getApprovalRequestType()?.getDialogMessages(
                            context = context,
                            approvalDisposition = ApprovalDisposition.APPROVE,
                            isInitiationRequest = approval.isInitiationRequest()
                        )
                            ?: UnknownApprovalType.getDialogMessages(
                                context = context,
                                approvalDisposition = ApprovalDisposition.APPROVE,
                                isInitiationRequest = approval?.isInitiationRequest() == true
                            )
                    )
                },
                onMoreInfoClicked = { approval ->
                    approval?.let { safeApproval ->
                        navController.navigate("${Screen.ApprovalDetailRoute.route}/${ApprovalRequest.toJson(safeApproval, AndroidUriWrapper())}" )
                    }
                },
                approvalRequests = approvalsState.approvals,
                shouldRefreshTimers = approvalsState.shouldRefreshTimers,
                getSecondsLeftUntilCountdownIsOver = { submitDate: String?, totalTimeInSeconds: Int? ->
                    calculateSecondsLeftUntilCountdownIsOver(
                        submitDate = submitDate,
                        totalTimeInSeconds = totalTimeInSeconds
                    )
                }
            )

            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxHeight()
            ) {
                StrikeSnackbar(
                    snackbarHostState = scaffoldState.snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (approvalsState.shouldDisplayConfirmDisposition != null) {
                if (approvalsState.selectedApproval?.getApprovalRequestType() is LoginApprovalRequest) {
                    approvalsViewModel.resetShouldDisplayConfirmDisposition()
                    launchNonceWork()
                } else {
                    StrikeConfirmDispositionAlertDialog(
                        dialogMessages = approvalsState.shouldDisplayConfirmDisposition.dialogMessages,
                        onConfirm = {
                            approvalsViewModel.resetShouldDisplayConfirmDisposition()
                            launchNonceWork()
                        },
                        onDismiss = {
                            approvalsViewModel.resetShouldDisplayConfirmDisposition()
                        }
                    )
                }
            }

            if (approvalsState.approvalsResultRequest is Resource.Error) {
                StrikeErrorScreen(
                    errorResource = approvalsState.approvalsResultRequest,
                    onDismiss = {
                        approvalsViewModel.resetWalletApprovalsResult()
                    },
                    onRetry = {
                        approvalsViewModel.resetWalletApprovalsResult()
                        approvalsViewModel.refreshData()
                    }
                )
            }

            if (approvalsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Error) {
                StrikeErrorScreen(
                    errorResource = approvalsState.approvalDispositionState.registerApprovalDispositionResult as Resource.Error<ApprovalDispositionRequest.RegisterApprovalDispositionBody>,
                    onDismiss = {
                        resetDataAfterErrorDismissed()
                    },
                    onRetry = { retryApprovalDisposition() }
                )
            }

            if (approvalsState.approvalDispositionState?.initiationDispositionResult is Resource.Error) {
                StrikeErrorScreen(
                    errorResource = approvalsState.approvalDispositionState.initiationDispositionResult as Resource.Error<InitiationRequest.InitiateRequestBody>,
                    onDismiss = {
                        resetDataAfterErrorDismissed()
                    },
                    onRetry = { retryApprovalDisposition() }
                )
            }

            if (durableNonceState.multipleAccountsResult is Resource.Error) {
                StrikeErrorScreen(
                    errorResource = durableNonceState.multipleAccountsResult,
                    onDismiss = {
                        resetDataAfterErrorDismissed()
                    },
                    onRetry = { retryApprovalDisposition() }
                )
            }
        }
    )
    //endregion
}

//region Screen Composables
@Composable
fun ApprovalsListTopAppBar(
    title: String,
    onAppBarIconClick: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String
) {
    StrikeCenteredTopAppBar(
        title = title,
        onAppBarIconClick = { onAppBarIconClick() },
        navigationIcon = navigationIcon,
        navigationIconContentDes = navigationIconContentDes,
    )
}

@Composable
fun ApprovalsList(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onApproveClicked: (ApprovalRequest?) -> Unit,
    onMoreInfoClicked: (ApprovalRequest?) -> Unit,
    approvalRequests: List<ApprovalRequest?>,
    shouldRefreshTimers: Boolean,
    getSecondsLeftUntilCountdownIsOver: (String?, Int?) -> Long?
) {
    val context = LocalContext.current

    SwipeRefresh(
        modifier = Modifier.fillMaxSize(),
        state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
        onRefresh = { onRefresh() },
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                // Pass the SwipeRefreshState + trigger through
                state = state,
                refreshTriggerDistance = trigger,
                // Enable the scale animation
                scale = true,
                // Change the color and shape
                backgroundColor = Color.Transparent,
                contentColor = Color.White,
                largeIndication = true,
                elevation = 0.dp
            )
        }
    ) {
        //region Approvals List
        if (approvalRequests.isNullOrEmpty() && !isRefreshing) {
            ListDataEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
            ) {
                items(approvalRequests.size) { index ->
                    val walletApproval = approvalRequests[index]
                    Spacer(modifier = Modifier.height(12.dp))

                    walletApproval?.let { safeApproval ->
                        val type = safeApproval.getApprovalRequestType()
                        val rowMetaData = type.getApprovalRowMetaData(safeApproval.vaultName?.toVaultName(context))

                        val calculatedTimerSecondsLeft = getSecondsLeftUntilCountdownIsOver(
                            safeApproval.submitDate,
                            safeApproval.approvalTimeoutInSeconds
                        )
                        val timeRemainingInSeconds =
                            if (shouldRefreshTimers) calculatedTimerSecondsLeft else calculatedTimerSecondsLeft

                        if (type.isUnknownTypeOrUIUnimplemented()) {
                            UnknownApprovalItem(
                                timeRemainingInSeconds = timeRemainingInSeconds,
                                onUpdateAppClicked = {
                                    val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            Uri.parse("http://play.google.com/store/apps/details?id=com.strikeprotocols.mobile")
                                        setPackage("com.android.vending")
                                    }
                                    try {
                                        startActivity(context, playStoreIntent, null)
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(
                                            context,
                                            "Play Store not found on this device.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                        } else {
                            ApprovalRowItem(
                                timeRemainingInSeconds = timeRemainingInSeconds,
                                onApproveClicked = { onApproveClicked(approvalRequests[index]) },
                                onMoreInfoClicked = { onMoreInfoClicked(approvalRequests[index]) },
                                rowMetaData = rowMetaData,
                                positiveButtonText = safeApproval.approveButtonCaption(context)
                            ) {
                                ApprovalRowContent(
                                    type = type,
                                    approval = safeApproval
                                )
                            }

                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
//endregion

@Composable
fun ListDataEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.nothing_to_approve),
            color = GreyText,
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.pull_to_refresh),
            color = GreyText,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Icon(
            imageVector = Icons.Rounded.ArrowDownward,
            contentDescription = stringResource(R.string.content_des_pull_to_refresh_icon),
            tint = GreyText
        )
    }
}

fun checkForHardRefreshAfterBackNavigation(
    navController: NavController,
    approvalsViewModel: ApprovalsViewModel
) {
    if (navController.currentBackStackEntry?.savedStateHandle?.contains(ApprovalsViewModel.Companion.KEY_SHOULD_REFRESH_DATA) == false) {
        return
    }

    val shouldRefreshData =
        navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>(ApprovalsViewModel.Companion.KEY_SHOULD_REFRESH_DATA)
    //Have to remove data immediately or else the composable will keep grabbing it.
    navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>(ApprovalsViewModel.Companion.KEY_SHOULD_REFRESH_DATA)

    if (shouldRefreshData != null) {
        approvalsViewModel.wipeDataAfterDispositionSuccess()
    }
}
//endregion