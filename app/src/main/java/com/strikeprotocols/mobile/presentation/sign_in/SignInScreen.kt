package com.strikeprotocols.mobile.presentation.sign_in

import android.annotation.SuppressLint
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.components.SignInTextField
import com.strikeprotocols.mobile.presentation.components.StrikeSignInTopAppBar
import com.strikeprotocols.mobile.presentation.key_management.PhraseBackground
import com.strikeprotocols.mobile.ui.theme.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)

@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current

    //region LaunchedEffect
    LaunchedEffect(key1 = state) {
        if (state.loginResult is Resource.Success) {
            viewModel.resetLoginCall()

            navController.navigate(Screen.EntranceRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
        }
    }
    //endregion

    //region Core UI
    val keyboardController = LocalSoftwareKeyboardController.current

    val screenTitle =
        if (state.loginStep == LoginStep.EMAIL_ENTRY) "" else stringResource(id = R.string.email)

    val onNavClick =
        if (state.loginStep == LoginStep.EMAIL_ENTRY) { { } } else viewModel::moveBackToEmailScreen

    Scaffold(
        topBar = {
            StrikeSignInTopAppBar(
                title = screenTitle,
                onAppBarIconClick = onNavClick,
            )
        },
        content = {
            Box {
                PhraseBackground()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val passwordVisibility = remember { mutableStateOf(false) }

                    Image(
                        modifier = Modifier.padding(),
                        painter = painterResource(R.drawable.strike_main_logo),
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                    )
                    Spacer(modifier = Modifier.weight(1.25f))
                    val headerText =
                        buildAnnotatedString {
                            if (state.loginStep == LoginStep.EMAIL_ENTRY) {
                                append(stringResource(R.string.sign_in_subtitle))
                            } else {
                                append("${stringResource(R.string.signing_in_as)}\n")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(state.email)
                                }
                            }
                        }
                    Text(
                        text = headerText,
                        color = StrikeWhite,
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        letterSpacing = 0.25.sp
                    )
                    Spacer(modifier = Modifier.weight(1.0f))

                    val boxItemsHorizontalPadding = 32.dp

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(width = 1.dp, color = UnfocusedGrey.copy(alpha = 0.50f))
                            .background(color = Color.Black)
                            .zIndex(2.5f)
                    ) {
                        Spacer(modifier = Modifier.size(20.dp))
                        if (state.loginStep == LoginStep.EMAIL_ENTRY) {
                            Text(
                                modifier = Modifier.padding(horizontal = boxItemsHorizontalPadding),
                                text = stringResource(id = R.string.email_hint),
                                color = StrikeWhite,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                            SignInTextField(
                                modifier = Modifier.padding(horizontal = boxItemsHorizontalPadding),
                                valueText = state.email,
                                onValueChange = viewModel::updateEmail,
                                onDoneAction = viewModel::signInActionCompleted,
                                keyboardType = KeyboardType.Email,
                                errorEnabled = state.emailErrorEnabled,
                                showDoneAction = false
                            )
                        } else {
                            Text(
                                modifier = Modifier.padding(horizontal = boxItemsHorizontalPadding),
                                text = stringResource(id = R.string.password_hint),
                                color = StrikeWhite,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.size(16.dp))
                            SignInTextField(
                                modifier = Modifier.padding(horizontal = boxItemsHorizontalPadding),
                                valueText = state.password,
                                onValueChange = viewModel::updatePassword,
                                keyboardType = KeyboardType.Password,
                                onPasswordClick = {
                                    passwordVisibility.value = !passwordVisibility.value
                                },
                                passwordVisibility = passwordVisibility.value,
                                onDoneAction = viewModel::attemptLogin,
                                errorEnabled = state.passwordErrorEnabled,
                                isPassword = true,
                                showDoneAction = true
                            )
                        }
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.weight(6f))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = StrikePurple,
                            disabledBackgroundColor = StrikePurple,
                        ),
                        enabled = true,
                        onClick = {
                            if(state.loginStep != LoginStep.EMAIL_ENTRY) {
                                keyboardController?.hide()
                            }
                            viewModel.signInActionCompleted()
                        }
                    ) {
                        if (state.loginResult is Resource.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(40.dp),
                                color = StrikeWhite,
                                strokeWidth = 4.dp,
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.sign_in_button),
                                fontSize = 16.sp,
                                color = StrikeWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (state.loginResult is Resource.Error) {
                val errorReason = state.loginResult.strikeError?.getErrorMessage(context)

                if (errorReason == null) {
                    stringResource(R.string.login_failed_message)
                } else {
                    "${errorReason}\n\n${stringResource(R.string.login_failed_message)}"
                }

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
                            text = errorReason ?: stringResource(R.string.login_failed_message),
                            color = StrikeWhite,
                            fontSize = 16.sp
                        )
                    }
                )
            }
        }
    )
    //endregion
}