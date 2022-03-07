package com.strikeprotocols.mobile.presentation.sign_in

import android.app.Activity.RESULT_OK
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.credentials.*
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.CredentialsProvider
import com.strikeprotocols.mobile.data.CredentialsProviderImpl
import com.strikeprotocols.mobile.data.CredentialsProviderImpl.Companion.INTENT_FAILED
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.components.SignInTextField
import com.strikeprotocols.mobile.ui.theme.*
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state = viewModel.state

    val credentialsProvider: CredentialsProvider = CredentialsProviderImpl(LocalContext.current)

    //region Credentials Launchers
    val saveCredentialLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) {
        if (it.resultCode != RESULT_OK) {
            viewModel.saveCredentialFailed(INTENT_FAILED)
            return@rememberLauncherForActivityResult
        }
        viewModel.saveCredentialSuccess()
    }

    val retrieveCredentialLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) {
        if (it.resultCode != RESULT_OK) {
            viewModel.retrieveCredentialFailed(INTENT_FAILED)
            return@rememberLauncherForActivityResult
        }

        val credential = it.data?.extras?.get(Credential.EXTRA_KEY) as String?
        viewModel.retrieveCredentialSuccess(credential)
    }
    //endregion

    //region LaunchedEffect
    LaunchedEffect(key1 = state) {
        if (state.loginResult is Resource.Success) {
            viewModel.resetLoginCallAndHandleUserAuthFlow()
        }

        if (state.saveCredential is Resource.Success) {
            navController.navigate(Screen.AuthRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
            viewModel.resetSaveCredential()
        }

        if (state.saveCredential is Resource.Loading) {
            state.initialAuthData?.generatedPassword?.let { generatedPassword ->
                credentialsProvider.saveCredential(
                    email = state.email, //"samiam${Random.nextInt(0,10000)}@jkxajkfadd.com",
                    password = BaseWrapper.encode(generatedPassword),
                    launcher = saveCredentialLauncher,
                    saveSuccess = viewModel::saveCredentialSuccess,
                    saveFailed = viewModel::retrieveCredentialFailed
                )
            }
        }

        if(state.retrieveCredential is Resource.Loading) {
            credentialsProvider.retrieveCredential(
                launcher = retrieveCredentialLauncher,
                retrievalSuccess = viewModel::retrieveCredentialSuccess,
                retrievalFailed = viewModel::retrieveCredentialFailed
            )
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
            if (state.loadingData) {
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

    if (state.loginResult is Resource.Error) {
        AlertDialog(
            backgroundColor = UnfocusedGrey,
            onDismissRequest = viewModel::resetLoginCallAndHandleUserAuthFlow,
            confirmButton = {
                TextButton(
                    onClick = viewModel::resetLoginCallAndHandleUserAuthFlow
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
    }
    //endregion
}
