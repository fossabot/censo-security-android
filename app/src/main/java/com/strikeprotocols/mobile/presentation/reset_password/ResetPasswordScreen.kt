package com.strikeprotocols.mobile.presentation.reset_password

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.presentation.components.SignInTextField
import com.strikeprotocols.mobile.presentation.components.StrikeAuthTopAppBar
import com.strikeprotocols.mobile.presentation.components.StrikeCenteredTopAppBar
import com.strikeprotocols.mobile.ui.theme.*


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(
    ExperimentalComposeUiApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    resetPasswordViewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val resetPasswordState = resetPasswordViewModel.state
    val context = LocalContext.current

    //region Screen Content
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            StrikeCenteredTopAppBar(
                title = stringResource(R.string.reset_password),
                onAppBarIconClick = { navController.navigateUp() },
                navigationIconContentDes = stringResource(id = R.string.content_des_back_icon),
                navigationIcon = Icons.Rounded.ArrowBack
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BackgroundBlack)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if(resetPasswordState.resetPasswordResult is Resource.Success) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.reset_pswd_email_sent),
                        color = StrikeWhite,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.email_hint),
                    color = StrikeWhite,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.size(20.dp))
                SignInTextField(
                    valueText = resetPasswordState.email,
                    onValueChange = resetPasswordViewModel::updateEmail,
                    keyboardType = KeyboardType.Email,
                    errorEnabled = resetPasswordState.emailErrorEnabled,
                    errorText =
                    if (resetPasswordState.resetPasswordResult is Resource.Error) {
                        val errorReason = resetPasswordState.resetPasswordResult.strikeError?.getErrorMessage(context)

                        if(errorReason == null) {
                            stringResource(R.string.unable_send_reset_email)
                        } else {
                            "${errorReason}\n\n${stringResource(R.string.unable_send_reset_email)}"
                        }
                    } else {
                        ""
                    },
                    showDoneAction = true,
                    onDoneAction = { resetPasswordViewModel.submitResetPassword() },
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = StrikePurple,
                        disabledBackgroundColor = StrikePurple,
                    ),
                    enabled = resetPasswordState.resetButtonEnabled,
                    onClick = resetPasswordViewModel::submitResetPassword
                ) {
                    when (resetPasswordState.resetPasswordResult) {
                        is Resource.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.height(40.dp),
                                color = StrikeWhite,
                                strokeWidth = 4.dp,
                            )
                        }
                        is Resource.Success -> {
                            Text(
                                text = stringResource(R.string.retry_recover_password),
                                fontSize = 16.sp,
                                color = if (resetPasswordState.resetButtonEnabled) StrikeWhite else DisabledButtonTextColor
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.reset_password),
                                fontSize = 16.sp,
                                color = if (resetPasswordState.resetButtonEnabled) StrikeWhite else DisabledButtonTextColor
                            )
                        }
                    }

                }
            }
        }
    )
}