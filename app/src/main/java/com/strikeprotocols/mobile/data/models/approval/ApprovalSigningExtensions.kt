package com.strikeprotocols.mobile.data.models.approval

import com.strikeprotocols.mobile.common.BaseWrapper
import org.web3j.crypto.Hash
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun String.base58Bytes() = BaseWrapper.decode(this)

fun String.sha256HashBytes() : ByteArray = Hash.sha256(toByteArray())

fun Long.convertToSeconds() : Long = this / 1000

fun Int.bytes() : ByteArray {
    val bytes = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
    val buffer = ByteBuffer.wrap(bytes)
    buffer.order(ByteOrder.LITTLE_ENDIAN)

    return buffer.array()
}

fun Long.bytes() : ByteArray {
    val bytes = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(this).array()
    val buffer = ByteBuffer.wrap(bytes)
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    return buffer.array()
}

fun SlotSignerInfo.combinedBytes(): ByteArray {
    return byteArrayOf(this.slotId) + this.value.publicKey.base58Bytes()
}

fun List<AccountMeta>.indexOfKey(publicKey: PublicKey) : Int {
    return indexOfFirst { it.publicKey == publicKey }
}