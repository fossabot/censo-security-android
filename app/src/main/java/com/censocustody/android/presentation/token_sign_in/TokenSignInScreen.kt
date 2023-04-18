package com.censocustody.android.presentation.token_sign_in

import android.annotation.SuppressLint
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.Resource
import com.censocustody.android.presentation.Screen
import com.censocustody.android.ui.theme.*
import androidx.compose.runtime.Composable
import com.censocustody.android.common.BioCryptoUtil

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")

@Composable
fun TokenSignInScreen(
    navController: NavController,
    email: String?,
    token: String?,
    viewModel: TokenSignInViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(email, token)
        onDispose { }
    }

    //region LaunchedEffect
    LaunchedEffect(key1 = state) {
        if (state.exitLoginFlow is Resource.Success) {
            viewModel.resetExitLoginFlow()
            navController.navigate(Screen.EntranceRoute.route)
        }

        if (state.triggerBioPrompt is Resource.Success) {
            viewModel.resetPromptTrigger()

            val promptInfo = BioCryptoUtil.createPromptInfo(context = context)

            val bioPrompt = BioCryptoUtil.createBioPrompt(
                fragmentActivity = context,
                onSuccess = { viewModel.biometryApproved(it?.cipher) },
                onFail = {
                    BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                        viewModel.biometryFailed()
                    }
                }
            )

            state.triggerBioPrompt.data?.let {
                bioPrompt.authenticate(
                    promptInfo,
                    BiometricPrompt.CryptoObject(state.triggerBioPrompt.data)
                )
            }
        }
    }
    //endregion

    Scaffold(
        content = {
            Box {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(72.dp))
                    Image(
                        modifier = Modifier.width(300.dp),
                        painter = painterResource(R.drawable.logo_red_black),
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                    )
                    Spacer(modifier = Modifier.height(54.dp))
                    val headerText =
                        buildAnnotatedString {
                            append("${stringResource(R.string.signing_in_as)}\n")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(state.email)
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
                    Spacer(modifier = Modifier.height(72.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp),
                        color = ButtonRed
                    )
                }
            }
        }
    )
}