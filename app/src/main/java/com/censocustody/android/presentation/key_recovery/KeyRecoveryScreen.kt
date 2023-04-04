package com.censocustody.android.presentation.key_recovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.censocustody.android.presentation.key_management.BackgroundUI

@Composable
fun KeyRecoveryScreen(
    navController: NavController,
    initialData: KeyRecoveryInitialData,
    viewModel: KeyRecoveryViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(initialData)
        onDispose {
            viewModel.cleanUp()
        }
    }

    LaunchedEffect(key1 = state) {
    }

    BackgroundUI()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    )
}