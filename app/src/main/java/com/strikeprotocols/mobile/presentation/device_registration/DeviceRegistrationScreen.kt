package com.strikeprotocols.mobile.presentation.device_registration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.BioCryptoUtil
import com.strikeprotocols.mobile.common.ImageCaptureError
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.presentation.device_registration.DeviceRegistrationViewModel.Companion.THUMBNAIL_DATA_KEY
import com.strikeprotocols.mobile.ui.theme.StrikeWhite
import com.strikeprotocols.mobile.ui.theme.UnfocusedGrey

@Composable
fun DeviceRegistrationScreen(
    navController: NavController,
    viewModel: DeviceRegistrationViewModel = hiltViewModel(),
) {

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
                viewModel.handleCapturedUserPhoto(userPhoto = imageThumbnailBitmap)
            } else {
                viewModel.handleImageCaptureError(ImageCaptureError.BAD_RESULT)
            }
        } else {
            viewModel.handleImageCaptureError(ImageCaptureError.ACTION_CANCELLED)
        }
    }

    fun getImageCaptureErrorMessage(context: Context, state: DeviceRegistrationState) =
        when (state.imageCaptureFailedError.data) {
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

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { }
    }

    LaunchedEffect(key1 = state) {
        if (state.triggerImageCapture is Resource.Success) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (intent.resolveActivity(packageManager) == null) {
                viewModel.handleImageCaptureError(ImageCaptureError.NO_HARDWARE_CAMERA)
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
                    state.triggerBioPrompt.data
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Text("Key Device Registration", color = StrikeWhite)
    }

    if (state.imageCaptureFailedError is Resource.Error) {
        val message = getImageCaptureErrorMessage(context = context, state = state)

        ImageCaptureErrorDialog(mainText = message) {
            viewModel.resetImageCaptureFailedError()
        }
    }
}

@Composable
fun ImageCaptureErrorDialog(
    mainText: String,
    onAccept: () -> Unit,
) {
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
                modifier = Modifier.padding(horizontal = 16.dp),
                text = mainText,
                textAlign = TextAlign.Center,
                color = StrikeWhite,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(36.dp))
            Button(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp)),
                onClick = onAccept,
            ) {
                Text(
                    text = stringResource(id = R.string.ok),
                    fontSize = 18.sp,
                    color = StrikeWhite,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}