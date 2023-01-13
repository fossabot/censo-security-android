package com.censocustody.android.common

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.censocustody.android.data.CryptographyManager
import com.censocustody.android.data.models.LogoType
import com.censocustody.android.data.models.UserImage
import com.raygun.raygun4android.RaygunClient
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.security.Signature
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val MAX_QUALITY_JPEG = 100

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    try {
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener(
                { continuation.resume(future.get()) },
                cameraExecutor
            )
        }
    } catch (e: Exception) {
        RaygunClient.send(
            e, listOf(
                CrashReportingUtil.IMAGE,
                CrashReportingUtil.MANUALLY_REPORTED_TAG
            )
        )
        continuation.resumeWithException(e)
    }
}

val Context.cameraExecutor: Executor
    get() = ContextCompat.getMainExecutor(this)

suspend fun ImageCapture.takePhoto(executor: Executor): File {
    return suspendCoroutine { continuation ->
        try {
            val photoFile: File = File.createTempFile("image", "jpg")

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            takePicture(
                outputOptions, executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        continuation.resume(photoFile)
                    }

                    override fun onError(ex: ImageCaptureException) {
                        RaygunClient.send(
                            ex, listOf(
                                CrashReportingUtil.IMAGE,
                                CrashReportingUtil.MANUALLY_REPORTED_TAG
                            )
                        )
                        continuation.resumeWithException(ex)
                    }
                }
            )
        } catch (e: Exception) {
            RaygunClient.send(
                e, listOf(
                    CrashReportingUtil.IMAGE,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG
                )
            )
            continuation.resumeWithException(e)
        }
    }
}

fun Bitmap.convertToByteArrayWithJPEGCompression(): ByteArray {
    val stream = ByteArrayOutputStream()

    compress(Bitmap.CompressFormat.JPEG, MAX_QUALITY_JPEG, stream)

    //After we compress the bitmap, recycle it since it will not be used anymore
    recycle()

    return stream.toByteArray()
}

fun generateUserImageObject(
    userPhoto: Bitmap,
    keyName: String,
    signature: Signature,
    cryptographyManager: CryptographyManager
): UserImage {
    //Convert bitmap to byteArray
    val imageByteArray = userPhoto.convertToByteArrayWithJPEGCompression()

    //256 hash of image bytes
    val hash = hashOfUserImage(imageByteArray)

    //Encoded byteArray
    val encodedImageData = BaseWrapper.encodeToBase64(byteArray = imageByteArray)

    //Signed byteArray
    val signedImageData =
        cryptographyManager.signDataWithDeviceKey(
            data = hash,
            keyName = keyName,
            signature = signature
        )

    return UserImage(
        image = encodedImageData,
        type = LogoType.JPEG,
        signature = BaseWrapper.encodeToBase64(signedImageData)
    )
}

fun hashOfUserImage(byteArray: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(byteArray)
    return digest.digest()
}

enum class ImageCaptureError {
    NO_HARDWARE_CAMERA, BAD_RESULT, ACTION_CANCELLED
}