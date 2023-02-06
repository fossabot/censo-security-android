package com.censocustody.android.presentation.device_registration

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.*
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.key_management.GradientBackgroundUI
import com.censocustody.android.presentation.key_management.SmallAuthFlowButton
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.ui.theme.UnfocusedGrey
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Composable
fun DeviceRegistrationScreen(
    navController: NavController,
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
        viewModel.onStart()
        onDispose { }
    }

    LaunchedEffect(key1 = state) {

        if (state.addUserDevice is Resource.Success) {
            navController.navigate(Screen.EntranceRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }
            viewModel.resetUserDevice()
        }

        if (!state.userLoggedIn) {
            navController.navigate(Screen.EntranceRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }
            viewModel.resetUserLoggedIn()
        }

        if (state.triggerBioPrompt is Resource.Success) {
            viewModel.resetPromptTrigger()

            state.triggerBioPrompt.data?.let {
                val promptInfo = BioCryptoUtil.createPromptInfo(context = context)

                val bioPrompt = BioCryptoUtil.createBioPrompt(
                    fragmentActivity = context,
                    onSuccess = {
                        if (it != null) {
                            viewModel.biometryApproved(it)
                        } else {
                            BioCryptoUtil.handleBioPromptOnFail(
                                context = context,
                                errorCode = BioCryptoUtil.NO_CIPHER_CODE
                            ) {
                                viewModel.biometryFailed()
                            }
                        }
                    },
                    onFail = {
                        BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                            viewModel.biometryFailed()
                        }
                    }
                )

                bioPrompt.authenticate(
                    promptInfo,
                    BiometricPrompt.CryptoObject(state.triggerBioPrompt.data)
                )
            }
        }

        if (state.triggerImageCapture is Resource.Success) {
            val cameraProvider = context.getCameraProvider()
            try {
                cameraProvider.unbindAll()
            } catch (ex: Exception) {

            }
            viewModel.resetTriggerImageCapture()
            viewModel.createKeyForDevice()
        }
    }

    //endregion

    GradientBackgroundUI()
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
                    color = CensoWhite,
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
                onImageCaptureSuccess = { bitmap ->
                    viewModel.capturedUserPhotoSuccess(bitmap)
                },
                onImageCaptureError = {
                    viewModel.capturedUserPhotoError(it)
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp)
                    .border(width = 1.5.dp, color = UnfocusedGrey.copy(alpha = 0.50f))
                    .background(color = Color.Black)
                    .zIndex(2.5f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.capturingDeviceKey is Resource.Uninitialized) {
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(R.string.take_photo_description),
                        textAlign = TextAlign.Center,
                        color = CensoWhite,
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
                        color = CensoWhite,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CensoWhite,
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}