package com.strikeprotocols.mobile.presentation.key_management

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.BioCryptoUtil
import com.strikeprotocols.mobile.common.ImageCaptureError
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.popUpToTop
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.INVALID_PHRASE_ERROR
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.NO_PHRASE_ERROR
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementViewModel.Companion.THUMBNAIL_DATA_KEY
import com.strikeprotocols.mobile.presentation.key_management.flows.*
import com.strikeprotocols.mobile.ui.theme.StrikeWhite
import com.strikeprotocols.mobile.ui.theme.UnfocusedGrey

@Composable
fun KeyManagementScreen(
    navController: NavController,
    viewModel: KeyManagementViewModel = hiltViewModel(),
    initialData: KeyManagementInitialData
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

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

    fun getImageCaptureErrorMessage(context: Context, state: KeyManagementState) =
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


    //region DisposableEffect
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(initialData)
        onDispose { }
    }
    //endregion

    //region LaunchedEffect
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

        val flowStepIsFinished = state.keyManagementFlowStep.isStepFinished()

        if (flowStepIsFinished) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }

            viewModel.resetAddWalletSignersCall()
        }

        if (state.goToAccount is Resource.Success) {
            navController.navigate(Screen.AccountRoute.route)
            viewModel.resetGoToAccount()
        }
    }
    //endregion

    //region PhraseVerificationUI
    when (state.keyManagementFlow) {
        KeyManagementFlow.KEY_RECOVERY -> {
            Box {
                KeyRecoveryFlowUI(
                    pastedPhrase = state.confirmPhraseWordsState.pastedPhrase,
                    keyRecoveryFlowStep =
                    (state.keyManagementFlowStep as KeyManagementFlowStep.RecoveryFlow).step,
                    onNavigate = viewModel::keyRecoveryNavigateForward,
                    onBackNavigate = viewModel::keyRecoveryNavigateBackward,
                    onExit = viewModel::exitPhraseFlow,
                    onPhraseFlowAction = viewModel::phraseFlowAction,
                    onPhraseEntryAction = viewModel::phraseEntryAction,
                    wordToVerifyIndex = state.confirmPhraseWordsState.phraseWordToVerifyIndex,
                    wordInput = state.confirmPhraseWordsState.wordInput,
                    wordVerificationErrorEnabled = state.confirmPhraseWordsState.errorEnabled,
                    keyRecoveryState = state.finalizeKeyFlow,
                    retryKeyRecovery = viewModel::retryKeyRecoveryFromPhrase,
                )
            }
        }
        else -> {
            Box {
                KeyCreationFlowUI(
                    phrase = state.keyGeneratedPhrase ?: "",
                    pastedPhrase = state.confirmPhraseWordsState.pastedPhrase,
                    phraseVerificationFlowStep =
                    (state.keyManagementFlowStep as KeyManagementFlowStep.CreationFlow).step,
                    wordIndexToDisplay = state.wordIndexForDisplay,
                    onPhraseFlowAction = viewModel::phraseFlowAction,
                    onPhraseEntryAction = viewModel::phraseEntryAction,
                    onNavigate = viewModel::keyCreationNavigateForward,
                    onBackNavigate = viewModel::keyCreationNavigateBackward,
                    onExit = viewModel::exitPhraseFlow,
                    wordToVerifyIndex = state.confirmPhraseWordsState.phraseWordToVerifyIndex,
                    wordInput = state.confirmPhraseWordsState.wordInput,
                    wordVerificationErrorEnabled = state.confirmPhraseWordsState.errorEnabled,
                    keyCreationState = state.finalizeKeyFlow,
                    retryKeyCreation = viewModel::retryKeyCreationFromPhrase
                )
            }
        }
    }

    if (state.triggerBioPrompt is Resource.Success) {
        val kickOffBioPrompt = {
            state.triggerBioPrompt.data?.let {
                val promptInfo = BioCryptoUtil.createPromptInfo(context = context)
                val bioPrompt = BioCryptoUtil.createBioPrompt(
                    fragmentActivity = context,
                    onSuccess = {
                        val cipher = it?.cipher
                        if (cipher != null) {
                            viewModel.biometryApproved(cipher)
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
            viewModel.resetPromptTrigger()
        }

        if (state.bioPromptData.immediate) {
            kickOffBioPrompt()
        } else {
            PreBiometryDialog(
                mainText = stringResource(id = R.string.save_biometry_info),
                onAccept = kickOffBioPrompt
            )
        }
    }
    //endregion

    if (state.imageCaptureFailedError is Resource.Error) {
        val message = getImageCaptureErrorMessage(context = context, state = state)

        ImageCaptureErrorDialog(mainText = message) {
            viewModel.resetImageCaptureFailedError()
        }
    }

    if (state.keyRecoveryManualEntryError is Resource.Error) {
        AlertDialog(
            backgroundColor = UnfocusedGrey,
            title = {
                Text(
                    text = stringResource(R.string.key_recovery_failed_title),
                    color = StrikeWhite,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.key_recovery_failed_message),
                    color = StrikeWhite,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resetRecoverManualEntryError() }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            onDismissRequest = {
                viewModel.resetRecoverManualEntryError()
            }
        )
    }

    if (state.showToast is Resource.Success) {

        val message = when (state.showToast.data) {
            NO_PHRASE_ERROR -> {
                stringResource(id = R.string.no_phrase_found)
            }
            INVALID_PHRASE_ERROR -> {
                stringResource(id = R.string.invalid_phrase)
            }
            else -> {
                state.showToast.data ?: ""
            }
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        viewModel.resetShowToast()
    }
    //endregion
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