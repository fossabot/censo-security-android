package com.censocustody.android.presentation.approvals

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.raygun.raygun4android.RaygunClient
import com.censocustody.android.R
import com.censocustody.android.common.*
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.util.sendError
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.approvals.approval_type_row_items.*
import com.censocustody.android.presentation.components.*
import com.censocustody.android.ui.theme.*

@Composable
fun ApprovalsListScreen(
    navController: NavController,
    approvalsViewModel: ApprovalsViewModel
) {
    val approvalsState = approvalsViewModel.state

    fun resetDataAfterErrorDismissed() {
        approvalsViewModel.dismissApprovalDispositionError()
    }

    val context = LocalContext.current as FragmentActivity

    val promptInfo = BioCryptoUtil.createPromptInfo(context = context)

    val scaffoldState = rememberScaffoldState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    fun checkPermissionDialog() {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                val notificationGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    )

                val shownPermissionJustOnceBefore = shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )

                val seenDialogBefore = approvalsViewModel.userHasSeenPushDialog()

                if (notificationGranted != PackageManager.PERMISSION_GRANTED) {
                    if (shownPermissionJustOnceBefore && !seenDialogBefore) {
                        //show dialog to user because they have rejected permissions once before
                        approvalsViewModel.setUserSeenPushDialog(true)
                        approvalsViewModel.triggerPushNotificationDialog()
                    } else if (!seenDialogBefore) {
                        //show permission to user for first time with no dialog
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        } catch (e: Exception) {
            e.sendError(CrashReportingUtil.PUSH_NOTIFICATION_PERMISSION_TAG)
        }
    }

    fun retryApprovalDisposition() {
        approvalsViewModel.dismissApprovalDispositionError()
        approvalsState.selectedApproval?.let { approval ->
            approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
                approval = approval,
                isApproving = true,
                dialogMessages = approval.details.getDialogMessages(
                    context = context,
                    approvalDisposition = ApprovalDisposition.APPROVE,
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

    LaunchedEffect(key1 = approvalsState) {

        if (approvalsState.bioPromptTrigger is Resource.Success) {

            val bioPrompt = BioCryptoUtil.createBioPrompt(
                fragmentActivity = context,
                onSuccess = { approvalsViewModel.biometryApproved() },
                onFail = {
                    BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                        resetDataAfterErrorDismissed()
                    }
                }
            )

            bioPrompt.authenticate(promptInfo)

            approvalsViewModel.resetPromptTrigger()
        }

        if (approvalsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success) {
            approvalsViewModel.wipeDataAfterDispositionSuccess()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_START
            -> {
                approvalsViewModel.handleScreenForegrounded()
                checkPermissionDialog()
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
            ApprovalsTopBar(
                title = stringResource(id = R.string.approvals),
                onAppBarIconClick = { navController.navigate(Screen.AccountRoute.route) },
                navigationIcon = Icons.Rounded.AccountCircle,
                navigationIconContentDes = stringResource(id = R.string.content_des_account_icon),
                onQrScanClick = { navController.navigate(Screen.ScanQRRoute.route) },
            )
        },
        content = { innerPadding ->
            ApprovalsList(
                isRefreshing = approvalsState.loadingData,
                onRefresh = approvalsViewModel::refreshData,
                onApproveClicked = { approval ->
                    approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
                        approval = approval,
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
                onMoreInfoClicked = { approval ->
                    approval?.let { safeApproval ->
                        navController.navigate("${Screen.ApprovalDetailRoute.route}/${ApprovalRequestV2.toJson(safeApproval, AndroidUriWrapper())}" )
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
                CensoSnackbar(
                    snackbarHostState = scaffoldState.snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (approvalsState.shouldDisplayConfirmDisposition != null) {
                if (approvalsState.selectedApproval?.details is ApprovalRequestDetailsV2.Login) {
                    approvalsViewModel.resetShouldDisplayConfirmDisposition()
                    approvalsViewModel.triggerBioPrompt()
                } else {
                    CensoConfirmDispositionAlertDialog(
                        dialogMessages = approvalsState.shouldDisplayConfirmDisposition.dialogMessages,
                        onConfirm = {
                            approvalsViewModel.resetShouldDisplayConfirmDisposition()
                            approvalsViewModel.triggerBioPrompt()
                        },
                        onDismiss = {
                            approvalsViewModel.resetShouldDisplayConfirmDisposition()
                        }
                    )
                }
            }

            if (approvalsState.showPushNotificationsDialog is Resource.Success) {
                PushNotificationDialog(
                    text = stringResource(id = R.string.push_notification_never_dialog),
                    onAccept = {
                        approvalsViewModel.resetPushNotificationDialog()
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onDismiss = {
                        approvalsViewModel.setUserSeenPushDialog(false)
                        approvalsViewModel.resetPushNotificationDialog()
                    }
                )
            }

            if (approvalsState.approvalsResultRequest is Resource.Error) {
                CensoErrorScreen(
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
                CensoErrorScreen(
                    errorResource = approvalsState.approvalDispositionState.registerApprovalDispositionResult as Resource.Error<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body>,
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
fun NavIconTopBar(
    title: String,
    onAppBarIconClick: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String,
    showNavIcon: Boolean = true
) {
    CenteredTopAppBar(
        title = title,
        backgroundColor = BackgroundGrey,
        onAppBarIconClick = { onAppBarIconClick() },
        navigationIcon = navigationIcon,
        showNavIcon = showNavIcon,
        navigationIconContentDes = navigationIconContentDes,
    )
}

//region Screen Composables
@Composable
fun ApprovalsTopBar(
    title: String,
    onAppBarIconClick: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String,
    onQrScanClick: () -> Unit
) {
    CenteredTopAppBar(
        title = title,
        backgroundColor = BackgroundGrey,
        onAppBarIconClick = { onAppBarIconClick() },
        navigationIcon = navigationIcon,
        navigationIconContentDes = navigationIconContentDes,
        actions = {
            Box(modifier = Modifier
                .fillMaxHeight()
                .clickable { onQrScanClick() }
                .padding(horizontal = 12.dp)
                .align(alignment = Alignment.CenterVertically)
            ) {
                Icon(
                    modifier = Modifier.size(24.dp).align(Alignment.Center),
                    imageVector = Icons.Rounded.QrCode,
                    tint = TextBlack,
                    contentDescription = stringResource(R.string.scan_qr_code_cont_desc)
                )
            }
        }
    )
}

@Composable
fun ApprovalsList(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onApproveClicked: (ApprovalRequestV2?) -> Unit,
    onMoreInfoClicked: (ApprovalRequestV2?) -> Unit,
    approvalRequests: List<ApprovalRequestV2?>,
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
                backgroundColor = Color.White,
                contentColor = ButtonRed,
                largeIndication = true,
                elevation = 0.dp
            )
        }
    ) {
        //region Approvals List
        if (approvalRequests.isEmpty() && !isRefreshing) {
            ListDataEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundGrey),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
            ) {
                items(approvalRequests.size) { index ->
                    val walletApproval = approvalRequests[index]
                    Spacer(modifier = Modifier.height(16.dp))

                    walletApproval?.let { safeApproval ->
                        val type = safeApproval.details
                        val rowMetaData = type.getApprovalRowMetaData(safeApproval.vaultName, context)

                        val calculatedTimerSecondsLeft = getSecondsLeftUntilCountdownIsOver(
                            safeApproval.submitDate,
                            safeApproval.approvalTimeoutInSeconds?.toInt()
                        )
                        val timeRemainingInSeconds =
                            if (shouldRefreshTimers) calculatedTimerSecondsLeft else calculatedTimerSecondsLeft

                        val showApprovalFromList = !safeApproval.details.isDAppRequest()

                        if (type.isUnknownTypeOrUIUnimplemented()) {
                            UnknownApprovalItem(
                                timeRemainingInSeconds = timeRemainingInSeconds,
                                onUpdateAppClicked = {
                                    val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            Uri.parse("http://play.google.com/store/apps/details?id=com.censocustody.android")
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
                                positiveButtonText = safeApproval.approveButtonCaption(context),
                                showApprovalButton = showApprovalFromList
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
            .background(BackgroundGrey)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.nothing_to_approve),
            textAlign = TextAlign.Center,
            color = DarkGreyText,
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.pull_to_refresh),
            color = DarkGreyText,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Icon(
            imageVector = Icons.Rounded.ArrowDownward,
            contentDescription = stringResource(R.string.content_des_pull_to_refresh_icon),
            tint = DarkGreyText
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

@Composable
fun PushNotificationDialog(
    text: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(width = 1.dp, color = UnfocusedGrey.copy(alpha = 0.50f))
                .background(color = DialogMainBackground)
                .shadow(elevation = 2.5.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = text,
                textAlign = TextAlign.Center,
                color = TextBlack,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(36.dp))
            Row {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(id = R.string.skip),
                        fontSize = 18.sp,
                        color = TextBlack,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = onAccept,
                ) {
                    Text(
                        text = stringResource(id = R.string.continue_text),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 18.sp,
                        color = CensoWhite,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}