package com.censocustody.android.presentation.key_creation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.presentation.Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.censocustody.android.presentation.key_management.BackgroundUI
import com.censocustody.android.presentation.key_management.SmallAuthFlowButton
import com.censocustody.android.R
import com.censocustody.android.common.*
import com.censocustody.android.presentation.key_management.PreBiometryDialog
import com.censocustody.android.ui.theme.*
import com.raygun.raygun4android.RaygunClient
import java.io.File

@Composable
fun KeyCreationScreen(
    navController: NavController,
    initialData: KeyCreationInitialData,
    viewModel: KeyCreationViewModel = hiltViewModel(),
) {

    val state = viewModel.state
    val context = LocalContext.current as FragmentActivity

    fun cropAndRotateImage(imageUrl: String): Bitmap? {
        var userImageBitmap: Bitmap?
        return try {
            userImageBitmap = BitmapFactory.decodeFile(imageUrl)
            if (userImageBitmap != null) {
                userImageBitmap = rotateImageIfRequired(context, userImageBitmap, File(imageUrl))
                squareCropImage(userImageBitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            RaygunClient.send(
                e,
                listOf(
                    CrashReportingUtil.MANUALLY_REPORTED_TAG,
                    CrashReportingUtil.IMAGE
                )
            )
            null
        }
    }

    DisposableEffect(key1 = viewModel) {

        val bitmap = if (initialData.bootstrapUserDeviceImageURI.isNotEmpty()) {
            cropAndRotateImage(initialData.bootstrapUserDeviceImageURI)
        } else {
            null
        }

        viewModel.onStart(initialData.verifyUserDetails, bootstrapUserDeviceImage = bitmap)
        onDispose {
            viewModel.cleanUp()
        }
    }

    LaunchedEffect(key1 = state) {
        if (state.uploadingKeyProcess is Resource.Success) {
            navController.navigate(Screen.ApprovalListRoute.route) {
                launchSingleTop = true
                popUpToTop()
            }
            viewModel.resetAddWalletSignerCall()
        }
    }

    BackgroundUI()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp)
    ) {
        if (state.uploadingKeyProcess is Resource.Error) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.something_went_wrong),
                    color = TextBlack,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.23.sp,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                SmallAuthFlowButton(
                    modifier = Modifier.wrapContentWidth(),
                    text = stringResource(R.string.retry),
                ) {
                    viewModel.retryKeyCreation()
                }
            }
        } else {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .shadow(
                            elevation = 5.dp,
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(color = BackgroundGrey),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.saving_key_to_device),
                        textAlign = TextAlign.Center,
                        color = TextBlack,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ButtonRed,
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }

    if (state.triggerBioPrompt is Resource.Success) {
        val kickOffBioPrompt = {
            val promptInfo = BioCryptoUtil.createPromptInfo(context = context)
            val bioPrompt = BioCryptoUtil.createBioPrompt(
                fragmentActivity = context,
                onSuccess = { viewModel.biometryApproved() },
                onFail = {
                    BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                        viewModel.biometryFailed()
                    }
                }
            )

            bioPrompt.authenticate(promptInfo)

            viewModel.resetPromptTrigger()
        }

        PreBiometryDialog(
            mainText = stringResource(id = R.string.save_biometry_info_key_creation),
            onAccept = kickOffBioPrompt
        )
    }
}