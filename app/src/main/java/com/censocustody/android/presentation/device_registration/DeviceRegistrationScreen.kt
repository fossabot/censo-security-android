package com.censocustody.android.presentation.device_registration

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.*
import com.censocustody.android.common.ui.ImageCaptureError
import com.censocustody.android.common.ui.getCameraProvider
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.entrance.UserType
import com.censocustody.android.presentation.key_management.BackgroundUI
import com.censocustody.android.presentation.key_management.SmallAuthFlowButton
import com.censocustody.android.ui.theme.*

@Composable
fun DeviceRegistrationScreen(
    navController: NavController,
    initialData: DeviceRegistrationInitialData,
    viewModel: DeviceRegistrationViewModel = hiltViewModel()
) {

    val state = viewModel.state

    val context = LocalContext.current as FragmentActivity

    fun getErrorMessage(
        context: Context,
        deviceRegistrationError: DeviceRegistrationError,
        imageCaptureError: ImageCaptureError? = null
    ) = when (deviceRegistrationError) {
        DeviceRegistrationError.NONE -> context.getString(R.string.image_capture_failed_default_message)
        DeviceRegistrationError.API -> context.getString(R.string.device_registration_api_error)
        DeviceRegistrationError.BOOTSTRAP -> context.getString(R.string.bootstrap_failure)
        DeviceRegistrationError.IMAGE_CAPTURE -> {
            when (imageCaptureError) {
                ImageCaptureError.NO_HARDWARE_CAMERA ->
                    context.getString(R.string.image_capture_failed_no_camera_message)
                ImageCaptureError.BAD_RESULT ->
                    context.getString(R.string.image_capture_failed_default_message)
                ImageCaptureError.ACTION_CANCELLED ->
                    context.getString(R.string.image_capture_failed_action_cancelled_message)
                null -> {
                    context.getString(R.string.image_capture_failed_default_message)
                }
            }
        }
        DeviceRegistrationError.SIGNING_IMAGE -> context.getString(R.string.signing_image_error)
        DeviceRegistrationError.BIOMETRY -> context.getString(R.string.biometry_failed_device_registration_error)
    }

    //region Launched Effects
    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(initialData)
        onDispose { }
    }

    LaunchedEffect(key1 = state) {

        if (!state.userLoggedIn || state.kickUserToEntrance || state.addUserDevice is Resource.Success) {
            navController.navigate(Screen.EntranceRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }
            viewModel.resetUserDevice()
            viewModel.resetKickUserOut()
            viewModel.resetUserLoggedIn()
        }

        if (state.triggerBioPrompt is Resource.Success) {
            viewModel.resetPromptTrigger()

            val promptInfo = BioCryptoUtil.createPromptInfo(context = context)

            val bioPrompt = BioCryptoUtil.createBioPrompt(
                fragmentActivity = context,
                onSuccess = { viewModel.biometryApproved() },
                onFail = {
                    BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                        viewModel.biometryFailed()
                    }
                }
            )

            bioPrompt.authenticate(promptInfo)
        }

        if (state.triggerImageCapture is Resource.Success) {
            val cameraProvider = context.getCameraProvider()
            try {
                cameraProvider.unbindAll()
            } catch (ex: Exception) {

            }
            viewModel.resetTriggerImageCapture()
            viewModel.imageCaptured()
        }
    }

    //endregion

    BackgroundUI()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.deviceRegistrationError != DeviceRegistrationError.NONE) {
            val message = getErrorMessage(
                context = context,
                deviceRegistrationError = state.deviceRegistrationError,
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    color = TextBlack,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.23.sp,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                SmallAuthFlowButton(
                    modifier = Modifier.wrapContentWidth(),
                    text = stringResource(R.string.retry),
                ) {
                    viewModel.retry()
                }
            }

        } else if (state.triggerImageCapture is Resource.Loading) {
            CaptureUserImageContent(
                onImageCaptureSuccess = { bitmap, fileUrl ->
                    viewModel.capturedUserPhotoSuccess(bitmap, fileUrl)
                },
                onImageCaptureError = {
                    viewModel.capturedUserPhotoError(it)
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .shadow(
                        elevation = 5.dp,
                    )
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = BackgroundGrey),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.capturingDeviceKey is Resource.Uninitialized) {
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = when(state.userType) {
                            UserType.STANDARD -> "STANDARD"
                            UserType.ORGANIZATION -> "ORGANIZATION RECOVERY"
                            UserType.BOOTSTRAP -> "BOOTSTRAP"
                        },
                        textAlign = TextAlign.Center,
                        color = TextBlack,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    CensoButton(
                        onClick = { viewModel.triggerImageCapture() },
                        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 28.dp)
                    ) {
                        Text(stringResource(R.string.open_camera), color = CensoWhite)
                    }
                    Spacer(modifier = Modifier.height(36.dp))
                } else {
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.adding_photo_main_message),
                        textAlign = TextAlign.Center,
                        color = TextBlack,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ButtonRed,
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }

    IconButton(onClick = { navController.navigate(Screen.AccountRoute.route) }) {
        Icon(
            Icons.Rounded.AccountCircle,
            stringResource(id = R.string.content_des_account_icon),
            tint = TextBlack
        )
    }
}