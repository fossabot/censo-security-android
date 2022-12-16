package com.censocustody.android.data.models.approval

import com.censocustody.android.common.BaseWrapper
import org.web3j.crypto.Hash
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun String.base58Bytes() = BaseWrapper.decode(this)

fun String.sha256HashBytes() : ByteArray = Hash.sha256(toByteArray())

fun ByteArray.sha256HashBytes() : ByteArray = Hash.sha256(this)

fun Long.convertToSeconds() : Long = this / 1000

fun ByteArrayOutputStream.writeShortLE(value: Short) {
    this.write(
        ByteBuffer.allocate(2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(value)
            .array()
    )
}

fun ByteArrayOutputStream.writeIntLE(value: Int) {
    this.write(
        ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(value)
            .array()
    )
}

fun ByteArrayOutputStream.writeLongLE(value: Long) {
    this.write(
        ByteBuffer.allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putLong(value)
            .array()
    )
}

fun SlotSignerInfo.combinedBytes(): ByteArray {
    return byteArrayOf(this.slotId).plus(this.value.publicKey.base58Bytes()).plus(
        if (this.value.nameHashIsEmpty) ByteArray(32) else this.value.email.sha256HashBytes()
    )
}

fun SlotSignerInfo.opHashBytes(): ByteArray {
    return byteArrayOf(this.slotId).plus(this.value.publicKey.base58Bytes())
}

fun List<AccountMeta>.indexOfKey(publicKey: PublicKey): Int {
    val index = indexOfFirst { it.publicKey.bytes.contentEquals(publicKey.bytes) }

    if (index == -1) {
        throw Exception("Could not find accountIndex")
    }

    return index
}