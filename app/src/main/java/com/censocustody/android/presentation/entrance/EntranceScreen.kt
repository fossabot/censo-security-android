package com.censocustody.android.presentation.entrance

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.common.AndroidUriWrapper
import com.censocustody.android.common.Resource
import com.censocustody.android.common.popUpToTop
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.components.CensoErrorScreen
import com.censocustody.android.ui.theme.BackgroundWhite
import com.censocustody.android.R
import com.censocustody.android.presentation.device_registration.DeviceRegistrationInitialData
import com.censocustody.android.presentation.key_creation.KeyCreationInitialData
import com.censocustody.android.presentation.key_recovery.KeyRecoveryInitialData
import com.censocustody.android.ui.theme.ButtonRed

@Composable
fun EntranceScreen(
    navController: NavController,
    viewModel: EntranceViewModel = hiltViewModel(),
) {
    val state = viewModel.state

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { }
    }

    LaunchedEffect(key1 = state) {
        if (state.userDestinationResult is Resource.Success) {
            val userDestinationRoute = when (state.userDestinationResult.data) {
                UserDestination.HOME -> Screen.ApprovalListRoute.route
                UserDestination.UPLOAD_KEYS -> Screen.UploadKeysRoute.route
                UserDestination.RE_AUTHENTICATE -> Screen.ReAuthenticateRoute.route
                UserDestination.KEY_MANAGEMENT_CREATION -> {
                    val keyCreationInitialData = KeyCreationInitialData(
                        verifyUserDetails = state.verifyUserResult.data,
                        bootstrapUserDeviceImageURI = state.bootstrapImageUrl
                    )

                    val keyCreationJson =
                        KeyCreationInitialData.toJson(
                            keyCreationInitialData,
                            AndroidUriWrapper()
                        )
                    "${Screen.KeyCreationRoute.route}/$keyCreationJson"
                }
                UserDestination.PENDING_APPROVAL -> {
                    Screen.PendingApprovalRoute.route
                }
                UserDestination.KEY_MANAGEMENT_RECOVERY -> {

                    val keyRecoveryInitialData = KeyRecoveryInitialData(
                        verifyUserDetails = state.verifyUserResult.data,
                    )

                    val keyRecoveryJson =
                        KeyRecoveryInitialData.toJson(
                            keyRecoveryInitialData,
                            AndroidUriWrapper()
                        )
                    "${Screen.KeyRecoveryRoute.route}/$keyRecoveryJson"
                }
                UserDestination.DEVICE_REGISTRATION -> {
                    val deviceRegistrationInitialData = DeviceRegistrationInitialData(
                        bootstrapUser = state.verifyUserResult.data != null && state.verifyUserResult.data.shardingPolicy == null,
                        verifyUser = state.verifyUserResult.data
                    )

                    val deviceRegistrationJson =
                        DeviceRegistrationInitialData.toJson(
                            deviceRegistrationInitialData,
                            AndroidUriWrapper()
                        )
                    "${Screen.DeviceRegistrationRoute.route}/$deviceRegistrationJson"
                }
                UserDestination.FORCE_UPDATE -> Screen.EnforceUpdateRoute.route
                UserDestination.INVALID_KEY -> Screen.ContactCensoRoute.route
                UserDestination.LOGIN, null -> Screen.SignInRoute.route
            }

            viewModel.resetUserDestinationResult()

            navController.navigate(userDestinationRoute) {
                launchSingleTop = true
                popUpToTop()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundWhite)
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 172.dp, start = 44.dp, end = 44.dp),
            painter = painterResource(R.drawable.censo_horizontal_ko),
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
        )
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .size(60.dp),
            color = ButtonRed
        )
    }

    if (state.verifyUserResult is Resource.Error) {
        CensoErrorScreen(
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
}