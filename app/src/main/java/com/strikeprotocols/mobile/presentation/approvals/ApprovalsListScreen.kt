package com.strikeprotocols.mobile.presentation.approvals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.models.WalletApprovals
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.components.StrikeTopAppBar
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.HeaderBlack
import com.strikeprotocols.mobile.ui.theme.StrikeWhite
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.*

@Composable
fun ApprovalsListScreen(
    navController: NavController,
    viewModel: ApprovalsViewModel = hiltViewModel()
) {
    val state = viewModel.state

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ApprovalsListTopAppBar(
                title = stringResource(id = R.string.approvals),
                onAppBarIconClick = {},
                navigationIcon = Icons.Outlined.AccountCircle,
                navigationIconContentDes = stringResource(id = R.string.content_des_account_icon)
            )
        },
        content = {
            ApprovalsList(
                isRefreshing = state.loadingData,
                onRefresh = viewModel::refreshData,
                walletApprovals = state.walletApprovalsResult.data,
                onApproveClicked = {
                    //TODO Connect to VM
                    strikeLog(message = "Approve clicked")
                },
                onMoreInfoClicked = { navController.navigate(Screen.ApprovalDetailRoute.route) }
            )
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
    walletApprovals: WalletApprovals?,
    onApproveClicked: () -> Unit,
    onMoreInfoClicked: () -> Unit
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
                largeIndication = true
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlack),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
        ) {

            if (walletApprovals != null && !walletApprovals.approvals.isNullOrEmpty()) {
                items(walletApprovals.approvals.size) { index ->
                    Spacer(modifier = Modifier.height(12.dp))
                    ApprovalItem(
                        onApproveClicked = { onApproveClicked() },
                        onMoreInfoClicked = { onMoreInfoClicked() }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}