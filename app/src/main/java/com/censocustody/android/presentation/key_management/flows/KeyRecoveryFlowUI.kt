package com.censocustody.android.presentation.key_management.flows

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.censocustody.android.R
import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.Signers
import com.censocustody.android.presentation.components.AuthTopAppBar
import com.censocustody.android.presentation.key_management.*

fun moveUserToNextRecoveryScreen(flowStep: KeyRecoveryFlowStep) =
    when (flowStep) {
        KeyRecoveryFlowStep.ENTRY_STEP -> KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP
        KeyRecoveryFlowStep.VERIFY_WORDS_STEP -> KeyRecoveryFlowStep.VERIFY_WORDS_STEP
        KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP -> KeyRecoveryFlowStep.ALL_SET_STEP
        KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP -> KeyRecoveryFlowStep.ALL_SET_STEP
        KeyRecoveryFlowStep.ALL_SET_STEP -> KeyRecoveryFlowStep.FINISHED
        KeyRecoveryFlowStep.FINISHED,
        KeyRecoveryFlowStep.UNINITIALIZED -> KeyRecoveryFlowStep.UNINITIALIZED
    }

fun moveUserToPreviousRecoveryScreen(flowStep: KeyRecoveryFlowStep) =
    when (flowStep) {
        KeyRecoveryFlowStep.VERIFY_WORDS_STEP, KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP,
        KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP -> KeyRecoveryFlowStep.ENTRY_STEP
        KeyRecoveryFlowStep.ALL_SET_STEP -> KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP
        KeyRecoveryFlowStep.ENTRY_STEP, KeyRecoveryFlowStep.FINISHED,
        KeyRecoveryFlowStep.UNINITIALIZED -> KeyRecoveryFlowStep.UNINITIALIZED
    }

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun KeyRecoveryFlowUI(
    keyRecoveryFlowStep: KeyRecoveryFlowStep,
    pastedPhrase: String,
    onNavigate: () -> Unit,
    onBackNavigate: () -> Unit,
    onPhraseFlowAction: (phraseFlowAction: PhraseFlowAction) -> Unit,
    onPhraseEntryAction: (PhraseEntryAction) -> Unit,
    onExit: () -> Unit,
    wordToVerifyIndex: Int,
    wordInput: String,
    wordVerificationErrorEnabled: Boolean,
    retryKeyRecovery: () -> Unit,
    keyRecoveryState: Resource<Signers>
) {
    val screenTitle = when (keyRecoveryFlowStep) {
        KeyRecoveryFlowStep.VERIFY_WORDS_STEP,
        KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP,
        KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP -> stringResource(id = R.string.start_over)
        KeyRecoveryFlowStep.ENTRY_STEP,
        KeyRecoveryFlowStep.ALL_SET_STEP,
        KeyRecoveryFlowStep.UNINITIALIZED,
        KeyRecoveryFlowStep.FINISHED -> ""
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (screenTitle.isNotEmpty()) {
                AuthTopAppBar(
                    title = screenTitle,
                    onAppBarIconClick = { onBackNavigate() },
                )
            } else {
               Spacer(modifier = Modifier)
            }
        },
        content = {
            GradientBackgroundUI()
            Box {
                when (keyRecoveryFlowStep) {
                    KeyRecoveryFlowStep.VERIFY_WORDS_STEP -> VerifyPhraseWordUI(
                        wordIndex = wordToVerifyIndex,
                        value = wordInput,
                        onPhraseEntryAction = onPhraseEntryAction,
                        errorEnabled = wordVerificationErrorEnabled,
                        isCreationFlow = false
                    )
                    KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP -> ConfirmKeyUI(
                        pastedPhrase = pastedPhrase,
                        errorEnabled = false,
                        onPhraseEntryAction = onPhraseEntryAction,
                        header = stringResource(R.string.confirm_recovery_phrase_header),
                        title = "",
                        message = stringResource(R.string.enter_key_message)
                    )

                    KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP -> ConfirmKeyUI(
                        pastedPhrase = pastedPhrase,
                        errorEnabled = true,
                        onPhraseEntryAction = onPhraseEntryAction,
                        header = stringResource(R.string.confirm_recovery_phrase_header),
                        title = stringResource(R.string.key_not_valid_title),
                        message = stringResource(R.string.key_not_valid_message)
                    )
                    KeyRecoveryFlowStep.ALL_SET_STEP -> AllSetUI(
                        onNavigate = onNavigate,
                        retry = retryKeyRecovery,
                        allSetState = keyRecoveryState
                    )
                    else -> EntryScreenPhraseUI(
                        title = stringResource(R.string.time_to_restore_key),
                        subtitle = stringResource(R.string.how_did_you_back_it_up),
                        buttonOneText = stringResource(R.string.paste_phrase),
                        buttonTwoText = stringResource(R.string.by_keyboard),
                        onExit = onExit,
                        onPhraseFlowAction = onPhraseFlowAction,
                        creationFlow = false,
                        onNavigate = onNavigate
                    )
                }
            }
        })

}