package com.censocustody.android.data.models.evm

import com.censocustody.android.common.evm.*
import com.censocustody.android.data.models.approval.EthTokenType
import com.censocustody.android.data.models.approval.SigningData
import com.censocustody.android.data.models.approval.SymbolAndAmountInfo
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.nio.ByteBuffer

object EvmTransferTransactionBuilder {
    fun erc20WithdrawalTx(toAddress: EvmAddress, amount: BigInteger): ByteArray {
        val data = ByteBuffer.allocate(4 + 32*2)
        data.put(Hex.decode("a9059cbb"))
        data.putPadded(Hex.decode(toAddress.clean()))
        data.putPadded(amount.toByteArray())
        return data.array()
    }

    fun erc721WithdrawalTx(fromAddress: EvmAddress, toAddress: EvmAddress, tokenId: BigInteger): ByteArray {
        // safeTransferFrom(address,address,uint256)
        val data = ByteBuffer.allocate(4 + 32*3)
        data.put(Hex.decode("42842e0e"))
        data.putPadded(Hex.decode(fromAddress.clean()))
        data.putPadded(Hex.decode(toAddress.clean()))
        data.putPadded(tokenId.toByteArray())
        return data.array()
    }

    fun erc1155WithdrawalTx(fromAddress: EvmAddress, toAddress: EvmAddress, tokenId: BigInteger, amount: BigInteger): ByteArray {
        // safeTransferFrom(address,address,uint256,uint256,bytes)
        val data = ByteBuffer.allocate(4 + 32*6)
        data.put(Hex.decode("f242432a"))
        data.putPadded(Hex.decode(fromAddress.clean()))
        data.putPadded(Hex.decode(toAddress.clean()))
        data.putPadded(tokenId.toByteArray())
        data.putPadded(amount.toByteArray())
        // this the 5th bytes param which is dynamic data - the 160 is the offset where dynamic data starts
        // followed by 32 bytes of 0 for the length of the dynamic data
        data.putPadded(BigInteger("160").toByteArray())
        data.putPadded(ByteArray(0))
        return data.array()
    }

    fun withdrawalSafeHash(symbolAndAmountInfo: SymbolAndAmountInfo, walletAddress: EvmAddress, destinationAddress: EvmAddress, ethereumTransaction: SigningData.EthereumTransaction): ByteArray {
        return symbolAndAmountInfo.symbolInfo.tokenMintAddress?.let { contractAddress ->
            EvmTransactionUtil.computeSafeTransactionHash(
                ethereumTransaction.chainId,
                walletAddress,
                contractAddress,
                BigInteger.ZERO,
                when (symbolAndAmountInfo.symbolInfo.ethTokenInfo?.tokenType) {
                    EthTokenType.ERC721 -> EvmTransferTransactionBuilder.erc721WithdrawalTx(
                        walletAddress,
                        destinationAddress,
                        BigInteger(symbolAndAmountInfo.symbolInfo.ethTokenInfo.tokenId!!)
                    )
                    EthTokenType.ERC1155 -> EvmTransferTransactionBuilder.erc1155WithdrawalTx(
                        walletAddress,
                        destinationAddress,
                        BigInteger(symbolAndAmountInfo.symbolInfo.ethTokenInfo.tokenId!!),
                        symbolAndAmountInfo.fundamentalAmountAsBigInteger()
                    )
                    else -> EvmTransferTransactionBuilder.erc20WithdrawalTx(
                        destinationAddress,
                        symbolAndAmountInfo.fundamentalAmountAsBigInteger()
                    )
                },
                Operation.CALL,
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                "0x0000000000000000000000000000000000000000",
                "0x0000000000000000000000000000000000000000",
                ethereumTransaction.safeNonce.toBigInteger(),
            )
        } ?:
        EvmTransactionUtil.computeSafeTransactionHash(
            ethereumTransaction.chainId,
            walletAddress,
            destinationAddress,
            symbolAndAmountInfo.fundamentalAmountAsBigInteger(),
            ByteArray(0),
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