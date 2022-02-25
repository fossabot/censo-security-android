package com.strikeprotocols.mobile.presentation.sign_in

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.components.SignInTextField
import com.strikeprotocols.mobile.ui.theme.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state = viewModel.state

    LaunchedEffect(key1 = state) {
        if (state.loginResult is Resource.Success) {
            viewModel.attemptVerify()
//            navController.navigate(Screen.ApprovalListRoute.route) {
//                popUpTo(Screen.SignInRoute.route) {
//                    inclusive = true
//                }
//            }
            viewModel.resetLoginCall()
        }
    }

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
                    color = if (state.signInButtonEnabled) StrikeWhite else DisabledButtonTextColor
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
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
    }
}
