package com.censocustody.android.presentation.entrance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.common.AndroidUriWrapper
import com.censocustody.android.common.Resource
import com.censocustody.android.common.popUpToTop
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.components.CensoErrorScreen
import com.censocustody.android.R
import com.censocustody.android.presentation.device_registration.DeviceRegistrationInitialData
import com.censocustody.android.presentation.key_creation.KeyCreationInitialData
import com.censocustody.android.presentation.key_recovery.KeyRecoveryInitialData
import com.censocustody.android.presentation.sign_in.SignInAlertDialog
import com.censocustody.android.ui.theme.*

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
                        verifyUser = state.verifyUserResult.data,
                        recoveryType = state.recoveryType.data ?: RecoveryType.DEVICE
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

    if (state.displayOrgRecoveryDialog is Resource.Success) {
        OrgRecoveryDialog(
            organizationSelected = { viewModel.userSelectedOrgRecoveryType(RecoveryType.ORGANIZATION) },
            thisDeviceSelected = { viewModel.userSelectedOrgRecoveryType(RecoveryType.DEVICE) },
            onDismiss = viewModel::userDismissedRecoveryTypeDialog
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


@Composable
fun OrgRecoveryDialog(
    organizationSelected: () -> Unit,
    thisDeviceSelected: () -> Unit,
    onDismiss: () -> Unit,
) {
    val upperInteractionSource = remember { MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .padding(4.dp)
            .clickable(indication = null, interactionSource = upperInteractionSource) { },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val innerInteractionSource = remember { MutableInteractionSource() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(color = Color.Transparent)
                .border(
                    width = 1.0.dp,
                    shape = RoundedCornerShape(4.dp),
                    color = BorderGrey,
                )
                .shadow(elevation = 2.5.dp)
                .clickable(indication = null, interactionSource = innerInteractionSource) { },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DialogMainBackground,
                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp),
                    )
                    .padding(vertical = 16.dp),
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    text = stringResource(R.string.org_recovery_dialog_title),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = TextBlack,
                    fontSize = 24.sp
                )
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    onClick = onDismiss
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_dialog),
                        tint = TextBlack
                    )
                }
            }
            Divider(thickness = 2.dp, color = BorderGrey)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(
                    color = DialogMainBackground,
                    shape = RoundedCornerShape(0.dp, 0.dp, 4.dp, 4.dp),
                )
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 48.dp),
                    text = stringResource(R.string.org_recovery_dialog_message),
                    textAlign = TextAlign.Center,
                    color = TextBlack,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val modifier: Modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .weight(1.35f)
                        .fillMaxWidth()
                    val textAlign = TextAlign.Center
                    Spacer(modifier = Modifier.weight(0.20f))
                    Button(
                        modifier = modifier,
                        onClick = thisDeviceSelected
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 2.dp),
                            fontSize = 18.sp,
                            text = stringResource(R.string.this_device),
                            color = CensoWhite,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.20f))
                    Button(
                        modifier = modifier,
                        onClick = organizationSelected,
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 2.dp),
                            text = stringResource(R.string.organization),
                            fontSize = 18.sp,
                            color = CensoWhite,
                            textAlign = textAlign
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.20f))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}