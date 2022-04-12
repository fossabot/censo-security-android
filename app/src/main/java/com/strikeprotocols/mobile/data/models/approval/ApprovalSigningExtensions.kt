package com.strikeprotocols.mobile.data.models.approval

import com.strikeprotocols.mobile.common.BaseWrapper
import org.web3j.crypto.Hash
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun String.base58Bytes() = BaseWrapper.decode(this)

fun String.sha256HashBytes() : ByteArray = Hash.sha256(toByteArray())

fun Long.convertToSeconds() : Long = this / 1000

fun ByteArrayOutputStream.writeShortLE(value: Short) {
    this.write(
        ByteBuffer.allocate(2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(value)
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
    return byteArrayOf(this.slotId) + this.value.publicKey.base58Bytes()
}

fun List<AccountMeta>.indexOfKey(publicKey: PublicKey) : Int {
    return indexOfFirst { it.publicKey == publicKey }
}