package com.strikeprotocols.mobile.presentation.key_management

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.NO_PHRASE_ERROR
import com.strikeprotocols.mobile.presentation.key_management.flows.*
import com.strikeprotocols.mobile.ui.theme.StrikeWhite
import com.strikeprotocols.mobile.ui.theme.UnfocusedGrey

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
    val context = LocalContext.current

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
                navController.backQueue.clear()
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
                    pastedPhrase = state.pastedPhrase,
                    keyRecoveryFlowStep =
                        (state.keyManagementFlowStep as KeyManagementFlowStep.RecoveryFlow).step,
                    onNavigate = viewModel::recoverKeyNavigationForward,
                    onBackNavigate = viewModel::recoverKeyNavigationBackward,
                    verifyPastedPhrase = viewModel::verifyPhraseToRecoverKeyPair,
                    onExit = viewModel::exitPhraseFlow,
                    onPhraseFlowAction = viewModel::phraseFlowAction,
                    wordToVerifyIndex = state.confirmPhraseWordsState.wordIndex,
                    wordInput = state.confirmPhraseWordsState.wordInput,
                    wordInputChange = viewModel::updateWordInput,
                    wordVerificationErrorEnabled = state.confirmPhraseWordsState.errorEnabled,
                    navigatePreviousWord = viewModel::navigatePreviousWord,
                    navigateNextWord = viewModel::navigateNextWord,
                    onSubmitWord = viewModel::submitWordInput,
                    keyRecoveryState = state.addWalletSignerResult,
                    retryKeyRecovery = {},
                )
            }
        }

        KeyManagementFlow.KEY_REGENERATION -> {
            Box {
                KeyRegenerationFlowUI(
                    onNavigate = viewModel::regenerateKeyNavigationForward,
                    retryKeyCreation = viewModel::retryRegenerateData,
                    keyRegenerationState = state.addWalletSignerResult
                )
            }
        }
        else -> {
            Box {
                KeyCreationFlowUI(
                    phrase = state.phrase ?: "",
                    pastedPhrase = state.pastedPhrase,
                    phraseVerificationFlowStep =
                        (state.keyManagementFlowStep as KeyManagementFlowStep.CreationFlow).step,
                    wordIndex = state.wordIndex,
                    onPhraseFlowAction = viewModel::phraseFlowAction,
                    onNavigate = viewModel::createKeyNavigationForward,
                    onBackNavigate = viewModel::createKeyNavigationBackward,
                    verifyPastedPhrase = viewModel::verifyPastedPhrase,
                    onExit = viewModel::exitPhraseFlow,
                    wordToVerifyIndex = state.confirmPhraseWordsState.wordIndex,
                    wordInput = state.confirmPhraseWordsState.wordInput,
                    wordInputChange = viewModel::updateWordInput,
                    wordVerificationErrorEnabled = state.confirmPhraseWordsState.errorEnabled,
                    onSubmitWord = viewModel::submitWordInput,
                    keyCreationState = state.addWalletSignerResult,
                    retryKeyCreation = viewModel::retryKeyCreationFromPhrase
                )
            }
        }
    }
    //endregion

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

        val message = if(state.showToast.data == NO_PHRASE_ERROR) {
            stringResource(id = R.string.no_phrase_found)
        } else {
            state.showToast.data ?: ""
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        viewModel.resetShowToast()
    }
    //endregion
}