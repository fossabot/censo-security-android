package com.censocustody.android.common.evm

import com.censocustody.android.common.pad
import com.censocustody.android.common.toHexString
import org.bouncycastle.util.encoders.Hex
import org.web3j.crypto.StructuredDataEncoder
import java.math.BigInteger
import java.nio.ByteBuffer

fun ByteBuffer.putPadded(data: ByteArray, padTo: Int = 32) {
    require(data.size <= padTo)
    this.put(ByteArray(padTo - data.size))
    this.put(data)
}

enum class Operation {
    CALL,
    DELEGATECALL;

    fun toByteArray() = this.ordinal.toBigInteger().toByteArray()
}

object EvmTransactionUtil {

    private fun padHex(hex: String) = if (hex.length % 2 == 1) "0$hex" else hex

    fun normalizeAddress(address: String) = Hex.decode(
        padHex(address.removePrefix("0x").trimStart('0'))
    ).pad(20)

    fun computeSafeTransactionHash(
        chainId: Long,
        safeAddress: String,
        to: String,
        value: BigInteger,
        data: ByteArray,
        operation: Operation = Operation.CALL,
        safeTxGas: BigInteger = BigInteger.ZERO,
        baseGas: BigInteger = BigInteger.ZERO,
        gasPrice: BigInteger = BigInteger.ZERO,
        gasToken: String = "0x0",
        refundReceiver: String = "0x0",
        nonce: BigInteger
    ): ByteArray {
        val encoder = StructuredDataEncoder(
            """{
                "types": {
                    "EIP712Domain": [
                        { "type": "uint256", "name": "chainId" },
                        { "type": "address", "name": "verifyingContract" }
                    ],
                    "SafeTx": [
                        { "type": "address", "name": "to" },
                        { "type": "uint256", "name": "value" },
                        { "type": "bytes", "name": "data" },
                        { "type": "uint8", "name": "operation" },
                        { "type": "uint256", "name": "safeTxGas" },
                        { "type": "uint256", "name": "baseGas" },
                        { "type": "uint256", "name": "gasPrice" },
                        { "type": "address", "name": "gasToken" },
                        { "type": "address", "name": "refundReceiver" },
                        { "type": "uint256", "name": "nonce" }
                    ]
                },
                "primaryType": "SafeTx",
                "domain": {
                    "chainId": $chainId,
                    "verifyingContract": "$safeAddress"
                },
                "message": {
                    "to": "$to",
                    "value": $value,
                    "data": "${data.toHexString()}",
                    "operation": ${operation.ordinal},
                    "safeTxGas": $safeTxGas,
                    "baseGas": $baseGas,
                    "gasPrice": $gasPrice,
                    "gasToken": "$gasToken",
                    "refundReceiver": "$refundReceiver",
                    "nonce": $nonce
                }
            }"""
        )
        return encoder.hashStructuredData()
    }
}