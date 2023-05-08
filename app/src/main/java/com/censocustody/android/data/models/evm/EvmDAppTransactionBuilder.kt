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
}