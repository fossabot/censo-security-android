package com.censocustody.android.presentation.keys_upload

import android.widget.Toast
import com.censocustody.android.presentation.key_management.PreBiometryDialog
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.common.BioCryptoUtil
import com.censocustody.android.common.BioCryptoUtil.NO_CIPHER_CODE
import com.censocustody.android.common.Resource
import com.censocustody.android.common.popUpToTop
import com.censocustody.android.presentation.Screen
import com.censocustody.android.R
import com.censocustody.android.common.BioPromptReason
import com.censocustody.android.data.BioPromptData

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

@Composable
fun KeysUploadScreen(
    navController: NavController,
    viewModel: KeysUploadViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    //region DisposableEffect
    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { }
    }
    //endregion

    //region LaunchedEffect
    LaunchedEffect(key1 = state) {
        if (state.finishedUpload) {
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
    KeysUploadUI(
        errorEnabled = state.addWalletSigner is Resource.Error,
        retry = viewModel::retry
    )

    if (state.triggerBioPrompt is Resource.Success) {

        val kickOffBioPrompt = {
            val promptInfo = BioCryptoUtil.createPromptInfo(context = context)
            val bioPrompt = BioCryptoUtil.createBioPrompt(
                fragmentActivity = context,
                onSuccess = { viewModel.biometryApproved() },
                onFail = { failedReason ->
                    BioCryptoUtil.handleBioPromptOnFail(
                        context = context,
                        errorCode = failedReason
                    ) {
                        viewModel.biometryFailed(failedReason)
                    }
                }
            )

            bioPrompt.authenticate(promptInfo)

            viewModel.resetPromptTrigger()
        }

        if (state.bioPromptData.bioPromptReason == BioPromptReason.RETRIEVE_V3_ROOT_SEED) {
            PreBiometryDialog(
                mainText = stringResource(id = R.string.initial_key_upload_message),
                onAccept = kickOffBioPrompt
            )
        } else {
            kickOffBioPrompt()
        }
    }

    if (state.showToast is Resource.Success) {
        Toast.makeText(context, R.string.need_complete_key_upload, Toast.LENGTH_LONG).show()
        viewModel.resetShowToast()
    }
    //endregion
}