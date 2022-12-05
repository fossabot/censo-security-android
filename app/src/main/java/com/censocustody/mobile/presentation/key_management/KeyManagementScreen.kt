package com.censocustody.mobile.presentation.key_management

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.censocustody.mobile.R
import com.censocustody.mobile.common.BioCryptoUtil
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.common.popUpToTop
import com.censocustody.mobile.presentation.Screen
import com.censocustody.mobile.presentation.key_management.KeyManagementState.Companion.INVALID_PHRASE_ERROR
import com.censocustody.mobile.presentation.key_management.KeyManagementState.Companion.NO_PHRASE_ERROR
import com.censocustody.mobile.presentation.key_management.flows.*
import com.censocustody.mobile.ui.theme.CensoWhite
import com.censocustody.mobile.ui.theme.UnfocusedGrey

@OptIn(
    ExperimentalComposeUiApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

@Composable
fun KeyManagementScreen(
    navController: NavController,
    viewModel: KeyManagementViewModel = hiltViewModel(),
    initialData: KeyManagementInitialData
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

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

    if (state.keyRecoveryManualEntryError is Resource.Error) {
        AlertDialog(
            backgroundColor = UnfocusedGrey,
            title = {
                Text(
                    text = stringResource(R.string.key_recovery_failed_title),
                    color = CensoWhite,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.key_recovery_failed_message),
                    color = CensoWhite,
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