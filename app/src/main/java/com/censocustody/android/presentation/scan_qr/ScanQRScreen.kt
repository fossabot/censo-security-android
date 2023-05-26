package com.censocustody.android.presentation.scan_qr

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.censocustody.android.R
import com.censocustody.android.common.CensoButton
import com.censocustody.android.common.Resource
import com.censocustody.android.common.maskAddress
import com.censocustody.android.data.models.AvailableDAppVault
import com.censocustody.android.data.models.AvailableDAppWallet
import com.censocustody.android.presentation.approvals.NavIconTopBar
import com.censocustody.android.presentation.device_registration.Permission
import com.censocustody.android.presentation.device_registration.sendUserToPermissions
import com.censocustody.android.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalGetImage
@Composable
fun ScanQRScreen(
    navController: NavController,
    viewModel: ScanQRViewModel = hiltViewModel()
) {
    val state = viewModel.state

    //region Side Effects
    DisposableEffect(key1 = viewModel) {
        onDispose { viewModel.onStop() }
    }

    LaunchedEffect(key1 = state) {
        if (state.exitScreen) {
            viewModel.exitScreen()
            navController.popBackStack()
        }
    }
    //endregion

    //region UI
    Scaffold(
        topBar = {
            NavIconTopBar(
                title = stringResource(R.string.scan_qr_code_title),
                onAppBarIconClick = {
                    viewModel.exitScreen()
                    navController.popBackStack()
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

                    val scanQRResult = state.scanQRCodeResult
                    val uploadWcUriResult = state.uploadWcUri
                    val checkingConnections = state.checkSessionsOnConnection

                    if (state.availableDAppVaultsResult is Resource.Success) {
                        if (state.availableDAppVaultsResult.data?.vaults?.flatMap { it.wallets }.isNullOrEmpty()) {
                            Column(modifier = Modifier.align(Alignment.TopCenter)) {
                                Spacer(modifier = Modifier.height(44.dp))
                                Text(
                                    text = stringResource(R.string.no_dapp_enabled_wallets),
                                    fontSize = 28.sp,
                                    color = TextBlack,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(44.dp))
                                Text(
                                    text = stringResource(R.string.select_wallet_title),
                                    fontSize = 24.sp,
                                    color = TextBlack
                                )
                                Spacer(modifier = Modifier.height(44.dp))
                                state.availableDAppVaultsResult.data?.vaults?.forEach {
                                    WalletsInVault(vault = it, viewModel::userSelectedWallet)
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    } else if (checkingConnections is Resource.Success) {
                        ScanQRBoxUI {
                            Spacer(modifier = Modifier.height(36.dp))
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = stringResource(R.string.checked_connections_success),
                                textAlign = TextAlign.Center,
                                color = TextBlack,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.height(36.dp))
                            CensoButton(onClick = viewModel::userFinished) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = stringResource(id = R.string.done),
                                    fontSize = 20.sp,
                                    color = CensoWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(36.dp))
                        }
                    } else if (scanQRResult is Resource.Error || uploadWcUriResult is Resource.Error || checkingConnections is Resource.Error) {
                        ScanQRBoxUI {
                            Spacer(modifier = Modifier.height(36.dp))
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text =
                                if (scanQRResult is Resource.Error) {
                                    stringResource(R.string.failed_scan_qr_code)
                                } else if (checkingConnections is Resource.Error) {
                                    stringResource(R.string.no_active_sessions_found)
                                } else {
                                    stringResource(R.string.failed_upload_wc_uri)
                                },
                                textAlign = TextAlign.Center,
                                color = TextBlack,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.height(36.dp))
                            CensoButton(onClick = viewModel::retryScan) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = stringResource(id = R.string.try_again),
                                    fontSize = 20.sp,
                                    color = CensoWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(36.dp))
                        }
                    } else if (scanQRResult is Resource.Uninitialized) {
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
                            CensoButton(onClick = viewModel::retryScan) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = stringResource(R.string.scan_qr_code),
                                    fontSize = 20.sp,
                                    color = CensoWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(36.dp))
                        }
                    } else if (scanQRResult is Resource.Success || checkingConnections is Resource.Loading) {
                        ScanQRBoxUI {
                            Spacer(modifier = Modifier.height(36.dp))
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = if (checkingConnections is Resource.Loading) stringResource(R.string.checking_sessions_loading)
                                else stringResource(R.string.scan_qr_code_success),
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

                    if (state.scanQRCodeResult is Resource.Loading) {
                        ScanQRCodeComposable(
                            receivedUri = viewModel::receivedWalletConnectUri,
                            scanFailure = viewModel::failedToScan
                        )
                    }
                }
            }
        }
    )
    //endregion
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun ScanQRCodeComposable(
    receivedUri: (String?) -> Unit,
    scanFailure: (Exception) -> Unit
) {
    AndroidView(
        { context ->
            val cameraExecutor = Executors.newSingleThreadExecutor()
            val previewView = PreviewView(context).also {
                it.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider =
                    cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageCapture = ImageCapture.Builder().build()

                val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutor,
                            WalletConnectBarcodeAnalyzer(
                                foundQrCallback = receivedUri,
                                failedToScanCallback = scanFailure
                            )
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        context as ComponentActivity,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalyzer
                    )

                } catch (exc: Exception) {
                    scanFailure(exc)
                }
            }, ContextCompat.getMainExecutor(context))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun WalletsInVault(vault: AvailableDAppVault, onSelected: (AvailableDAppWallet) -> Unit) {
    VaultHeader(vault.vaultName)
    Divider(modifier = Modifier.height(1.0.dp), color = BorderGrey)

    Column() {
        vault.wallets.forEach {
            WalletInVault(it, onSelected)
            Divider(modifier = Modifier.height(1.0.dp), color = BorderGrey)
        }
    }
}

@Composable
fun VaultHeader(vaultName: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        textAlign = TextAlign.Start,
        text = vaultName,
        fontSize = 20.sp
    )
}

@Composable
fun WalletInVault(wallet: AvailableDAppWallet, onSelected: (AvailableDAppWallet) -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onSelected(wallet) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "${wallet.walletName} (${wallet.chains.first()}) - ${wallet.walletAddress.maskAddress()}"
        )
    }
}

@Composable
fun ScanQRBoxUI(
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(
                    elevation = 5.dp,
                )
                .align(Alignment.Center)
                .clip(RoundedCornerShape(4.dp))
                .background(color = CensoWhite),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}