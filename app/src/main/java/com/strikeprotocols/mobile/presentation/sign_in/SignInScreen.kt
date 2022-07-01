package com.strikeprotocols.mobile.presentation.sign_in

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.getAuthFlowErrorMessage
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.components.SignInTextField
import com.strikeprotocols.mobile.ui.theme.*


@OptIn(
    ExperimentalComposeUiApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)


@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: SignInViewModel = hiltViewModel(),
) {

    val state = viewModel.state

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    //region DisposableEffect
    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { }
    }
    //endregion

    //region LaunchedEffect
    LaunchedEffect(key1 = state) {
        if (state.loginResult is Resource.Success) {
            viewModel.resetLoginCallAndRetrieveUserInformation()
        }

        if (state.keyCreationFlowStep == KeyCreationFlowStep.FINISHED) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
            viewModel.setUserLoggedInSuccess()
            viewModel.exitPhraseFlow()
            viewModel.loadingFinished()
            viewModel.resetAddWalletSignersCall()
        }

        if (state.keyRecoveryFlowStep == KeyRecoveryFlowStep.FINISHED) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
            viewModel.setUserLoggedInSuccess()
            viewModel.exitPhraseFlow()
            viewModel.loadingFinished()
            viewModel.resetAddWalletSignersCall()
        }

        if (state.regenerateData is Resource.Success) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
            viewModel.setUserLoggedInSuccess()
            viewModel.loadingFinished()
            viewModel.resetRegenerateData()
        }

        if (state.keyValid is Resource.Success) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
            viewModel.setUserLoggedInSuccess()
            viewModel.resetValidKey()
        }

        if (state.shouldAbortUserFromAuthFlow) {
            navController.navigate(Screen.ContactStrikeRoute.route) { popUpToId }
        }
    }
    //endregion

    //region Core UI
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        val passwordVisibility = remember { mutableStateOf(false) }

        Spacer(modifier = Modifier.weight(3f))
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp, vertical = 36.dp),
            painter = painterResource(R.drawable.strike_main_logo),
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
        )
        Spacer(modifier = Modifier.weight(0.75f))
        SignInTextField(
            valueText = state.email,
            onValueChange = viewModel::updateEmail,
            placeholder = stringResource(R.string.email_hint),
            keyboardType = KeyboardType.Email,
            errorEnabled = state.emailErrorEnabled
        )
        Spacer(modifier = Modifier.size(20.dp))
        SignInTextField(
            valueText = state.password,
            onValueChange = viewModel::updatePassword,
            placeholder = stringResource(R.string.password_hint),
            keyboardType = KeyboardType.Password,
            onPasswordClick = { passwordVisibility.value = !passwordVisibility.value },
            passwordVisibility = passwordVisibility.value,
            onDoneAction = viewModel::attemptLogin,
            errorEnabled = state.passwordErrorEnabled,
            isPassword = true
        )

        Spacer(modifier = Modifier.weight(6f))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = StrikePurple,
                disabledBackgroundColor = StrikePurple,
            ),
            enabled = state.signInButtonEnabled,
            onClick = viewModel::attemptLogin
        ) {
            if (state.manualAuthFlowLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(40.dp),
                    color = StrikeWhite,
                    strokeWidth = 4.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.sign_in_button),
                    fontSize = 16.sp,
                    color = if (state.signInButtonEnabled) StrikeWhite else DisabledButtonTextColor
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    //region PhraseVerificationUI
    if (state.showKeyCreationUI) {
        Box {
            PhraseVerificationFlowUI(
                phrase = state.phrase ?: "",
                pastedPhrase = state.pastedPhrase,
                phraseVerificationFlowStep = state.keyCreationFlowStep,
                wordIndex = state.wordIndex,
                onPhraseFlowAction = viewModel::phraseFlowAction,
                onNavigate = viewModel::phraseVerificationAction,
                onBackNavigate = viewModel::phraseVerificationBackNavigation,
                verifyPastedPhrase = viewModel::verifyPastedPhrase,
                onExit = viewModel::exitPhraseFlow
            )
        }
    }
    //endregion

    //region PhraseKeyRegenerationUI
    if (state.showKeyRecoveryUI) {
        Box {
            PhraseRecoveryFlowUI(
                pastedPhrase = state.pastedPhrase,
                keyRecoveryFlowStep = state.keyRecoveryFlowStep,
                onNavigate = viewModel::phraseRegenerationAction,
                onBackNavigate = viewModel::phraseRegenerationBackNavigation,
                verifyPastedPhrase = viewModel::verifyPhraseToRecoverKeyPair,
                onExit = viewModel::exitPhraseFlow,
                onPhraseFlowAction = viewModel::phraseFlowAction
            )
        }
    }
    //endregion

    if (state.loggedInStatusResult is Resource.Loading || state.autoAuthFlowLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = BackgroundBlack)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 172.dp, start = 44.dp, end = 44.dp),
                painter = painterResource(R.drawable.strike_main_logo),
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
            )
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp),
                color = StrikeWhite
            )
        }
    }

    if (state.loginResult is Resource.Error) {
        AlertDialog(
            backgroundColor = UnfocusedGrey,
            onDismissRequest = viewModel::resetLoginCall,
            confirmButton = {
                TextButton(
                    onClick = viewModel::resetLoginCall
                )
                {
                    Text(text = stringResource(R.string.ok))
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.login_failed_title),
                    color = StrikeWhite,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.login_failed_message),
                    color = StrikeWhite,
                    fontSize = 16.sp
                )
            }
        )
        viewModel.loadingFinished()
    }

    if (state.verifyUserResult is Resource.Error) {
        viewModel.loadingFinished()
        PhraseAlertDialog(
            dialogTitle = stringResource(R.string.verify_user_fail_title),
            dialogText = stringResource(R.string.verify_user_fail_message),
            onConfirm = {
                viewModel.loadingFinished()
                viewModel.resetVerifyUserResult()
            }
        )
    }

    if (state.walletSignersResult is Resource.Error) {
        viewModel.loadingFinished()
        PhraseAlertDialog(
            dialogTitle = stringResource(R.string.wallet_signers_fail_title),
            dialogText = stringResource(R.string.wallet_signers_fail_message),
            onConfirm = {
                viewModel.loadingFinished()
                viewModel.resetWalletSignersCall()
            }
        )
    }

    if(state.addWalletSignerResult is Resource.Error || state.regenerateData is Resource.Error) {
        viewModel.loadingFinished()
        PhraseAlertDialog(
            dialogTitle = stringResource(R.string.unable_to_add_wallet_signer_title),
            dialogText = stringResource(R.string.unable_to_add_wallet_signer_message),
            onConfirm = {
                viewModel.loadingFinished()
                viewModel.resetAddWalletSignersCall()
                viewModel.resetRegenerateData()
            }
        )
    }

    if (state.authFlowException is Resource.Success) {
        AlertDialog(
            backgroundColor = UnfocusedGrey,
            onDismissRequest = {
                viewModel.resetAuthFlowException()
                viewModel.resetLoginCall()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAuthFlowException()
                        viewModel.resetLoginCall()
                    }
                )
                {
                    Text(text = stringResource(R.string.ok))
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.login_failed_title),
                    color = StrikeWhite,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = getAuthFlowErrorMessage(
                        e = state.authFlowException.data ?: Exception(),
                        context = LocalContext.current
                    ),
                    color = StrikeWhite,
                    fontSize = 16.sp
                )
            }
        )
        viewModel.loadingFinished()
    }
    //endregion
}

@Composable
fun PhraseAlertDialog(
    dialogTitle: String = stringResource(id = R.string.phrase_dialog_title),
    dialogText: String,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        backgroundColor = UnfocusedGrey,
        onDismissRequest = onConfirm,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.ok))
            }
        },
        title = {
            Text(
                text = dialogTitle,
                color = StrikeWhite,
                fontSize = 22.sp
            )
        },
        text = {
            Text(
                text = dialogText,
                color = StrikeWhite,
                fontSize = 17.sp
            )
        }
    )
}