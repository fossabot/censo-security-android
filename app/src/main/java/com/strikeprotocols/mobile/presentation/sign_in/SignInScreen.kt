package com.strikeprotocols.mobile.presentation.sign_in

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.getAuthFlowErrorMessage
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.components.AutoCompleteUI
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
    val context = LocalContext.current

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

        if (state.verifiedPhrase is Resource.Success) {
            Toast.makeText(context, "Phrase verified, by app. Saving private key to storage. Adding wallet signer...", Toast.LENGTH_LONG).show()
        }

        if (state.addWalletSignerResult is Resource.Success)  {
            navController.navigate(Screen.ApprovalListRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
            viewModel.setUserLoggedInSuccess()
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

        if (state.verifiedPhrase is Resource.Loading) {
            viewModel.launchPhraseVerificationUI()
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

    if (state.showPhraseVerificationUI) {
        val words = state.phrase?.split(" ") ?: emptyList()
        val leftWords = words.filterIndexed { index, _ -> index % 2 == 0 }
        val rightWords = words.filterIndexed { index, _ -> index % 2 != 0 }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .clickable { },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            AutoCompleteUI(
                modifier = Modifier.fillMaxWidth(),
                query = viewModel.state.wordQuery,
                queryHint = stringResource(R.string.key_phrase_hint),
                predictions = viewModel.state.wordPredictions,
                onQueryChanged = viewModel::updatePredictions,
                onClearClick = viewModel::clearQuery,
                onItemClick = viewModel::wordSelected,
                onDoneActionClick = viewModel::wordEntered,
                itemContent = {
                    Box(modifier = Modifier.background(color = StrikeWhite)) {
                        Text(text = it, color = Color.Black)
                    }
                }
            )

            Row {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    for (leftWord in leftWords) {
                        val touched = remember { mutableStateOf(false) }

                        Text(
                            text = leftWord,
                            color = StrikeWhite,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .background(
                                    color = if (touched.value) Color.White else Color.Transparent
                                )
                                .clickable {
                                    touched.value = !touched.value
                                }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    for (rightWord in rightWords) {
                        val touched = remember { mutableStateOf(false) }

                        Text(
                            text = rightWord,
                            color = StrikeWhite,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .background(
                                    color = if (touched.value) Color.White else Color.Transparent
                                )
                                .clickable {
                                    touched.value = !touched.value
                                }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
            Button(onClick = {
                viewModel.verifyPhraseToGenerateKeyPair(state.phrase ?: "")
            }) {
                Text(
                    text = "I Have Saved The Full Phrase",
                    color = StrikeWhite, fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (state.showPhraseKeyRegenerationUI) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .clickable { },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(onClick = {
                viewModel.verifyPhraseToRegenerateKeyPair()
            }) {
                if (state.keyRegenerationLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(40.dp),
                        color = StrikeWhite,
                        strokeWidth = 4.dp,
                    )
                } else {
                    Text(
                        text = "I have entered my secret phrase",
                        color = StrikeWhite, fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }

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

    if (state.showPhraseVerificationDialog) {
        PhraseAlertDialog(
            dialogText = stringResource(R.string.phrase_dialog_verification),
            onConfirm = viewModel::launchVerifyPhraseFlow
        )
    }

    if(state.verifiedPhrase is Resource.Error) {
        val errorMessage = state.verifiedPhrase.message ?: stringResource(R.string.phrase_verification_fail)
        viewModel.resetShouldDisplayPhraseVerificationDialog()
        viewModel.loadingFinished()
        PhraseAlertDialog(
            dialogTitle = stringResource(R.string.phrase_verification_dialog_fail_title),
            dialogText = errorMessage,
            onConfirm = {
                viewModel.resetVerifiedPhrase()
            }
        )
    }

    if(state.regenerateKeyFromPhrase is Resource.Error) {
        PhraseAlertDialog(
            dialogTitle = stringResource(R.string.phrase_verification_dialog_fail_title),
            dialogText = stringResource(R.string.phrase_regeneration_fail),
            onConfirm = {
                viewModel.restartRegenerateKeyFromPhraseFlow()
            }
        )
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