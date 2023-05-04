package com.censocustody.android.presentation.org_key_recovery

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.CensoButton
import com.censocustody.android.common.Resource
import com.censocustody.android.presentation.approvals.NavIconTopBar
import com.censocustody.android.presentation.device_registration.Permission
import com.censocustody.android.presentation.device_registration.sendUserToPermissions
import com.censocustody.android.presentation.key_management.KeyManagementState
import com.censocustody.android.presentation.key_management.flows.KeyManagementFlowStep
import com.censocustody.android.presentation.key_management.flows.KeyRecoveryFlowUI
import com.censocustody.android.presentation.scan_qr.ScanQRBoxUI
import com.censocustody.android.presentation.scan_qr.ScanQRCodeComposable
import com.censocustody.android.ui.theme.ButtonRed
import com.censocustody.android.ui.theme.CensoWhite
import com.censocustody.android.ui.theme.TextBlack
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@Composable
fun OrgKeyRecoveryScreen(
    viewModel: OrgKeyRecoveryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.state

    LaunchedEffect(key1 = state) {
        if (state.showToast is Resource.Success) {
            val message = state.showToast.data ?: context.getString(R.string.phrase_entry_error)

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.resetShowToast()
        }
    }

    Box {
        when (state.recoveryStep) {
            RecoveryStep.RECOVERY_START -> {
                RecoveryEntryUI(
                    onPhraseEntryClick = viewModel::setPhraseEntry,
                    onScanQRClick = viewModel::setScanQRCode
                )
            }
            RecoveryStep.PHRASE_ENTRY -> {
                KeyRecoveryFlowUI(
                    keyRecoveryFlowStep = (state.keyRecoveryFlowStep as KeyManagementFlowStep.RecoveryFlow).step,
                    pastedPhrase = state.confirmPhraseWordsState.pastedPhrase,
                    onNavigate = viewModel::keyRecoveryNavigateForward,
                    onBackNavigate = viewModel::keyRecoveryNavigateBackward,
                    onPhraseFlowAction = viewModel::phraseFlowAction,
                    onPhraseEntryAction = viewModel::phraseEntryAction,
                    onExit = viewModel::exitPhraseEntryStep,
                    wordToVerifyIndex = state.confirmPhraseWordsState.phraseWordToVerifyIndex,
                    wordInput = state.confirmPhraseWordsState.wordInput,
                    wordVerificationErrorEnabled = state.confirmPhraseWordsState.errorEnabled,
                    retryKeyRecovery = viewModel::retryKeyRecoveryFromPhrase,
                    keyRecoveryState = state.finalizeKeyFlow
                )
            }
            RecoveryStep.SCAN_QR -> {
                ScanQRCodeUI(
                    onAppBarIconClick = {
                        viewModel.exitScanQRStep()
                    },
                    scanQRCodeResult = state.scanQRCodeResult,
                    onRetryScan = viewModel::retryScan,
                    onReceivedUri = viewModel::receivedQRCodeData,
                    onFailedScan = viewModel::failedToScan
                )
            }
            RecoveryStep.DISPLAY_QR -> {
                QRCodeForScanning(
                    qrCode = state.qrCodeBitmap,
                    onRestart = viewModel::restartRecoveryProcess
                )
            }
            RecoveryStep.RECOVERY_ERROR -> {
                RecoveryErrorUI(onRestart = viewModel::restartRecoveryProcess)
            }
        }
    }
}

@Composable
fun QRCodeForScanning(
    qrCode: Bitmap?,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (qrCode != null) {
            Text(
                text = stringResource(id = R.string.scan_qr_code_via_main_app),
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(36.dp))
            Image(
                bitmap = qrCode.asImageBitmap(),
                contentDescription = "QRCode"
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(id = R.string.once_the_qr_code_has_been_scanned),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = stringResource(id = R.string.recover_more_keys),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRestart) {
                Text(text = stringResource(id = R.string.restart_recovery))
            }
        } else {
            RecoveryErrorUI(onRestart = onRestart)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanQRCodeUI(
    onAppBarIconClick: () -> Unit,
    scanQRCodeResult: Resource<String>,
    onRetryScan: () -> Unit,
    onReceivedUri: (String?) -> Unit,
    onFailedScan: (Exception) -> Unit
) {
    //region UI
    Scaffold(
        topBar = {
            NavIconTopBar(
                title = stringResource(R.string.scan_qr_code_title),
                onAppBarIconClick = {
                    onAppBarIconClick()
                },
                navigationIcon = Icons.Rounded.ArrowBack,
                navigationIconContentDes = stringResource(R.string.exit_scan_qr_screen)
            )
        },
        content = { _ ->

            val context = LocalContext.current

            Permission(
                permission = Manifest.permission.CAMERA,
                rationale = stringResource(R.string.explain_photo_permission_scan_qr),
                permissionNotAvailableContent = {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            stringResource(R.string.missing_camera_permission),
                            color = TextBlack,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        CensoButton(
                            onClick = { context.sendUserToPermissions() },
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.open_settings),
                                color = CensoWhite,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    when (scanQRCodeResult) {
                        is Resource.Error -> {
                            ScanQRBoxUI {
                                Spacer(modifier = Modifier.height(36.dp))
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = stringResource(R.string.failed_scan_qr_code),
                                    textAlign = TextAlign.Center,
                                    color = TextBlack,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.height(36.dp))
                                CensoButton(onClick = onRetryScan) {
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = stringResource(id = R.string.try_again),
                                        fontSize = 20.sp,
                                        color = CensoWhite
                                    )
                                }
                                Spacer(modifier = Modifier.height(36.dp))
                            }
                        }
                        is Resource.Uninitialized -> {
                            ScanQRBoxUI {
                                Spacer(modifier = Modifier.height(36.dp))
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = stringResource(R.string.start_scanning),
                                    textAlign = TextAlign.Center,
                                    color = TextBlack,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.height(36.dp))
                                CensoButton(onClick = { onRetryScan() }) {
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = stringResource(R.string.scan_qr_code),
                                        fontSize = 20.sp,
                                        color = CensoWhite
                                    )
                                }
                                Spacer(modifier = Modifier.height(36.dp))
                            }
                        }
                        is Resource.Success -> {
                            ScanQRBoxUI {
                                Spacer(modifier = Modifier.height(36.dp))
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = stringResource(R.string.scan_qr_code_success),
                                    textAlign = TextAlign.Center,
                                    color = TextBlack,
                                    fontSize = 22.sp
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
                        is Resource.Loading -> {
                            ScanQRCodeComposable(
                                receivedUri = onReceivedUri,
                                scanFailure = onFailedScan
                            )
                        }
                    }
                }
            }
        }
    )
    //endregion
}

@Composable
fun RecoveryEntryUI(
    onPhraseEntryClick: () -> Unit,
    onScanQRClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.censo_icon_color), contentDescription = "Censo Logo")
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(id = R.string.welcome_to_censo_recovery),
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = { onPhraseEntryClick() }) {
            Text(text = stringResource(id = R.string.phrase_entry))
        }
        Spacer(modifier = Modifier.height(36.dp))
        Button(onClick = { onScanQRClick() }) {
            Text(text = stringResource(id = R.string.scan_qr_code_for_recovery))
        }
    }
}

@Composable
fun RecoveryErrorUI(
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.something_went_wrong_please_restart_recovery),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRestart) {
            Text(text = stringResource(id = R.string.restart_recovery))
        }
    }
}