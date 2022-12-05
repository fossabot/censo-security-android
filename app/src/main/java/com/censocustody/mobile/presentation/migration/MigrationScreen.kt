package com.censocustody.mobile.presentation.migration


import android.widget.Toast
import com.censocustody.mobile.presentation.key_management.PreBiometryDialog
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.mobile.R
import com.censocustody.mobile.common.BioCryptoUtil
import com.censocustody.mobile.common.BioCryptoUtil.NO_CIPHER_CODE
import com.censocustody.mobile.common.BioPromptReason
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.common.popUpToTop
import com.censocustody.mobile.presentation.Screen

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

@Composable
fun MigrationScreen(
    navController: NavController,
    viewModel: MigrationViewModel = hiltViewModel(),
    initialData: VerifyUserInitialData
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    //region DisposableEffect
    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(initialData)
        onDispose {
            viewModel.cleanUp()
        }
    }
    //endregion

    //region LaunchedEffect
    LaunchedEffect(key1 = state) {
        if (state.finishedMigration) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }

            viewModel.resetAddWalletSignerCall()
        }

        if (state.kickUserOut) {
            navController.navigate(Screen.EntranceRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }

            viewModel.resetKickOut()
        }
    }
    //endregion

    //region MAIN UI
    MigrationUI(
        errorEnabled = state.addWalletSigner is Resource.Error,
        retry = viewModel::retry
    )

    if (state.triggerBioPrompt is Resource.Success) {
        val kickOffBioPrompt = {
            state.triggerBioPrompt.data?.let {
                val promptInfo = BioCryptoUtil.createPromptInfo(context = context)
                val bioPrompt = BioCryptoUtil.createBioPrompt(
                    fragmentActivity = context,
                    onSuccess = {
                        val cipher = it?.cipher
                        if (cipher != null) {
                            viewModel.biometryApproved(cipher)
                        } else {
                            BioCryptoUtil.handleBioPromptOnFail(
                                context = context,
                                errorCode = NO_CIPHER_CODE
                            ) {
                                viewModel.biometryFailed(NO_CIPHER_CODE)
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
                    BiometricPrompt.CryptoObject(state.triggerBioPrompt.data)
                )
            }
            viewModel.resetPromptTrigger()
        }

        if (state.bioPromptData.immediate) {
            kickOffBioPrompt()
        } else {
            PreBiometryDialog(
                mainText = stringResource(id = R.string.initial_migration_message),
                onAccept = kickOffBioPrompt
            )
        }
    }

    if (state.showToast is Resource.Success) {
        Toast.makeText(context, R.string.need_complete_migration, Toast.LENGTH_LONG).show()
        viewModel.resetShowToast()
    }
    //endregion
}