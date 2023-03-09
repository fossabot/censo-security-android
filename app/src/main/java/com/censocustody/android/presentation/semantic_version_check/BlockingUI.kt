package com.censocustody.android.presentation.semantic_version_check

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.censocustody.android.common.BiometricUtil
import com.censocustody.android.common.Resource
import com.censocustody.android.presentation.biometry_disabled.BiometryDisabledScreen
import com.censocustody.android.ui.theme.CensoWhite
import javax.crypto.Cipher
import com.censocustody.android.common.CensoButton
import com.censocustody.android.R
import com.censocustody.android.ui.theme.BackgroundGrey
import com.censocustody.android.ui.theme.TextBlack

@Composable
fun BlockingUI(
    blockAppUI: BlockAppUI,
    bioPromptTrigger: Resource<Unit>,
    biometryUnavailable: Boolean,
    biometryStatus: BiometricUtil.Companion.BiometricsStatus?,
    retry: () -> Unit
) {
    when (blockAppUI) {
        BlockAppUI.BIOMETRY_DISABLED -> {
            DisabledBiometryUI(
                biometryStatus
            )
        }
        BlockAppUI.FOREGROUND_BIOMETRY -> {
            ForegroundBlockingUI(
                bioPromptTrigger = bioPromptTrigger,
                biometryUnavailable = biometryUnavailable,
                retry = retry
            )
        }
        BlockAppUI.NONE -> {
            Spacer(modifier = Modifier.height(0.dp))
        }
    }
}

@Composable
fun ForegroundBlockingUI(
    bioPromptTrigger: Resource<Unit>,
    biometryUnavailable: Boolean,
    retry: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(color = BackgroundGrey)
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) { },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 124.dp, start = 48.dp, end = 48.dp),
                text = if (biometryUnavailable)
                    stringResource(R.string.biometry_unavailable)
                else stringResource(R.string.foreground_access_app),
                fontSize = 24.sp,
                color = TextBlack,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (bioPromptTrigger is Resource.Error) {
                CensoButton(
                    contentPadding = PaddingValues(horizontal = 36.dp, vertical = 10.dp), onClick = retry) {
                    Text(
                        text = stringResource(R.string.try_again),
                        color = CensoWhite,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DisabledBiometryUI(biometryStatus: BiometricUtil.Companion.BiometricsStatus?) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(color = BackgroundGrey)
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) { },
    ) {
        val message: Int =
            if (biometryStatus == BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_DISABLED) {
                R.string.biometry_disabled_message
            } else {
                R.string.biometry_unavailable_message
            }

        BiometryDisabledScreen(
            message = stringResource(id = message),
            biometryAvailable = biometryStatus != BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_NOT_AVAILABLE
        )
    }
}