package com.strikeprotocols.mobile.presentation.approvals

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.BiometricUtil
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.components.StrikeTopAppBar
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ConfirmDispositionAlertDialog
import com.strikeprotocols.mobile.presentation.blockhash.BlockHashViewModel
import com.strikeprotocols.mobile.presentation.components.StrikeSnackbar
import com.strikeprotocols.mobile.ui.theme.*
import kotlinx.coroutines.launch
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun ApprovalsListScreen(
    navController: NavController,
    approvalsViewModel: ApprovalsViewModel = hiltViewModel(),
    blockHashViewModel: BlockHashViewModel = hiltViewModel()
) {
    val approvalsState = approvalsViewModel.state
    val blockHashState = blockHashViewModel.state

    val context = LocalContext.current as FragmentActivity

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    fun showToast(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    val promptInfo = BiometricUtil.getBasicBiometricPromptBuilder(context).build()

    val bioPrompt = BiometricUtil.createBioPrompt(
        fragmentActivity = context,
        onSuccess = {
            showToast("Authentication success")
            //Let VM handle this
            blockHashViewModel.setUserBiometricVerified(isVerified = true)
        },
        onFail = {
            showToast("Authentication failed")
            //Let VM handle this
            blockHashViewModel.setUserBiometricVerified(isVerified = false)
        }
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
        if (approvalsState.logoutResult is Resource.Success) {
            navController.navigate(Screen.SplashRoute.route) {
                popUpTo(Screen.ApprovalListRoute.route) {
                    inclusive = true
                }
            }
            approvalsViewModel.resetLogoutResource()
        }
        if (blockHashState.triggerBioPrompt) {
            blockHashViewModel.resetPromptTrigger()
            bioPrompt.authenticate(promptInfo)
        }
        if (blockHashState.recentBlockhashResult is Resource.Success) {
            strikeLog(message = "Success retrieving blockhash. BlockHash: ${blockHashState.blockHash?.blockHashString} ")
            if (blockHashState.blockHash != null) {
                approvalsViewModel.setBlockHash(blockHashState.blockHash)
                blockHashViewModel.resetState()
            }
        }
        if (blockHashState.recentBlockhashResult is Resource.Error) {
            strikeLog(message = "Failure retrieving blockhash")
            //TODO: Handle failure
        }
        if (approvalsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success) {
            showToast("registered approval disposition")
            approvalsViewModel.resetApprovalDispositionAPICalls()
            approvalsViewModel.refreshData()
        }
        if (approvalsState.approvalDispositionState?.registerApprovalDispositionResult is Resource.Error) {
            showToast("Failed to registered approval disposition")
            approvalsViewModel.resetApprovalDispositionAPICalls()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        snackbarHost = { scaffoldState.snackbarHostState },
        topBar = {
            ApprovalsListTopAppBar(
                title = stringResource(id = R.string.approvals),
                onAppBarIconClick = {},
                navigationIcon = Icons.Outlined.AccountCircle,
                navigationIconContentDes = stringResource(id = R.string.content_des_account_icon),
                onLogout = approvalsViewModel::logout
            )
        },
        content = { innerPadding ->
            ApprovalsList(
                isRefreshing = approvalsState.loadingData,
                onRefresh = approvalsViewModel::refreshData,
                onApproveClicked = { approval ->
                    strikeLog(message = "Approve clicked")
                    approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
                        approval = approval,
                        isApproving = true,
                        dialogTitle = "Confirm Approval",
                        dialogText = "Please confirm you want to approve this transfer"
                    )
                },
                onMoreInfoClicked = { approval ->
                    approval?.let { safeApproval ->
                        navController.navigate("${Screen.ApprovalDetailRoute.route}/${WalletApproval.toJson(safeApproval)}" )
                    }
                },
                walletApprovals = approvalsState.approvals
            )
            
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxHeight()
            ) {
                StrikeSnackbar(
                    snackbarHostState = scaffoldState.snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (approvalsState.shouldDisplayConfirmDispositionDialog != null) {
                approvalsState.shouldDisplayConfirmDispositionDialog.let { safeDialogDetails ->
                    ConfirmDispositionAlertDialog(
                        dialogTitle = safeDialogDetails.dialogTitle,
                        dialogText = safeDialogDetails.dialogText,
                        onConfirm = {
                            strikeLog(message = "Confirming disposition")
                            approvalsViewModel.resetShouldDisplayConfirmDispositionDialog()
                            blockHashViewModel.setPromptTrigger()
                        },
                        onDismiss = {
                            strikeLog(message = "Dismissing dialog")
                            approvalsViewModel.resetShouldDisplayConfirmDispositionDialog()
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun ApprovalsListTopAppBar(
    title: String,
    onAppBarIconClick: () -> Unit,
    onLogout: () -> Unit,
    navigationIcon: ImageVector,
    navigationIconContentDes: String
) {
    StrikeTopAppBar(
        title = title,
        onAppBarIconClick = { onAppBarIconClick() },
        navigationIcon = navigationIcon,
        navigationIconContentDes = navigationIconContentDes,
        actions = {
            TextButton(onClick = onLogout) {
                Text(stringResource(R.string.logout), color = StrikeWhite)
            }
        }
    )
}

@Composable
fun ApprovalsList(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onApproveClicked: (WalletApproval?) -> Unit,
    onMoreInfoClicked: (WalletApproval?) -> Unit,
    walletApprovals: List<WalletApproval?>
) {
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
                    ApprovalItem(
                        onApproveClicked = { onApproveClicked(walletApprovals[index]) },
                        onMoreInfoClicked = { onMoreInfoClicked(walletApprovals[index]) },
                        timeRemainingInSeconds = walletApproval?.approvalTimeoutInSeconds ?: 0
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

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