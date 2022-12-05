package com.censocustody.mobile.presentation.regeneration

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.common.popUpToTop
import com.censocustody.mobile.presentation.Screen
import com.censocustody.mobile.presentation.migration.MigrationUI

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