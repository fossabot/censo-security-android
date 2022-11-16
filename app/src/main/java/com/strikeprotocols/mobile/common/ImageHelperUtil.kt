package com.strikeprotocols.mobile.common

import android.graphics.Bitmap
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.models.ImageType
import com.strikeprotocols.mobile.data.models.LogoType
import com.strikeprotocols.mobile.data.models.UserImage
import java.io.ByteArrayOutputStream
import javax.crypto.Cipher

const val MAX_QUALITY_JPEG = 100

suspend fun Bitmap.convertToByteArrayWithJPEGCompression(): ByteArray {
    val stream = ByteArrayOutputStream()

    compress(Bitmap.CompressFormat.JPEG, MAX_QUALITY_JPEG, stream)

    //After we compress the bitmap, recycle it since it will not be used anymore
    recycle()

    return stream.toByteArray()
}

suspend fun generateUserImageObject(
    userPhoto: Bitmap,
    cipher: Cipher,
    keyRepository: KeyRepository
): UserImage {
    //Convert bitmap to byteArray
    val imageByteArray = userPhoto.convertToByteArrayWithJPEGCompression()

    //Encoded byteArray
    val encodedImageData = BaseWrapper.encodeToBase64(byteArray = imageByteArray)

    //Signed byteArray
    val signedImageData =
        keyRepository.signImageData(imageByteArray = imageByteArray, cipher = cipher)

    return UserImage(
        image = encodedImageData,
        type = LogoType(ImageType.JPEG),
        signature = signedImageData
    )
}

enum class ImageCaptureError {
    NO_HARDWARE_CAMERA, BAD_RESULT, ACTION_CANCELLED
}