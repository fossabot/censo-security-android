package com.censocustody.android.presentation.regeneration

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.BioCryptoUtil
import com.censocustody.android.common.Resource
import com.censocustody.android.common.popUpToTop
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.key_management.PreBiometryDialog
import com.censocustody.android.presentation.migration.MigrationUI

@Composable
fun RegenerationScreen(
    navController: NavController,
    viewModel: RegenerationViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { }
    }

    LaunchedEffect(key1 = state) {
        if (state.finishedRegeneration) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }
            viewModel.resetAddWalletSignerCall()
        }
    }

    MigrationUI(
        errorEnabled = state.regenerationError is Resource.Success,
        retry = viewModel::retryRegeneration
    )

    if (state.triggerBioPrompt is Resource.Success) {
        val kickOffBioPrompt = {
            state.triggerBioPrompt.data?.let {
                val promptInfo = BioCryptoUtil.createPromptInfo(context = context)
                val bioPrompt = BioCryptoUtil.createBioPrompt(
                    fragmentActivity = context,
                    onSuccess = {
                        if (it?.cipher != null || it?.signature != null) {
                            viewModel.biometryApproved(it)
                        } else {
                            BioCryptoUtil.handleBioPromptOnFail(
                                context = context,
                                errorCode = BioCryptoUtil.NO_CIPHER_CODE
                            ) {
                                viewModel.biometryFailed(BioCryptoUtil.NO_CIPHER_CODE)
                            }
                        }
                    },
                    onFail = { failedReason ->
                        BioCryptoUtil.handleBioPromptOnFail(
                            context = context,
                            errorCode = failedReason
                        ) {
                            viewModel.biometryFailed(failedReason)
                        }
                    }
                )

                bioPrompt.authenticate(
                    promptInfo,
                    state.triggerBioPrompt.data
                )
            }
            viewModel.resetPromptTrigger()
        }

        PreBiometryDialog(
            mainText = stringResource(id = R.string.initial_migration_message),
            onAccept = kickOffBioPrompt
        )
    }

    if (state.showToast is Resource.Success) {
        Toast.makeText(context, R.string.need_complete_migration, Toast.LENGTH_LONG).show()
        viewModel.resetShowToast()
    }
}