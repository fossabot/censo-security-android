package com.censocustody.android.data.models.evm

import com.censocustody.android.common.evm.*
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.nio.ByteBuffer

object EvmDAppTransactionBuilder {
    fun sendTransactionSafeHash(
        walletAddress: EvmAddress,
        transaction: ApprovalRequestDetailsV2.EvmTransaction,
        ethereumTransaction: ApprovalRequestDetailsV2.SigningData.EthereumTransaction
    ): ByteArray {
        return EvmTransactionUtil.computeSafeTransactionHash(
            ethereumTransaction.chainId,
            walletAddress,
            transaction.to,
            transaction.value.removePrefix("0x").toBigInteger(16),
            Hex.decode(transaction.data.removePrefix("0x")),
            Operation.CALL,
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ZERO,
            "0x0000000000000000000000000000000000000000",
            "0x0000000000000000000000000000000000000000",
            ethereumTransaction.safeNonce.toBigInteger(),
        )
    }

    private fun signMessageData(signParams: ApprovalRequestDetailsV2.DAppParams.EthSign) = signMessageHashData(Hex.decode(signParams.messageHash.removePrefix("0x")))
    private fun signMessageData(signParams: ApprovalRequestDetailsV2.DAppParams.EthSignTypedData) = signMessageHashData(signParams.structuredData().hashStructuredData())
    private fun signMessageHashData(messageHash: ByteArray): ByteArray {
        // signMessage(bytes), but bytes is always a 32-byte message hash
        val data = ByteBuffer.allocate(4 + 32*3)
        data.put(Hex.decode("85a5affe"))
        // this is the offset where bytes starts
        data.putPadded(BigInteger("32").toByteArray())
        // followed by 32 bytes with the length of the data
        data.putPadded(BigInteger("32").toByteArray())
        // followed by the data
        data.put(messageHash)
        return data.array()
    }

    private fun signSafeHash(
        walletAddress: String,
        signMessageData: ByteArray,
        transaction: ApprovalRequestDetailsV2.SigningData.EthereumTransaction
    ): ByteArray {
        return EvmTransactionUtil.computeSafeTransactionHash(
            transaction.chainId,
            walletAddress,
            GnosisSafeConstants.signMessageLibAddress,
            BigInteger.ZERO,
            signMessageData,
            Operation.DELEGATECALL,
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ZERO,
            "0x0000000000000000000000000000000000000000",
            "0x0000000000000000000000000000000000000000",
            transaction.safeNonce.toBigInteger()
        )
    }

    fun signSafeHash(
        walletAddress: String,
        signParams: ApprovalRequestDetailsV2.DAppParams.EthSign,
        transaction: ApprovalRequestDetailsV2.SigningData.EthereumTransaction
    ) = signSafeHash(walletAddress, signMessageData(signParams), transaction)

    fun signSafeTypedDataHash(
        walletAddress: String,
        signParams: ApprovalRequestDetailsV2.DAppParams.EthSignTypedData,
        transaction: ApprovalRequestDetailsV2.SigningData.EthereumTransaction
    ) = signSafeHash(walletAddress, signMessageData(signParams), transaction)
}