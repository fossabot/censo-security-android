@file:OptIn(ExperimentalPermissionsApi::class)

package com.censocustody.android.presentation.device_registration

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Canvas
import com.censocustody.android.R
import com.censocustody.android.common.*
import com.censocustody.android.ui.theme.CensoButtonBlue
import com.censocustody.android.ui.theme.CensoWhite
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.raygun.raygun4android.RaygunClient
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CaptureUserImageContent(
    modifier: Modifier = Modifier,
    onImageCaptureSuccess: (Bitmap) -> Unit,
    onImageCaptureError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp

    var imageFile: File? by remember { mutableStateOf(null) }
    if (imageFile != null) {

        var userImageBitmap: Bitmap?
        try {
            val imageUrl = imageFile?.absolutePath
            userImageBitmap = BitmapFactory.decodeFile(imageUrl)
            if (userImageBitmap != null) {
                userImageBitmap = rotateImageIfRequired(context, userImageBitmap, imageFile)
                userImageBitmap = squareCropImage(userImageBitmap)
            }
        } catch (e: Exception) {
            RaygunClient.send(e,
                listOf(
                    CrashReportingUtil.MANUALLY_REPORTED_TAG,
                    CrashReportingUtil.IMAGE
                )
            )
            userImageBitmap = null
        }

        Box(modifier = modifier) {
            if(userImageBitmap != null) {
                Image(
                    modifier = Modifier
                        .size(DpSize(width = screenWidth, height = screenWidth))
                        .align(Alignment.Center),
                    bitmap = userImageBitmap.asImageBitmap(),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = stringResource(R.string.capture_image_content_desc)
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                CensoButton(
                    onClick = {
                        if(userImageBitmap != null) {
                            onImageCaptureSuccess(userImageBitmap)
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.unable_process_image),
                                Toast.LENGTH_LONG
                            ).show()
                        }
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
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp

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
                    modifier = Modifier
                        .size(DpSize(width = screenWidth, height = screenWidth))
                        .align(Alignment.Center),
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
                    border = BorderStroke(width = 1.5.dp, color = CensoWhite),
                    colors = ButtonDefaults.outlinedButtonColors(backgroundColor = CensoButtonBlue)
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

    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val screenHeightPixels = screenHeight.dpToPx()
    val screenWidthPixels = screenWidth.dpToPx()

    val cutoffSize = (screenHeight - screenWidth) / 2

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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(
            modifier =
            Modifier
                .height(cutoffSize)
                .fillMaxWidth()
                .background(color = Color.Black)
        )

        Spacer(
            modifier =
            Modifier
                .height(cutoffSize)
                .fillMaxWidth()
                .background(color = Color.Black)
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = .99f }
    ) {
        // Destination
        drawRect(Color.Black.copy(alpha = 0.5f))

        val ovalWidth = screenWidthPixels * 0.75f
        val ovalHeight = screenHeightPixels * 0.45f

        val topLeftX = (screenWidthPixels - ovalWidth) / 2
        val topLeftY = (screenHeightPixels - ovalHeight) / 2

        drawOval(
            topLeft = Offset(x = topLeftX, y = topLeftY),
            size = Size(height = ovalHeight, width = ovalWidth),
            color = Color.Transparent,
            blendMode = BlendMode.Clear
        )
    }

}