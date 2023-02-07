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
import com.censocustody.android.presentation.key_management.KeyManagementFlow
import com.censocustody.android.presentation.key_management.KeyManagementInitialData
import com.censocustody.android.ui.theme.BackgroundBlack
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.R

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
                UserDestination.UPLOAD_KEYS -> Screen.KeyCreationRoute.route
                UserDestination.KEY_MANAGEMENT_CREATION -> Screen.KeyCreationRoute.route
                UserDestination.KEY_MANAGEMENT_RECOVERY -> {

                    val flow = when (state.userDestinationResult.data) {
                        UserDestination.KEY_MANAGEMENT_CREATION -> KeyManagementFlow.KEY_CREATION
                        UserDestination.KEY_MANAGEMENT_RECOVERY -> KeyManagementFlow.KEY_RECOVERY
                        else -> KeyManagementFlow.KEY_CREATION
                    }

                    val keyManagementInitialData = KeyManagementInitialData(
                        verifyUserDetails = state.verifyUserResult.data,
                        flow = flow
                    )

                    val keyManagementJson =
                        KeyManagementInitialData.toJson(
                            keyManagementInitialData,
                            AndroidUriWrapper()
                        )
                    "${Screen.KeyManagementRoute.route}/$keyManagementJson"
                }
                UserDestination.DEVICE_REGISTRATION -> Screen.DeviceRegistrationRoute.route
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
            .background(color = BackgroundBlack)
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
            color = CensoWhite
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