package com.censocustody.android.presentation.regeneration

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.common.Resource
import com.censocustody.android.common.popUpToTop
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.migration.MigrationUI

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