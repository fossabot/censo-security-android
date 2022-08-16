package com.strikeprotocols.mobile.presentation.key_management.flows

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.components.StrikeAuthTopAppBar
import com.strikeprotocols.mobile.presentation.key_management.*

fun moveUserToNextCreationScreen(flowStep: KeyCreationFlowStep) =
    when (flowStep) {
        KeyCreationFlowStep.ENTRY_STEP -> KeyCreationFlowStep.COPY_PHRASE_STEP
        KeyCreationFlowStep.PHRASE_COPIED_STEP, KeyCreationFlowStep.COPY_PHRASE_STEP ->
            KeyCreationFlowStep.PHRASE_COPIED_STEP
        KeyCreationFlowStep.PHRASE_SAVED_STEP -> KeyCreationFlowStep.CONFIRM_KEY_ENTRY_STEP
        KeyCreationFlowStep.CONFIRM_KEY_ENTRY_STEP, KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP ->
            KeyCreationFlowStep.ALL_SET_STEP
        KeyCreationFlowStep.WRITE_WORD_STEP -> KeyCreationFlowStep.WRITE_WORD_STEP
        KeyCreationFlowStep.VERIFY_WORDS_STEP -> KeyCreationFlowStep.VERIFY_WORDS_STEP
        KeyCreationFlowStep.ALL_SET_STEP -> KeyCreationFlowStep.FINISHED
        KeyCreationFlowStep.FINISHED -> KeyCreationFlowStep.ENTRY_STEP
        KeyCreationFlowStep.UNINITIALIZED -> KeyCreationFlowStep.ENTRY_STEP
    }

