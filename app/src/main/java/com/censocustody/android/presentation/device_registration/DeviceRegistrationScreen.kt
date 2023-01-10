package com.censocustody.android.presentation.device_registration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.censocustody.android.presentation.device_registration.DeviceRegistrationViewModel.Companion.THUMBNAIL_DATA_KEY
import com.censocustody.android.presentation.key_management.GradientBackgroundUI
import com.censocustody.android.presentation.key_management.SmallAuthFlowButton
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.ui.theme.UnfocusedGrey

@Composable
fun DeviceRegistrationScreen(
    navController: NavController,
    viewModel: DeviceRegistrationViewModel = hiltViewModel(),
) {

    //region Variables
    val state = viewModel.state

    val context = LocalContext.current as FragmentActivity
    val packageManager = context.packageManager

    val cameraResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageThumbnailBitmap =
                result.data?.extras?.get(THUMBNAIL_DATA_KEY) as Bitmap?

            if (imageThumbnailBitmap != null) {
                viewModel.capturedUserPhotoSuccess(userPhoto = imageThumbnailBitmap)
            } else {
                viewModel.capturedUserPhotoError(ImageCaptureError.BAD_RESULT)
            }
        } else {
            viewModel.capturedUserPhotoError(ImageCaptureError.ACTION_CANCELLED)
        }
    }

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

    //endregion

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

        if (state.triggerImageCapture is Resource.Success) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (intent.resolveActivity(packageManager) == null) {
                viewModel.capturedUserPhotoError(ImageCaptureError.NO_HARDWARE_CAMERA)
            } else {
                cameraResultLauncher.launch(intent)
            }
            viewModel.resetTriggerImageCapture()
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
                    CryptoObject(state.triggerBioPrompt.data)
                )
            }
        }
    }

    //endregion

    //region Main UI
    GradientBackgroundUI()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        if (state.deviceRegistrationError != DeviceRegistrationError.NONE) {
            val message = getErrorMessage(
                context = context,
                deviceRegistrationError = state.deviceRegistrationError,
                imageCaptureError = state.imageCaptureFailedError.data
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
        } else {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(width = 1.5.dp, color = UnfocusedGrey.copy(alpha = 0.50f))
                        .background(color = Color.Black)
                        .zIndex(2.5f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

    if (state.userApproveSaveDeviceKey is Resource.Success) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .padding(all = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CensoButton(
                onClick = {
                    viewModel.createKeyForDevice()
                    viewModel.resetUserDialogToSaveDeviceKey()
                },
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.i_have_taken_photo),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            CensoButton(
                onClick = {
                    viewModel.retry()
                    viewModel.resetUserDialogToSaveDeviceKey()
                },
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 24.dp)
            ) {
                Text(text = stringResource(R.string.retake_photo), textAlign = TextAlign.Center)
            }
        }
    }

    //endregion
}