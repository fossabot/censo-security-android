package com.censocustody.mobile.presentation.reset_password

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.mobile.R
import com.censocustody.mobile.common.CensoButton
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.presentation.components.SignInTextField
import com.censocustody.mobile.presentation.components.CenteredTopAppBar
import com.censocustody.mobile.ui.theme.*


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

    //region Screen Content
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenteredTopAppBar(
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
                        color = CensoWhite,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(48.dp))
                SignInTextField(
                    valueText = resetPasswordState.email,
                    onValueChange = resetPasswordViewModel::updateEmail,
                    keyboardType = KeyboardType.Email,
                    errorEnabled = resetPasswordState.emailErrorEnabled,
                    errorText =
                    if (resetPasswordState.resetPasswordResult is Resource.Error) {
                        stringResource(R.string.reset_email_error)
                    } else {
                        ""
                    },
                    showDoneAction = true,
                    onDoneAction = { resetPasswordViewModel.submitResetPassword() },
                )
                Spacer(modifier = Modifier.height(48.dp))
                CensoButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = resetPasswordState.resetButtonEnabled,
                    height = 54.dp,
                    onClick = resetPasswordViewModel::submitResetPassword) {
                    val textColor =  if(resetPasswordState.resetButtonEnabled) CensoWhite else CensoWhite.copy(alpha = 0.35f)
                    when (resetPasswordState.resetPasswordResult) {
                        is Resource.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.height(40.dp),
                                color = CensoWhite,
                                strokeWidth = 4.dp,
                            )
                        }
                        is Resource.Success -> {
                            Text(
                                text = stringResource(R.string.retry_recover_password),
                                fontSize = 16.sp,
                                color = textColor
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.reset_password),
                                fontSize = 16.sp,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    )
}