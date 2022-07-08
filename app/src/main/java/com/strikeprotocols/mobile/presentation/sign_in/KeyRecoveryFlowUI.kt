package com.strikeprotocols.mobile.presentation.sign_in

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.presentation.components.StrikeTopAppBar

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun KeyRecoveryFlowUI(
    keyRecoveryFlowStep: KeyRecoveryFlowStep,
    pastedPhrase: String,
    onNavigate: () -> Unit,
    onBackNavigate: () -> Unit,
    onPhraseFlowAction: (phraseFlowAction: PhraseFlowAction) -> Unit,
    onExit: () -> Unit,
    verifyPastedPhrase: (String) -> Unit,
    wordToVerifyIndex: Int,
    wordInput: String,
    wordInputChange: (String) -> Unit,
    wordVerificationErrorEnabled: Boolean,
    retryKeyRecovery: () -> Unit,
    onSubmitWord: (Context) -> Unit,
    keyRecoveryState: Resource<Boolean>
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
                when (keyRecoveryFlowStep) {
                    KeyRecoveryFlowStep.VERIFY_WORDS_STEP -> VerifyPhraseWordUI(
                        wordIndex = wordToVerifyIndex,
                        value = wordInput,
                        onValueChanged = wordInputChange,
                        onSubmitWord = onSubmitWord,
                        errorEnabled = wordVerificationErrorEnabled
                    )
                    KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP -> ConfirmKeyUI(
                        pastedPhrase = pastedPhrase,
                        errorEnabled = false,
                        verifyPastedPhrase = verifyPastedPhrase,
                        onNavigate = onNavigate,
                        header = stringResource(R.string.recreate_key_phrase),
                        title = "",
                        message = stringResource(R.string.phrase_enter_key_message)
                    )

                    KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP -> ConfirmKeyUI(
                        pastedPhrase = pastedPhrase,
                        errorEnabled = true,
                        verifyPastedPhrase = verifyPastedPhrase,
                        onNavigate = onNavigate,
                        header = stringResource(R.string.recreate_key_phrase),
                        title = stringResource(R.string.phrase_key_not_valid_title),
                        message = stringResource(R.string.phrase_key_not_valid_message)
                    )
                    KeyRecoveryFlowStep.ALL_SET_STEP -> AllSetUI(
                        onNavigate = onNavigate,
                        retry = retryKeyRecovery,
                        allSetState = keyRecoveryState
                    )
                    else -> EntryScreenPhraseUI(
                        title = stringResource(R.string.lets_recreate_key),
                        subtitle = stringResource(R.string.how_would_you_enter),
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