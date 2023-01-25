package com.censocustody.android.presentation.key_recovery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.presentation.key_management.GradientBackgroundUI
import com.censocustody.android.ui.theme.CensoWhite

@Composable
fun KeyRecoveryScreen(
    navController: NavController,
    viewModel: KeyRecoveryViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {
        }
    }

    LaunchedEffect(key1 = state) { }

    GradientBackgroundUI()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Key recovery flow...",
            color = CensoWhite
        )
    }
}
