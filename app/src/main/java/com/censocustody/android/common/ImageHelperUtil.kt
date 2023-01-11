package com.censocustody.android.common

import android.graphics.Bitmap
import com.censocustody.android.data.CryptographyManager
import com.censocustody.android.data.models.LogoType
import com.censocustody.android.data.models.UserImage
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.Signature

const val MAX_QUALITY_JPEG = 100

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
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(imageByteArray)
    val hash = digest.digest()

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

enum class ImageCaptureError {
    NO_HARDWARE_CAMERA, BAD_RESULT, ACTION_CANCELLED
}