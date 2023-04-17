package com.censocustody.android.presentation.reauthenticate

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.censocustody.android.R
import com.censocustody.android.common.BioCryptoUtil
import com.censocustody.android.common.Resource
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.approvals.NavIconTopBar
import com.censocustody.android.presentation.key_management.BackgroundUI
import com.censocustody.android.presentation.key_management.PreBiometryDialog
import com.censocustody.android.ui.theme.ButtonRed
import com.censocustody.android.ui.theme.CensoWhite
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ReAuthenticateScreen(
    navController: NavController,
    viewModel: ReAuthenticateViewModel = hiltViewModel(),
) {
    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {}
    }

    LaunchedEffect(key1 = state) {

        if (state.loginResult is Resource.Success) {
            viewModel.resetLoginResult()

            navController.navigate(Screen.EntranceRoute.route) {
                popUpTo(Screen.SignInRoute.route) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            NavIconTopBar(
                title = stringResource(R.string.re_authenticate_title),
                onAppBarIconClick = { navController.navigate(Screen.AccountRoute.route) },
                navigationIcon = Icons.Rounded.AccountCircle,
                navigationIconContentDes = stringResource(id = R.string.content_des_account_icon)
            )
        },
        content = { _ ->

            BackgroundUI()
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.loginResult !is Resource.Error) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(60.dp)
                            .align(alignment = Alignment.Center),
                        color = ButtonRed,
                        strokeWidth = 5.dp,
                    )
                }
            }
        }
    )


    if (state.triggerBioPrompt is Resource.Success) {
        val kickOffBioPrompt = {
            val promptInfo = BioCryptoUtil.createPromptInfo(context = context)
            val bioPrompt = BioCryptoUtil.createBioPrompt(fragmentActivity = context,
                onSuccess = { viewModel.biometryApproved() },
                onFail = {
                    BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                        viewModel.biometryFailed()
                    }
                })

            bioPrompt.authenticate(promptInfo)

            viewModel.resetPromptTrigger()
        }

        PreBiometryDialog(
            mainText = stringResource(R.string.reauth_bioprompt_message),
            onAccept = kickOffBioPrompt
        )
    }

    if (state.loginResult is Resource.Error) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Failed to Authenticate User", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(28.dp))
                Button(onClick = viewModel::retry) {
                    Text(stringResource(R.string.try_again), fontSize = 18.sp, color = CensoWhite)
                }
            }
        }
    }

}