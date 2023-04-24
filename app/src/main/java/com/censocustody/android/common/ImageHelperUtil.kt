package com.censocustody.android.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.censocustody.android.data.cryptography.CryptographyManager
import com.censocustody.android.data.models.LogoType
import com.censocustody.android.data.models.UserImage
import com.raygun.raygun4android.RaygunClient
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


const val MAX_QUALITY_JPEG = 100
const val MAX_IMAGE_SIZE_BYTES = 8_388_608 //8MB

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

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

/**
 * Rotate an image if required.
 *
 * @param img           The image bitmap
 * @param selectedImage Image URI
 * @return The resulted Bitmap after manipulation
 */
fun rotateImageIfRequired(context: Context, image: Bitmap, imageFile: File?): Bitmap {

    var inputStream: InputStream? = null

    try {

        if (imageFile == null) return image

        val selectedImageUri = Uri.fromFile(imageFile)

        inputStream = context.contentResolver.openInputStream(selectedImageUri) ?: return image
        val exifInterface = ExifInterface(inputStream)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        inputStream.close()

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(image, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(image, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(image, 270f)
            else -> image
        }
    } catch (e: Exception) {
        inputStream?.close()
        return image
    }
}

fun squareCropImage(image: Bitmap): Bitmap {
    val imageWidth = image.width
    val imageHeight = image.height

    val startY = (imageHeight - imageWidth) / 2

    return Bitmap.createBitmap(
        image,
        0,
        startY,
        imageWidth,
        imageWidth
    )
}

private fun rotateImage(image: Bitmap, degree: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree)
    val rotatedImage = Bitmap.createBitmap(
        image, 0, 0, image.width, image.height, matrix, true
    )
    image.recycle()
    return rotatedImage
}

private fun Bitmap.compressImageToMaxSize(maxSizeBytes: Int): ByteArray {
    val stream = ByteArrayOutputStream()
    var currSize: Int
    var currQuality = MAX_QUALITY_JPEG

    do {
        stream.reset()
        compress(Bitmap.CompressFormat.JPEG, currQuality, stream)
        currSize = stream.toByteArray().size
        // limit quality by 5 percent every time
        currQuality -= 5
    } while (currSize >= maxSizeBytes && currQuality > 5)

    recycle()

    val reducedImage = stream.toByteArray()
    stream.reset()

    return reducedImage
}

fun generateUserImageObject(
    userPhoto: Bitmap,
    keyName: String,
    cryptographyManager: CryptographyManager
): UserImage {
    //Convert bitmap to byteArray
    val imageByteArray = userPhoto.compressImageToMaxSize(MAX_IMAGE_SIZE_BYTES)

    //256 hash of image bytes
    val hash = hashOfUserImage(imageByteArray)

    //Encoded byteArray
    val encodedImageData = BaseWrapper.encodeToBase64(byteArray = imageByteArray)

    //Signed byteArray
    val signedImageData =
        cryptographyManager.signData(
            dataToSign = hash,
            keyName = keyName,
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