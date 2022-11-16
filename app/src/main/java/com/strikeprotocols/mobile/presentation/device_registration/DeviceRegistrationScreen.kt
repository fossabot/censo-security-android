package com.strikeprotocols.mobile.presentation.device_registration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun DeviceRegistrationScreen(
    navController: NavController,
    viewModel: DeviceRegistrationViewModel = hiltViewModel(),
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Key Device Registration", color = StrikeWhite)
    }
}