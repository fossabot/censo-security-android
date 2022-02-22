package com.strikeprotocols.mobile.presentation.auth

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.strikeprotocols.mobile.R
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.ui.theme.*

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {

    val state = authViewModel.state

    val context = LocalContext.current as FragmentActivity

    fun showToast(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    //STRONG vs WEAK: https://source.android.com/compatibility/12/android-12-cdd#7_3_10_biometric_sensors
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(stringResource(R.string.bio_prompt_title))
        .setSubtitle(stringResource(R.string.bio_prompt_subtitle))
        .setNegativeButtonText(stringResource(R.string.cancel))
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()


    val bioPrompt = BiometricPrompt(context, object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            authViewModel.goBackToSetup()
            showToast("Authentication error: $errString")
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            authViewModel.updateStep()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            authViewModel.goBackToSetup()
            showToast("Authentication failed")
        }
    })

    LaunchedEffect(key1 = state) {
        if (state.triggerBioPrompt) {
            authViewModel.resetPromptTrigger()
            bioPrompt.authenticate(promptInfo)
        }

        if(state.authStep == AuthStep.LEAVE_SCREEN) {
            navController.navigate(Screen.ApprovalListRoute.route)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundBlack, GradientPurple, BackgroundBlack)
                )
            )
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        //region Logo
        Spacer(modifier = Modifier.weight(0.5f))
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp, end = 60.dp, top = 60.dp, bottom = 40.dp),
            painter = painterResource(id = R.drawable.strike_main_logo),
            contentDescription = stringResource(R.string.content_des_strike_logo),
            contentScale = ContentScale.FillWidth
        )
        //endregion

        //region Box of Content
        val verticalBoxPadding = when(state.authStep) {
            AuthStep.SETUP, AuthStep.BIOMETRIC -> 54.dp
            AuthStep.PROCESSING -> 30.dp
            AuthStep.FINISHED, AuthStep.LEAVE_SCREEN -> 24.dp
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 8f)
                .align(alignment = Alignment.CenterHorizontally)
                .background(color = Color.Transparent)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(2.dp))
                    .border(width = 1.dp, color = UnfocusedGrey)
                    .background(color = Color.Black)
                    .align(Alignment.Center)
                    .padding(vertical = verticalBoxPadding, horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                AuthBoxContent(authStep = state.authStep, content = this)
            }
        }
        //endregion

        //region Action Button
        Spacer(modifier = Modifier.weight(2f))
        if (state.letUserAdvance) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = StrikePurple,
                    disabledBackgroundColor = StrikePurple
                ),
                onClick = authViewModel::updateStep
            ) {
                Text(
                    text = if (state.authStep == AuthStep.SETUP) {
                        stringResource(R.string.setup_two_factor_auth)
                    } else {
                        stringResource(
                            R.string.dismiss
                        )
                    },
                    fontSize = 16.sp,
                    color = StrikeWhite,
                    style = TextStyle.Default.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(54.dp))
        }
        //endregion

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AuthBoxContent(
    authStep: AuthStep,
    content: ColumnScope
) {
    content.run {

        val textModifier = Modifier
            .padding(horizontal = 20.dp)
            .align(Alignment.CenterHorizontally)

        when (authStep) {
            AuthStep.SETUP, AuthStep.BIOMETRIC -> {
                AuthBoxTitle(modifier = textModifier, textRes = R.string.two_factor_auth_request)
                Spacer(modifier = Modifier.height(44.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        modifier = Modifier.padding(end = 8.dp),
                        painter = painterResource(id = R.drawable.ic_android_face),
                        contentDescription = stringResource(id = R.string.content_des_face_id)
                    )

                    Image(
                        modifier = Modifier.padding(start = 8.dp),
                        painter = painterResource(id = R.drawable.ic_android_fingerprint),
                        contentDescription = stringResource(id = R.string.content_des_finger_print)
                    )
                }

            }

            AuthStep.PROCESSING -> {
                AuthBoxTitle(modifier = textModifier, textRes = R.string.activating_auth)
                Spacer(modifier = Modifier.height(14.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = StrikeWhite,
                    strokeWidth = 4.dp,
                )
            }

            AuthStep.FINISHED, AuthStep.LEAVE_SCREEN -> {
                AuthBoxTitle(modifier = textModifier, textRes = R.string.you_are_set)
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(id = R.drawable.ic_android_fingerprint),
                    contentDescription = stringResource(id = R.string.content_des_finger_print)
                )
            }
        }
    }
}

@Composable
fun AuthBoxTitle(modifier: Modifier, @StringRes textRes: Int) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        text = stringResource(textRes),
        color = StrikeWhite,
        fontSize = 17.sp,
        lineHeight = 22.sp
    )
}

//can use this during flow at some point. Believe it will be shown before login is allowed.
fun canUseBiometric(context: Context) {
    val biometricManager = BiometricManager.from(context)
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> throw NotImplementedError()
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> throw NotImplementedError()
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> throw NotImplementedError()
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> throw NotImplementedError()
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> throw NotImplementedError()
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> throw NotImplementedError()
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> throw NotImplementedError()
    }
}