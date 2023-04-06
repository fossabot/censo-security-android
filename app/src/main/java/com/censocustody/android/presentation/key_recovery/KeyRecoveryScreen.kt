package com.censocustody.android.presentation.key_recovery

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.censocustody.android.R
import com.censocustody.android.common.BioCryptoUtil
import com.censocustody.android.common.HELP_URL
import com.censocustody.android.common.Resource
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.key_management.BackgroundUI
import com.censocustody.android.presentation.key_management.PreBiometryDialog
import com.censocustody.android.ui.theme.ButtonRed
import com.censocustody.android.ui.theme.CensoTextBlue

@Composable
fun KeyRecoveryScreen(
    navController: NavController,
    initialData: KeyRecoveryInitialData,
    viewModel: KeyRecoveryViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity
    val localHandler = LocalUriHandler.current

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(initialData)
        onDispose {
            viewModel.cleanUp()
        }
    }

    LaunchedEffect(key1 = state) {

        if (state.recoverKeyProcess is Resource.Success) {
            viewModel.resetKeyProcess()

            navController.navigate(Screen.EntranceRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
        }
    }

    BackgroundUI()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(60.dp)
                .align(alignment = Alignment.Center),
            color = ButtonRed,
            strokeWidth = 5.dp,
        )
    }

    if (state.triggerBioPrompt is Resource.Success) {
        val kickOffBioPrompt = {
            val promptInfo = BioCryptoUtil.createPromptInfo(context = context)
            val bioPrompt = BioCryptoUtil.createBioPrompt(fragmentActivity = context,
                onSuccess = { viewModel.biometryApproved() },
                onFail = {
                    BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                        viewModel.biometryFailed()
                    }
                })

            bioPrompt.authenticate(promptInfo)

            viewModel.resetPromptTrigger()
        }

        PreBiometryDialog(
            mainText = stringResource(id = R.string.save_biometry_info_key_creation),
            onAccept = kickOffBioPrompt
        )
    }

    if (state.recoverKeyProcess is Resource.Error) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(stringResource(R.string.recovery_failure))
                Spacer(modifier = Modifier.height(28.dp))
                Text(modifier = Modifier
                    .clickable {
                        localHandler.openUri(HELP_URL)
                    }
                    .padding(24.dp),
                    text = stringResource(R.string.get_help),
                    color = CensoTextBlue,
                    fontWeight = FontWeight.W500,
                    fontSize = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = viewModel::retry) {
                    Text(stringResource(R.string.recovery_failure))
                }
            }
        }
    }
}