package com.strikeprotocols.mobile.presentation.key_management.flows

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.key_management.AllSetUI
import com.strikeprotocols.mobile.presentation.key_management.PhraseBackground

fun moveToNextRegenerationScreen(flowStep: KeyRegenerationFlowStep) =
    when (flowStep) {
        KeyRegenerationFlowStep.ALL_SET_STEP -> KeyRegenerationFlowStep.FINISHED
        KeyRegenerationFlowStep.FINISHED -> KeyRegenerationFlowStep.UNINITIALIZED
        KeyRegenerationFlowStep.UNINITIALIZED -> KeyRegenerationFlowStep.ALL_SET_STEP
    }

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun KeyRegenerationFlowUI(
    onNavigate: () -> Unit,
    retryKeyCreation: () -> Unit,
    keyRegenerationState: Resource<WalletSigner?>
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { Spacer(modifier = Modifier) },
        content = {
            PhraseBackground()
            Box {
                AllSetUI(
                    onNavigate = onNavigate,
                    allSetState = keyRegenerationState,
                    retry = retryKeyCreation
                )
            }
        }
    )
}