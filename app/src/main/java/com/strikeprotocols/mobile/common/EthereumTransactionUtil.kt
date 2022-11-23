package com.strikeprotocols.mobile.common

import org.web3j.crypto.StructuredDataEncoder
import java.math.BigInteger

enum class Operation {
    CALL,
    DELEGATECALL,
}

object EthereumTransactionUtil {
    fun computeSafeTransactionHash(
        chainId: Long,
        safeAddress: String,
        to: String,
        value: BigInteger,
        data: ByteArray,
        operation: Operation,
        safeTxGas: BigInteger,
        baseGas: BigInteger,
        gasPrice: BigInteger,
        gasToken: String,
        refundReceiver: String,
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