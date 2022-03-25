package com.strikeprotocols.mobile.presentation.approvals

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.components.StrikeTopAppBar
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.components.StrikeSnackbar
import com.strikeprotocols.mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ApprovalsListScreen(
    navController: NavController,
    viewModel: ApprovalsViewModel = hiltViewModel()
) {
    val state = viewModel.state

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    //region DisposableEffect
    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }
    //endregion

    val snackbarRefreshErrorString = stringResource(R.string.snackbar_refresh_error)

    LaunchedEffect(key1 = state) {
        if (state.shouldShowErrorSnackbar) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = snackbarRefreshErrorString
                )
            }
            viewModel.resetShouldShowErrorSnackbar()
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
                navigationIconContentDes = stringResource(id = R.string.content_des_account_icon)
            )
        },
        content = { innerPadding ->
            ApprovalsList(
                isRefreshing = state.loadingData,
                onRefresh = viewModel::refreshData,
                onApproveClicked = {
                    //TODO Connect to VM
                    strikeLog(message = "Approve clicked")
                },
                onMoreInfoClicked = { approval ->
                    approval?.let { safeApproval ->
                        navController.navigate("${Screen.ApprovalDetailRoute.route}/${WalletApproval.toJson(safeApproval)}" )
                    }
                },
                walletApprovals = state.approvals
            )
            
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxHeight()
            ) {
                StrikeSnackbar(
                    snackbarHostState = scaffoldState.snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    )
}

@Composable
fun ApprovalsListTopAppBar(
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
fun ApprovalsList(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onApproveClicked: () -> Unit,
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
                        onApproveClicked = { onApproveClicked() },
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