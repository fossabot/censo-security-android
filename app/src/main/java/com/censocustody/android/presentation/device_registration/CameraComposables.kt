@file:OptIn(ExperimentalCoilApi::class, ExperimentalPermissionsApi::class)

package com.censocustody.android.presentation.device_registration

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.censocustody.android.R
import com.censocustody.android.common.*
import com.censocustody.android.ui.theme.CensoButtonBlue
import com.censocustody.android.ui.theme.CensoWhite
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CaptureUserImageContent(
    modifier: Modifier = Modifier,
    onImageCaptureSuccess: (Bitmap) -> Unit,
    onImageCaptureError: (Exception) -> Unit
) {
    var imageFile: File? by remember { mutableStateOf(null) }
    if (imageFile != null) {
        Box(modifier = modifier) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberImagePainter(imageFile),
                contentDescription = stringResource(R.string.capture_image_content_desc)
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                CensoButton(
                    onClick = {
                        val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath)
                        onImageCaptureSuccess(bitmap)
                    },
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(stringResource(R.string.use_photo), color = CensoWhite, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
                CensoButton(
                    onClick = {
                        imageFile = null
                    },
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(
                        stringResource(R.string.retake_device_photo),
                        color = CensoWhite,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } else {
        CameraCapture(
            modifier = modifier,
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
            onImageFile = { file ->
                imageFile = file
            },
            onError = {
                onImageCaptureError(it)
            }
        )
    }
}

@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector,
    onImageFile: (File) -> Unit,
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    Permission(
        permission = Manifest.permission.CAMERA,
        rationale = stringResource(R.string.explain_photo_permission),
        permissionNotAvailableContent = {
            Column(
                modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.missing_camera_permission),
                    color = CensoWhite,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                CensoButton(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    },
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
        Box(modifier = modifier) {
            val lifecycleOwner = LocalLifecycleOwner.current
            val coroutineScope = rememberCoroutineScope()
            var previewUseCase by remember { mutableStateOf<UseCase>(Preview.Builder().build()) }
            val imageCaptureUseCase by remember {
                mutableStateOf(
                    ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()
                )
            }
            Box {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onUseCase = {
                        previewUseCase = it
                    }
                )
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                onImageFile(imageCaptureUseCase.takePhoto(context.cameraExecutor))
                            } catch (e: Exception) {
                                onError(e)
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(paddingValues = PaddingValues(36.dp))
                        .size(100.dp)
                        .align(Alignment.BottomCenter),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, CensoWhite),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CensoButtonBlue)
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = stringResource(R.string.take_photo_content_desc),
                        tint = CensoWhite
                    )
                }
            }
            LaunchedEffect(previewUseCase) {
                val cameraProvider = context.getCameraProvider()
                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, previewUseCase, imageCaptureUseCase
                    )
                } catch (ex: Exception) {
                    onError(ex)
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    onUseCase: (UseCase) -> Unit = { }
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            onUseCase(
                Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
            )
            previewView
        }
    )
}