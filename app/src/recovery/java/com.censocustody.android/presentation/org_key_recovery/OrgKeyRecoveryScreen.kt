package com.censocustody.android.presentation.org_key_recovery

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.common.Resource
import com.censocustody.android.presentation.key_management.flows.KeyManagementFlowStep
import com.censocustody.android.presentation.key_management.flows.KeyRecoveryFlowUI

@Composable
fun OrgKeyRecoveryScreen(
    navController: NavController,
    viewModel: OrgKeyRecoveryViewModel = hiltViewModel()
) {

    val state = viewModel.state

    //Disposable effect to prepare viewModel on screen startup
    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {  }
    }

    //Launched effect to move on when the recovery is finished
    LaunchedEffect(key1 = state) {

        if (state.onExit is Resource.Success) {
            navController.navigateUp()
            viewModel.resetOnExit()
        }
    }

    //region PhraseVerificationUI (Recovery only)

    Box {
      KeyRecoveryFlowUI(
          keyRecoveryFlowStep = (state.keyRecoveryFlowStep as KeyManagementFlowStep.RecoveryFlow).step,
          pastedPhrase = state.confirmPhraseWordsState.pastedPhrase,
          onNavigate = viewModel::keyRecoveryNavigateForward,
          onBackNavigate = viewModel::keyRecoveryNavigateBackward,
          onPhraseFlowAction = viewModel::phraseFlowAction,
          onPhraseEntryAction = viewModel::phraseEntryAction,
          onExit = viewModel::exitPhraseFlow,
          wordToVerifyIndex = state.confirmPhraseWordsState.phraseWordToVerifyIndex,
          wordInput = state.confirmPhraseWordsState.wordInput,
          wordVerificationErrorEnabled = state.confirmPhraseWordsState.errorEnabled,
          retryKeyRecovery = viewModel::retryKeyRecoveryFromPhrase,
          keyRecoveryState = state.finalizeKeyFlow
      )
    }
}