package com.censocustody.android.presentation.key_creation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.common.Resource
import com.censocustody.android.common.popUpToTop
import com.censocustody.android.presentation.Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.censocustody.android.presentation.key_management.BackgroundUI
import com.censocustody.android.presentation.key_management.SmallAuthFlowButton
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.ui.theme.UnfocusedGrey
import com.censocustody.android.R
import com.censocustody.android.common.BioCryptoUtil
import com.censocustody.android.presentation.key_management.PreBiometryDialog
import com.censocustody.android.ui.theme.ButtonRed
import com.censocustody.android.ui.theme.TextBlack

@Composable
fun KeyCreationScreen(
    navController: NavController,
    viewModel: KeyCreationViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {
            viewModel.cleanUp()
        }
    }

    LaunchedEffect(key1 = state) {
        if (state.uploadingKeyProcess is Resource.Success) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }
            viewModel.resetAddWalletSignerCall()
        }
    }

    BackgroundUI()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        if (state.uploadingKeyProcess is Resource.Error) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.something_went_wrong),
                    color = TextBlack,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.23.sp,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                SmallAuthFlowButton(
                    modifier = Modifier.wrapContentWidth(),
                    text = stringResource(R.string.retry),
                ) {
                    viewModel.retryKeyCreation()
                }
            }
        } else {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(width = 1.5.dp, color = UnfocusedGrey.copy(alpha = 0.50f))
                        .background(color = Color.Black)
                        .shadow(elevation = 2.5.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.saving_key_to_device),
                        textAlign = TextAlign.Center,
                        color = TextBlack,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ButtonRed,
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }

    if (state.triggerBioPrompt is Resource.Success) {
        val kickOffBioPrompt = {
            state.triggerBioPrompt.data?.let {
                val promptInfo = BioCryptoUtil.createPromptInfo(context = context)
                val bioPrompt = BioCryptoUtil.createBioPrompt(
                    fragmentActivity = context,
                    onSuccess = { viewModel.biometryApproved() },
                    onFail = {
                        BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                            viewModel.biometryFailed()
                        }
                    }
                )

                bioPrompt.authenticate(promptInfo)
            }
            viewModel.resetPromptTrigger()
        }

        PreBiometryDialog(
            mainText = stringResource(id = R.string.save_biometry_info_key_creation),
            onAccept = kickOffBioPrompt
        )
    }
}