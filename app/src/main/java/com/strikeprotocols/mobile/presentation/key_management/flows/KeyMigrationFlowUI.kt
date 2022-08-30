package com.strikeprotocols.mobile.presentation.key_management.flows

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.key_management.AllSetUI
import com.strikeprotocols.mobile.presentation.key_management.PhraseBackground

fun moveToNextMigrationScreen(flowStep: KeyMigrationFlowStep) =
    when (flowStep) {
        KeyMigrationFlowStep.ALL_SET_STEP -> KeyMigrationFlowStep.FINISHED
        KeyMigrationFlowStep.FINISHED -> KeyMigrationFlowStep.UNINITIALIZED
        KeyMigrationFlowStep.UNINITIALIZED -> KeyMigrationFlowStep.ALL_SET_STEP
    }

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun KeyMigrationFlowUI(
    onNavigate: () -> Unit,
    retryKeyMigration: () -> Unit,
    keyMigrationState: Resource<WalletSigner?>
) {

    val loadingText = stringResource(id = R.string.migrating_key_loading)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { Spacer(modifier = Modifier) },
        content = {
            PhraseBackground()
            Box {
                AllSetUI(
                    onNavigate = onNavigate,
                    allSetState = keyMigrationState,
                    retry = retryKeyMigration,
                    loadingText = loadingText
                )
            }
        }
    )
}