fun moveUserToPreviousCreationScreen(
    flowStep: KeyCreationFlowStep,
    addWalletSignerResult: Resource<WalletSigner?>) =
    when (flowStep) {
        KeyCreationFlowStep.ENTRY_STEP -> KeyCreationFlowStep.UNINITIALIZED
        KeyCreationFlowStep.COPY_PHRASE_STEP,
        KeyCreationFlowStep.PHRASE_SAVED_STEP,
        KeyCreationFlowStep.PHRASE_COPIED_STEP -> KeyCreationFlowStep.ENTRY_STEP
        KeyCreationFlowStep.CONFIRM_KEY_ENTRY_STEP,
        KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP -> KeyCreationFlowStep.COPY_PHRASE_STEP
        KeyCreationFlowStep.ALL_SET_STEP ->
            if (addWalletSignerResult is Resource.Success) {
                KeyCreationFlowStep.FINISHED
            } else {
                KeyCreationFlowStep.ENTRY_STEP
            }
        KeyCreationFlowStep.WRITE_WORD_STEP -> KeyCreationFlowStep.ENTRY_STEP
        KeyCreationFlowStep.VERIFY_WORDS_STEP -> KeyCreationFlowStep.WRITE_WORD_STEP
        KeyCreationFlowStep.FINISHED -> KeyCreationFlowStep.UNINITIALIZED
        KeyCreationFlowStep.UNINITIALIZED -> KeyCreationFlowStep.UNINITIALIZED
    }


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun KeyCreationFlowUI(
    phrase: String,
    phraseVerificationFlowStep: KeyCreationFlowStep,
    pastedPhrase: String,
    wordIndex: Int,
    onNavigate: () -> Unit,
    onPhraseFlowAction: (phraseFlowAction: PhraseFlowAction) -> Unit,
    onBackNavigate: () -> Unit,
    onExit: () -> Unit,
    verifyPastedPhrase: (String) -> Unit,
    wordToVerifyIndex: Int,
    wordInput: String,
    wordInputChange: (String) -> Unit,
    wordVerificationErrorEnabled: Boolean,
    retryKeyCreation: () -> Unit,
    onSubmitWord: (String) -> Unit,
    keyCreationState: Resource<WalletSigner?>
) {

    val screenTitle = when (phraseVerificationFlowStep) {
        KeyCreationFlowStep.ENTRY_STEP -> ""
        KeyCreationFlowStep.COPY_PHRASE_STEP,
        KeyCreationFlowStep.WRITE_WORD_STEP,
        KeyCreationFlowStep.PHRASE_COPIED_STEP,
        KeyCreationFlowStep.PHRASE_SAVED_STEP -> stringResource(R.string.start_over)
        KeyCreationFlowStep.VERIFY_WORDS_STEP -> stringResource(R.string.verify_phrase)
        KeyCreationFlowStep.CONFIRM_KEY_ENTRY_STEP,
        KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP -> stringResource(R.string.copy_key)
        KeyCreationFlowStep.ALL_SET_STEP -> ""
        KeyCreationFlowStep.UNINITIALIZED -> ""
        KeyCreationFlowStep.FINISHED -> ""
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (screenTitle.isNotEmpty()) {
                StrikeAuthTopAppBar(
                    title = screenTitle,
                    onAppBarIconClick = { onBackNavigate() },
                )
            } else {
                Spacer(modifier = Modifier)
            }
        },
        content = {
            PhraseBackground()
            Box {
                when (phraseVerificationFlowStep) {
                    KeyCreationFlowStep.COPY_PHRASE_STEP ->
                        CopyKeyUI(
                            onNavigate = onNavigate,
                            phrase = phrase,
                            phraseCopied = false,
                            phraseSaved = false
                        )
                    KeyCreationFlowStep.PHRASE_COPIED_STEP ->
                        CopyKeyUI(
                            onNavigate = onNavigate,
                            phrase = phrase,
                            phraseCopied = true,
                            phraseSaved = false
                        )
                    KeyCreationFlowStep.PHRASE_SAVED_STEP ->
                        CopyKeyUI(
                            onNavigate = onNavigate,
                            phrase = phrase,
                            phraseCopied = true,
                            phraseSaved = true
                        )
                    KeyCreationFlowStep.WRITE_WORD_STEP -> {
                        WriteWordUI(
                            phrase = phrase,
                            index = wordIndex,
                            onPhraseFlowAction = onPhraseFlowAction,
                            onNavigate = onNavigate
                        )
                    }
                    KeyCreationFlowStep.VERIFY_WORDS_STEP -> {
                        VerifyPhraseWordUI(
                            wordIndex = wordToVerifyIndex,
                            value = wordInput,
                            onValueChanged = wordInputChange,
                            errorEnabled = wordVerificationErrorEnabled,
                            onSubmitWord = onSubmitWord,
                            onPreviousWordNavigate = {},
                            onNextWordNavigate = {},
                            isCreationFlow = true
                        )
                    }
                    KeyCreationFlowStep.CONFIRM_KEY_ENTRY_STEP -> ConfirmKeyUI(
                        pastedPhrase = pastedPhrase,
                        errorEnabled = false,
                        verifyPastedPhrase = verifyPastedPhrase,
                        header = stringResource(R.string.confirm_recovery_phrase),
                        title = stringResource(R.string.enter_key_title),
                        message = stringResource(R.string.enter_key_message)
                    )
                    KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP -> ConfirmKeyUI(
                        pastedPhrase = pastedPhrase,
                        errorEnabled = true,
                        verifyPastedPhrase = verifyPastedPhrase,
                        header = stringResource(R.string.confirm_recovery_phrase),
                        title = stringResource(R.string.key_not_valid_title),
                        message = stringResource(R.string.key_not_valid_message)
                    )
                    KeyCreationFlowStep.ALL_SET_STEP ->
                        AllSetUI(
                            onNavigate = onNavigate,
                            allSetState = keyCreationState,
                            retry = retryKeyCreation
                        )
                    else -> EntryScreenPhraseUI(
                        title = stringResource(R.string.phrase_generated),
                        subtitle = stringResource(R.string.how_would_you_save_it),
                        buttonOneText = stringResource(R.string.password_manager),
                        buttonTwoText = stringResource(R.string.pen_and_paper),
                        onExit = onExit,
                        onPhraseFlowAction = onPhraseFlowAction,
                        creationFlow = true,
                        onNavigate = {}
                    )
                }
            }
        })
}