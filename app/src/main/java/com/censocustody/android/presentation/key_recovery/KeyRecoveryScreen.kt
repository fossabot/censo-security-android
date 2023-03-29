package com.censocustody.android.presentation.key_recovery

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun KeyRecoveryScreen(
    navController: NavController,
    initialData: KeyRecoveryInitialData,
    viewModel: KeyRecoveryViewModel = hiltViewModel()
) {

    val state = viewModel.state

    val context = LocalContext.current as FragmentActivity

    Box {
        Text(text = "Holding for recovery...")
    }
}