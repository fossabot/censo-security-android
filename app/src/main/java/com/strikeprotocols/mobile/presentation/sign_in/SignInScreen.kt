package com.strikeprotocols.mobile.presentation.sign_in

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
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
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.getAuthFlowErrorMessage
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.InvalidKeyPhraseException
import com.strikeprotocols.mobile.data.RegenerateKeyPhraseException
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

    //region PhraseVerificationUI
    if (state.showPhraseVerificationUI) {
        val phrase = state.phrase
        val words = phrase?.split(" ") ?: emptyList()
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
                if (phrase != null) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText(SignInViewModel.CLIPBOARD_LABEL_PHRASE, phrase)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, context.getString(R.string.copy_key_phrase_success), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.copied_key_phrase_failure), Toast.LENGTH_LONG).show()
                }
            }) {
                Text(
                    text = "Copy full phrase",
                    color = StrikeWhite, fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
            Button(onClick = {
                viewModel.verifyPhraseToGenerateKeyPair()
            }) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    clipboard.clearPrimaryClip()
                } else {
                    val clip: ClipData = ClipData.newPlainText(SignInViewModel.CLIPBOARD_LABEL_PHRASE, "")
                    clipboard.setPrimaryClip(clip)
                }
                Text(
                    text = "I Have Saved The Full Phrase",
                    color = StrikeWhite, fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
    }
    //endregion

    //region PhraseKeyRegenerationUI
    if (state.showPhraseKeyRegenerationUI) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .clickable { },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                value = state.phrase ?: "",
                onValueChange = viewModel::updatePhrase,
                placeholder = {
                    Text(text = "Enter phrase here", color = StrikeWhite)
                },
                textStyle = MaterialTheme.typography.subtitle1.copy(color = StrikeWhite),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = StrikeWhite,
                    backgroundColor = UnfocusedGrey
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(36.dp))
            Button(
                onClick = {
                    val primaryClipDescription = clipboard.primaryClipDescription
                    if (primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) != true) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.invalid_clipboard_data),
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    val item = clipboard.primaryClip?.getItemAt(0)
                    if (item == null || item.text == null) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.unable_to_retrieve_clipboard_data),
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    viewModel.updatePhrase(phrase = item.text.toString())
                }) {
                Text(
                    text = "Paste phrase",
                    color = StrikeWhite, fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                enabled = !state.phrase.isNullOrEmpty(),
                onClick = {
                viewModel.verifyPhraseToRegenerateKeyPair()
            }) {
                if (state.keyRegenerationLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(40.dp),
                        color = StrikeWhite,
                        strokeWidth = 4.dp,
                    )
                } else {
                    if (state.phrase.isNullOrEmpty()) {
                        Text(
                            text = "Please enter key phrase for regeneration",
                            color = StrikeWhite, fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Submit key phrase for regeneration",
                            color = StrikeWhite, fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(250.dp))
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
        val message: String = when (state.regenerateKeyFromPhrase.message) {
            RegenerateKeyPhraseException.DEFAULT_KEY_REGENERATION_ERROR -> {
                stringResource(R.string.key_regeneration_error)
            }
            InvalidKeyPhraseException.INVALID_KEY_PHRASE_ERROR -> {
                stringResource(R.string.invalid_key_phrase_error)
            }
            else -> {
                stringResource(R.string.phrase_regeneration_fail)
            }
        }

        PhraseAlertDialog(
            dialogTitle = stringResource(R.string.phrase_verification_dialog_fail_title),
            dialogText = message,
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