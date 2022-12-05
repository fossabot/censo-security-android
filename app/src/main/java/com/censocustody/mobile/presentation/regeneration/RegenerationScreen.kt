package com.strikeprotocols.mobile.presentation.regeneration

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.popUpToTop
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.migration.MigrationUI

@Composable
fun RegenerationScreen(
    navController: NavController,
    viewModel: RegenerationViewModel = hiltViewModel(),
) {
    val state = viewModel.state

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { }
    }

    LaunchedEffect(key1 = state) {
        if (state.finishedRegeneration) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }
            viewModel.resetAddWalletSignerCall()
        }
    }

    MigrationUI(
        errorEnabled = state.regenerationError is Resource.Success,
        retry = viewModel::retryRegeneration
    )
}