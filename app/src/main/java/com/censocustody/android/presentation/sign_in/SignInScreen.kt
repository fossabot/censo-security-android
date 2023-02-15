package com.censocustody.android.presentation.sign_in

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.BioCryptoUtil
import com.censocustody.android.common.CensoButton
import com.censocustody.android.common.Resource
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.components.SignInTextField
import com.censocustody.android.presentation.components.SignInTopAppBar
import com.censocustody.android.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)

@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    //region LaunchedEffect
    LaunchedEffect(key1 = state) {
        if (state.exitLoginFlow is Resource.Success) {
            viewModel.resetExitLoginFlow()

            navController.navigate(Screen.EntranceRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
        }

        if (state.triggerBioPrompt is Resource.Success) {
            viewModel.resetPromptTrigger()

            state.triggerBioPrompt.data?.let {
                val promptInfo = BioCryptoUtil.createPromptInfo(context = context)

                val bioPrompt = BioCryptoUtil.createBioPrompt(
                    fragmentActivity = context,
                    onSuccess = {
                        if (it != null) {
                            viewModel.biometryApproved(it)
                        } else {
                            BioCryptoUtil.handleBioPromptOnFail(
                                context = context,
                                errorCode = BioCryptoUtil.NO_CIPHER_CODE
                            ) {
                                viewModel.biometryFailed()
                            }
                        }
                    },
                    onFail = {
                        BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                            viewModel.biometryFailed()
                        }
                    }
                )

                bioPrompt.authenticate(promptInfo, state.triggerBioPrompt.data)
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
            SignInTopAppBar(
                title = screenTitle,
                onAppBarIconClick = onNavClick,
            )
        },
        content = {
            Box {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val passwordVisibility = remember { mutableStateOf(false) }
                    Image(
                        modifier = Modifier.width(200.dp),
                        painter = painterResource(R.drawable.logo_red_black),
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                    )
                    Spacer(modifier = Modifier.weight(1.75f))
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
                        color = TextBlack,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        letterSpacing = 0.25.sp
                    )
                    val boxItemsHorizontalPadding = 8.dp

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.size(20.dp))
                        if (state.loginStep == LoginStep.EMAIL_ENTRY) {
                            Spacer(modifier = Modifier.size(16.dp))
                            SignInTextField(
                                modifier = Modifier.padding(horizontal = boxItemsHorizontalPadding),
                                valueText = state.email,
                                placeholder = stringResource(R.string.email_hint),
                                onValueChange = viewModel::updateEmail,
                                onDoneAction = viewModel::signInActionCompleted,
                                keyboardType = KeyboardType.Email,
                                errorEnabled = state.emailErrorEnabled,
                                showDoneAction = false
                            )
                        } else {
                            SignInTextField(
                                modifier = Modifier.padding(horizontal = boxItemsHorizontalPadding),
                                valueText = state.password,
                                placeholder = stringResource(id = R.string.password_hint),
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
                        Spacer(modifier = Modifier.size(12.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .clickable { navController.navigate(Screen.ResetPasswordRoute.route) }
                                    .padding(top = 8.dp, end = boxItemsHorizontalPadding),
                                text = stringResource(R.string.reset_password),
                                color = TextRed,
                                textAlign = TextAlign.End,
                                fontWeight = FontWeight.W400
                            )
                        }
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.weight(6f))
                    CensoButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        enabled = state.email.isNotEmpty(),
                        onClick = {
                            if(state.loginStep != LoginStep.EMAIL_ENTRY) {
                                keyboardController?.hide()
                            }
                            viewModel.signInActionCompleted()
                        }) {
                        if (state.loginResult is Resource.Loading) {
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(28.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(28.dp),
                                    color = CensoWhite,
                                    strokeWidth = 2.dp,
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.sign_in_button),
                                fontSize = 18.sp,
                                color = if(state.email.isNotEmpty()) CensoWhite else CensoWhite.copy(alpha = 0.35f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (state.loginResult is Resource.Error) {
                val errorReason = state.loginResult.censoError?.getErrorMessage(context)

                if (state.loginStep == LoginStep.PASSWORD_ENTRY) {

                    if (errorReason == null) {
                        stringResource(R.string.login_failed_message)
                    } else {
                        "${errorReason}\n\n${stringResource(R.string.login_failed_message)}"
                    }

                    SignInAlertDialog(
                        title = stringResource(R.string.login_failed_title),
                        confirmText = stringResource(R.string.ok),
                        dismissText = stringResource(id = R.string.cancel),
                        onCancel = viewModel::resetLoginCall,
                        onExit = viewModel::resetLoginCall,
                        onConfirm = viewModel::resetLoginCall,
                        message = errorReason ?: stringResource(R.string.login_failed_message),
                        showDismissButton = false
                    )
                } else {
                    SignInAlertDialog(
                        title = stringResource(R.string.sign_in_error),
                        confirmText = stringResource(R.string.try_again),
                        dismissText = stringResource(id = R.string.login_with_password),
                        onCancel = viewModel::skipToPasswordEntry,
                        onExit = viewModel::resetLoginCall,
                        onConfirm = viewModel::kickOffBiometryLoginOrMoveToPasswordEntry,
                        message = errorReason
                            ?: stringResource(R.string.error_occurred_signature_login),
                        showDismissButton = true
                    )
                }
            }
        }
    )
    //endregion
}

@Composable
fun SignInAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    confirmText: String,
    dismissText: String,
    onExit: () -> Unit,
    showDismissButton: Boolean
    ) {
    val upperInteractionSource = remember { MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .padding(4.dp)
            .clickable(indication = null, interactionSource = upperInteractionSource) { },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val innerInteractionSource = remember { MutableInteractionSource() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(color = Color.Transparent)
                .border(
                    width = 1.0.dp,
                    shape = RoundedCornerShape(4.dp),
                    color = BorderGrey,
                )
                .shadow(elevation = 2.5.dp)
                .clickable(indication = null, interactionSource = innerInteractionSource) { },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DialogMainBackground,
                        shape = RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp),
                    )
                    .padding(vertical = 16.dp),
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .align(Alignment.Center),
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = TextBlack,
                    fontSize = 24.sp
                )
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    onClick = onExit
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_dialog),
                        tint = TextBlack
                    )
                }
            }
            Divider(thickness = 2.dp, color = BorderGrey)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(
                    color = DialogMainBackground,
                    shape = RoundedCornerShape(0.dp, 0.dp, 4.dp, 4.dp),
                )
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 48.dp),
                    text = message,
                    textAlign = TextAlign.Center,
                    color = TextBlack,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val modifier: Modifier = if (showDismissButton) Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .weight(1.35f)
                        .fillMaxWidth()
                    else Modifier
                        .clip(RoundedCornerShape(36.dp))
                        .width(72.dp)
                    val textAlign = if (showDismissButton) TextAlign.Center else TextAlign.Start
                    if (showDismissButton) {
                        Spacer(modifier = Modifier.weight(0.20f))
                        Button(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .weight(1.35f)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = CancelButtonGrey
                            ),
                            onClick = onCancel
                        ) {
                            Text(
                                modifier = Modifier.padding(vertical = 2.dp),
                                fontSize = 18.sp,
                                text = dismissText,
                                color = CensoWhite,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(0.20f))
                    Button(
                        modifier = modifier,
                        onClick = onConfirm,
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 2.dp),
                            text = confirmText,
                            fontSize = 18.sp,
                            color = CensoWhite,
                            textAlign = textAlign
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.20f))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
