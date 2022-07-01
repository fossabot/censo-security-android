package com.strikeprotocols.mobile.presentation.sign_in

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
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
fun PhraseRecoveryFlowUI(
    keyRecoveryFlowStep: KeyRecoveryFlowStep,
    pastedPhrase: String,
    onNavigate: () -> Unit,
    onBackNavigate: () -> Unit,
    onPhraseFlowAction : (phraseFlowAction: PhraseFlowAction) -> Unit,
    onExit: () -> Unit,
    verifyPastedPhrase: (String) -> Unit
) {
    val screenTitle = when (keyRecoveryFlowStep) {
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
                    KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP -> ConfirmKeyUI(
                        pastedPhrase = pastedPhrase,
                        errorEnabled = false,
                        verifyPastedPhrase = verifyPastedPhrase,
                        onNavigate = onNavigate,
                        header = stringResource(R.string.recreate_key_phrase),
                        title = stringResource(R.string.phrase_enter_key_title),
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
                    KeyRecoveryFlowStep.ALL_SET_STEP -> AllSetUI(onNavigate = onNavigate)
                    else -> EntryScreenPhraseUI(
                        title = stringResource(R.string.lets_recreate_key),
                        subtitle = stringResource(R.string.how_would_you_enter),
                        buttonOneText = stringResource(R.string.paste_phrase),
                        buttonTwoText = stringResource(R.string.by_keyboard),
                        onExit = onExit,
                        onPhraseFlowAction = onPhraseFlowAction,
                        creationFlow = false
                    )
                }
            }
        })

}