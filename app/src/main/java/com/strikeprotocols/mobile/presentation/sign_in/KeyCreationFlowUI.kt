package com.strikeprotocols.mobile.presentation.sign_in

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.presentation.components.StrikeTopAppBar

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PhraseVerificationFlowUI(
    phrase: String,
    phraseVerificationFlowStep: KeyCreationFlowStep,
    pastedPhrase: String,
    wordIndex: Int,
    onNavigate: () -> Unit,
    onPhraseFlowAction: (phraseFlowAction: PhraseFlowAction) -> Unit,
    onBackNavigate: () -> Unit,
    onExit: () -> Unit,
    verifyPastedPhrase: (String) -> Unit
) {

    val screenTitle = when (phraseVerificationFlowStep) {
        KeyCreationFlowStep.ENTRY_STEP -> ""
        KeyCreationFlowStep.COPY_PHRASE_STEP,
        KeyCreationFlowStep.WRITE_WORD_STEP,
        KeyCreationFlowStep.PHRASE_COPIED_STEP,
        KeyCreationFlowStep.PHRASE_SAVED_STEP -> stringResource(R.string.start_over)
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
                StrikeTopAppBar(
                    title = screenTitle,
                    navigationIcon = Icons.Rounded.ArrowBack,
                    onAppBarIconClick = { onBackNavigate() },
                    navigationIconContentDes = screenTitle
                )
            } else {
                StrikeTopAppBar(
                    title = "",
                    onAppBarIconClick = { },
                    navigationIcon = Icons.Rounded.ArrowBack,
                    showNavIcon = false,
                    navigationIconContentDes = ""
                )
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
                            onPhraseFlowAction = onPhraseFlowAction
                        )
                    }
                    KeyCreationFlowStep.CONFIRM_KEY_ENTRY_STEP -> ConfirmKeyUI(
                        pastedPhrase = pastedPhrase,
                        errorEnabled = false,
                        verifyPastedPhrase = verifyPastedPhrase,
                        onNavigate = onNavigate,
                        header = stringResource(R.string.confirm_private_key),
                        title = stringResource(R.string.enter_key_title),
                        message = stringResource(R.string.enter_key_message)
                    )
                    KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP -> ConfirmKeyUI(
                        pastedPhrase = pastedPhrase,
                        errorEnabled = true,
                        verifyPastedPhrase = verifyPastedPhrase,
                        onNavigate = onNavigate,
                        header = stringResource(R.string.confirm_private_key),
                        title = stringResource(R.string.key_not_valid_title),
                        message = stringResource(R.string.key_not_valid_message)
                    )
                    KeyCreationFlowStep.ALL_SET_STEP -> AllSetUI(onNavigate = onNavigate)
                    else -> EntryScreenPhraseUI(
                        title = stringResource(R.string.time_to_back_up),
                        subtitle = stringResource(R.string.how_would_you_save_it),
                        buttonOneText = stringResource(R.string.password_manager),
                        buttonTwoText = stringResource(R.string.pen_and_paper),
                        onExit = onExit,
                        onPhraseFlowAction = onPhraseFlowAction,
                        creationFlow = true
                    )
                }
            }
        })
}