package com.censocustody.mobile.data.models.approval

import com.censocustody.mobile.common.BaseWrapper
import java.io.ByteArrayOutputStream

class SignDataHelper {
    companion object {
        fun serializeSignData(base64Data: String, commonBytes: ByteArray, opCode: Byte): ByteArray {
            val dataToSign = BaseWrapper.decodeFromBase64(base64Data).sha256HashBytes()
            val buffer = ByteArrayOutputStream()
            buffer.write(byteArrayOf(opCode))
            buffer.write(commonBytes)
            buffer.writeShortLE(dataToSign.size.toShort())
            buffer.write(dataToSign)
            return buffer.toByteArray()
        }
    }
}
