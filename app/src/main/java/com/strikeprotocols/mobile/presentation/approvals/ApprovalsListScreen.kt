package com.strikeprotocols.mobile.presentation.approvals

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.*
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.approvals.approval_type_row_items.*
import com.strikeprotocols.mobile.presentation.components.*
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import com.strikeprotocols.mobile.ui.theme.*
import kotlinx.coroutines.launch
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack

@Composable
fun ApprovalsListScreen(
    navController: NavController,
    approvalsViewModel: ApprovalsViewModel,
    durableNonceViewModel: DurableNonceViewModel = hiltViewModel()
) {
    val approvalsState = approvalsViewModel.state
    val blockHashState = durableNonceViewModel.state

    val context = LocalContext.current as FragmentActivity

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val promptInfo = BiometricUtil.getBasicBiometricPromptBuilder(context).build()

    val bioPrompt = BiometricUtil.createBioPrompt(
        fragmentActivity = context,
        onSuccess = {
            val nonceAddresses = approvalsState.selectedApproval?.retrieveAccountAddresses()
            nonceAddresses?.let {
                durableNonceViewModel.setNonceAccountAddresses(nonceAddresses)
            }
            durableNonceViewModel.setUserBiometricVerified(isVerified = true)
        },
        onFail = {
            durableNonceViewModel.setUserBiometricVerified(isVerified = false)
        }
    )

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

    val snackbarRefreshErrorString = stringResource(R.string.snackbar_refresh_error)

    LaunchedEffect(key1 = approvalsState, key2 = blockHashState) {
        if (approvalsState.shouldShowErrorSnackbar) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = snackbarRefreshErrorString
                )
            }
            approvalsViewModel.resetShouldShowErrorSnackbar()
        }

        if (blockHashState.triggerBioPrompt) {
            durableNonceViewModel.resetPromptTrigger()
            bioPrompt.authenticate(promptInfo)
        }
        if (blockHashState.multipleAccountsResult is Resource.Success) {
            if (blockHashState.multipleAccounts != null) {
                approvalsViewModel.setMultipleAccounts(blockHashState.multipleAccounts)
                durableNonceViewModel.resetState()
            }
        }
        if (approvalsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success ||
            approvalsState.approvalDispositionState?.initiationDispositionResult is Resource.Success) {
            approvalsViewModel.wipeDataAfterDispositionSuccess()
        }
        if (approvalsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Error ||
            approvalsState.approvalDispositionState?.initiationDispositionResult is Resource.Error) {
            approvalsViewModel.setShouldDisplayApprovalDispositionError()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_START
            -> {
                approvalsViewModel.refreshData()
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
                isRefreshing = approvalsState.loadingData || blockHashState.isLoading,
                onRefresh = approvalsViewModel::refreshData,
                onApproveClicked = { approval ->
                    approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
                        approval = approval,
                        isApproving = true,
                        dialogTitle = approval?.getSolanaApprovalRequestType()?.getApprovalTypeDialogTitle(context)
                            ?: UnknownApprovalType.getApprovalTypeDialogTitle(context),
                        dialogText = approval?.getSolanaApprovalRequestType()?.getDialogFullMessage(
                            context = context,
                            approvalDisposition = ApprovalDisposition.APPROVE,
                            initiationRequest = approval.isInitiationRequest()
                        )
                            ?: UnknownApprovalType.getDialogFullMessage(
                                context = context,
                                approvalDisposition = ApprovalDisposition.APPROVE,
                                initiationRequest = approval?.isInitiationRequest() == true
                            )
                    )
                },
                onMoreInfoClicked = { approval ->
                    approval?.let { safeApproval ->
                        navController.navigate("${Screen.ApprovalDetailRoute.route}/${WalletApproval.toJson(safeApproval, AndroidUriWrapper())}" )
                    }
                },
                walletApprovals = approvalsState.approvals,
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
                if (approvalsState.selectedApproval?.getSolanaApprovalRequestType() is LoginApprovalRequest) {
                    approvalsViewModel.resetShouldDisplayConfirmDisposition()
                    durableNonceViewModel.setPromptTrigger()
                } else {
                    approvalsState.shouldDisplayConfirmDisposition.let { safeDialogDetails ->
                        StrikeConfirmDispositionAlertDialog(
                            dialogTitle = safeDialogDetails.dialogTitle,
                            dialogText = safeDialogDetails.dialogText,
                            onConfirm = {
                                approvalsViewModel.resetShouldDisplayConfirmDisposition()
                                durableNonceViewModel.setPromptTrigger()
                            },
                            onDismiss = {
                                approvalsViewModel.resetShouldDisplayConfirmDisposition()
                            }
                        )
                    }
                }
            }

            if (approvalsState.shouldDisplayApprovalDispositionError) {
                val approvalDispositionError = approvalsState.approvalDispositionState?.approvalDispositionError
                approvalDispositionError?.let { safeApprovalDispositionError ->
                    val dialogErrorText = retrieveApprovalDispositionDialogErrorText(safeApprovalDispositionError, context)
                    StrikeApprovalDispositionErrorAlertDialog(
                        dialogTitle = stringResource(R.string.approval_disposition_error_title),
                        dialogText = dialogErrorText,
                        onConfirm = {
                            approvalsViewModel.dismissApprovalDispositionError()
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
    onApproveClicked: (WalletApproval?) -> Unit,
    onMoreInfoClicked: (WalletApproval?) -> Unit,
    walletApprovals: List<WalletApproval?>,
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
        if (walletApprovals.isNullOrEmpty() && !isRefreshing) {
            ListDataEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
            ) {
                items(walletApprovals.size) { index ->
                    val walletApproval = walletApprovals[index]
                    Spacer(modifier = Modifier.height(12.dp))

                    walletApproval?.let { safeApproval ->
                        val type = safeApproval.getSolanaApprovalRequestType()
                        val rowMetaData = type.getApprovalRowMetaData(LocalContext.current)

                        val calculatedTimerSecondsLeft = getSecondsLeftUntilCountdownIsOver(safeApproval.submitDate, safeApproval.approvalTimeoutInSeconds)
                        val timeRemainingInSeconds = if (shouldRefreshTimers) calculatedTimerSecondsLeft else calculatedTimerSecondsLeft

                        if (type.isUnknownTypeOrUIUnimplemented()) {
                            UnknownApprovalItem(
                                timeRemainingInSeconds = timeRemainingInSeconds,
                                accountRowMetaData = rowMetaData,
                                onUpdateAppClicked = {
                                    val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            Uri.parse("http://play.google.com/store/apps/details?id=com.strikeprotocols.mobile")
                                        setPackage("com.android.vending")
                                    }
                                    try {
                                        startActivity(context, playStoreIntent, null)
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(context, "Play Store not found on this device.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        } else {
                            ApprovalRowItem(
                                timeRemainingInSeconds = timeRemainingInSeconds,
                                onApproveClicked = { onApproveClicked(walletApprovals[index]) },
                                onMoreInfoClicked = { onMoreInfoClicked(walletApprovals[index]) },
                                rowMetaData = rowMetaData,
                                positiveButtonText = safeApproval.approveButtonCaption(context)
                            ) {
                                ApprovalRowContent(
                                    type = type
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