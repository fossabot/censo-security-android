package com.censocustody.android.presentation.pending_approval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.presentation.key_management.BackgroundUI

@Composable
fun PendingApprovalScreen(
    navController: NavController,
    initialData: PendingApprovalInitialData,
    viewModel: PendingApprovalViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(initialData)
        onDispose {
            viewModel.cleanUp()
        }
    }

    LaunchedEffect(key1 = state) {}

    BackgroundUI()
}