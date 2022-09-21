package com.strikeprotocols.mobile.presentation.entrance

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.AndroidUriWrapper
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.components.StrikeErrorScreen
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementFlow
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementInitialData
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.StrikeWhite

@Composable
fun EntranceScreen(
    navController: NavController,
    viewModel: EntranceViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { }
    }

    LaunchedEffect(key1 = state) {
        if (state.userDestinationResult is Resource.Success) {
            val userDestinationRoute = when (state.userDestinationResult.data) {
                UserDestination.HOME -> Screen.ApprovalListRoute.route
                UserDestination.KEY_MANAGEMENT_REGENERATION,
                UserDestination.KEY_MANAGEMENT_RECOVERY,
                UserDestination.KEY_MIGRATION,
                UserDestination.KEY_MANAGEMENT_CREATION -> {

                    val flow = when (state.userDestinationResult.data) {
                        UserDestination.KEY_MANAGEMENT_CREATION -> KeyManagementFlow.KEY_CREATION
                        UserDestination.KEY_MANAGEMENT_RECOVERY -> KeyManagementFlow.KEY_RECOVERY
                        UserDestination.KEY_MANAGEMENT_REGENERATION -> KeyManagementFlow.KEY_REGENERATION
                        UserDestination.KEY_MIGRATION -> KeyManagementFlow.KEY_MIGRATION
                        else -> KeyManagementFlow.KEY_CREATION
                    }

                    val keyManagementInitialData = KeyManagementInitialData(
                        verifyUserDetails = state.verifyUserResult.data,
                        walletSigners = state.walletSignersResult.data,
                        flow = flow
                    )

                    val keyManagementJson =
                        KeyManagementInitialData.toJson(
                            keyManagementInitialData,
                            AndroidUriWrapper()
                        )
                    "${Screen.KeyManagementRoute.route}/$keyManagementJson"
                }
                UserDestination.INVALID_KEY -> Screen.ContactStrikeRoute.route
                UserDestination.LOGIN, null -> Screen.SignInRoute.route
            }

            viewModel.resetUserDestinationResult()

            val currentDestinationRoute = navController.currentDestination?.route
            if (currentDestinationRoute != Screen.EnforceUpdateRoute.route
                && currentDestinationRoute != Screen.BiometryDisabledRoute.route
            ) {
                navController.navigate(userDestinationRoute) {
                    launchSingleTop = true
                    navController.backQueue.clear()
                }
            } else {
                val messageForUser =
                    if (currentDestinationRoute == Screen.EnforceUpdateRoute.route) {
                        context.getString(R.string.update_app_entrance_message)
                    } else {
                        context.getString(R.string.update_biometry_entrance_message)
                    }
                Toast.makeText(context, messageForUser, Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundBlack)
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 172.dp, start = 44.dp, end = 44.dp),
            painter = painterResource(R.drawable.strike_main_logo),
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
        )
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .size(60.dp),
            color = StrikeWhite
        )
    }

    if (state.verifyUserResult is Resource.Error) {
        StrikeErrorScreen(
            errorResource = state.verifyUserResult,
            onDismiss = {
                //since this screen is just loading UI, we always want to retry.
                viewModel.retryRetrieveVerifyUserDetails()
            },
            onRetry = {
                viewModel.retryRetrieveVerifyUserDetails()
            }
        )
    }

    if (state.walletSignersResult is Resource.Error) {
        StrikeErrorScreen(
            errorResource = state.walletSignersResult,
            onDismiss = {
                //since this screen is just loading UI, we always want to retry.
                viewModel.retryRetrieveVerifyUserDetails()
            },
            onRetry = {
                viewModel.retryRetrieveVerifyUserDetails()
            }
        )
    }
}