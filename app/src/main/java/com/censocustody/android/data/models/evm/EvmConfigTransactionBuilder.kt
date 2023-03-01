package com.censocustody.android.data.models.evm

import com.censocustody.android.common.evm.*
import com.censocustody.android.common.pad
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import org.bouncycastle.util.encoders.Hex
import org.web3j.crypto.Hash
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class PolicyUpdateData(
    val data: ByteArray,
    val multiSend: Boolean
)

object EvmConfigTransactionBuilder {

    fun getSetGuardExecutionFromModuleDataSafeHash(walletAddress: EvmAddress, guardAddress: EvmAddress, signingData: ApprovalRequestDetailsV2.SigningData.EthereumTransaction): ByteArray {
        return EvmTransactionUtil.computeSafeTransactionHash(
            chainId = signingData.chainId,
            safeAddress = signingData.vaultAddress!!,
            to = walletAddress,
            value = BigInteger.ZERO,
            data = execTransactionFromModuleTx(
                walletAddress,
                BigInteger.ZERO,
                setGuardTx(guardAddress),
                Operation.CALL),
            nonce = signingData.safeNonce.toBigInteger()
        )
    }

    fun getSetGuardExecutionFromModuleData(walletAddress: EvmAddress, guardAddress: EvmAddress): ByteArray {
        return execTransactionFromModuleTx(
            walletAddress,
            BigInteger.ZERO,
            setGuardTx(guardAddress),
            Operation.CALL)
    }

    fun getUpdateWhitelistExecutionFromModuleDataSafeHash(walletAddress: EvmAddress, addsOrRemoves: List<ByteArray>, signingData: ApprovalRequestDetailsV2.SigningData.EthereumTransaction): ByteArray {
        return EvmTransactionUtil.computeSafeTransactionHash(
            chainId = signingData.chainId,
            safeAddress = signingData.vaultAddress!!,
            to = walletAddress,
            value = BigInteger.ZERO,
            data = execTransactionFromModuleTx(
                walletAddress,
                BigInteger.ZERO,
                updateWhitelistTx(addsOrRemoves),
                Operation.CALL),
            nonce = signingData.safeNonce.toBigInteger()
        )
    }

    fun getUpdateWhitelistExecutionFromModuleData(walletAddress: EvmAddress, addsOrRemoves: List<ByteArray>): ByteArray {
        return execTransactionFromModuleTx(
            walletAddress,
            BigInteger.ZERO,
            updateWhitelistTx(addsOrRemoves),
            Operation.CALL)
    }


    fun getPolicyUpdateDataSafeHash(txs: List<SafeTx>, signingData: ApprovalRequestDetailsV2.SigningData.EthereumTransaction): ByteArray {
        val vaultAddress = signingData.vaultAddress!!
        val (data, isMultiSend) = getPolicyUpdateData(vaultAddress, txs)
        return EvmTransactionUtil.computeSafeTransactionHash(
            chainId = signingData.chainId,
            safeAddress = vaultAddress,
            to = if (isMultiSend) GnosisSafeConstants.multiSendCallOnlyAddress else vaultAddress,
            value = BigInteger.ZERO,
            data = data,
            nonce = signingData.safeNonce.toBigInteger(),
            operation = if (isMultiSend) Operation.DELEGATECALL else Operation.CALL
        )
    }

    fun getPolicyUpdateData(safeAddress: EvmAddress, txs: List<SafeTx>): PolicyUpdateData {
        val encodedTxs = getPolicyChangeDataList(txs)
        return when {
            encodedTxs.isEmpty() -> PolicyUpdateData(ByteArray(0), false)
            encodedTxs.size == 1 -> PolicyUpdateData(encodedTxs[0], false)
            else -> {
                val normalizedAddress = EvmTransactionUtil.normalizeAddress(safeAddress)
                PolicyUpdateData(
                    multiSendTx(
                        encodedTxs.map {
                            encodeTransaction(normalizedAddress, it)
                        }.reduce { array, next -> array + next }
                    ),
                    true
                )
            }
        }
    }

    fun getPolicyUpdateExecutionFromModuleData(
        safeAddress: String,
        txs: List<SafeTx>
    ): ByteArray {
        val (data, isMultiSend) = getPolicyUpdateData(safeAddress, txs)
        return execTransactionFromModuleTx(
            if (isMultiSend) GnosisSafeConstants.multiSendCallOnlyAddress else safeAddress,
            BigInteger.ZERO,
            data,
            if (isMultiSend) Operation.DELEGATECALL else Operation.CALL
        )
    }

    fun getPolicyUpdateExecutionFromModuleDataSafeHash(
        verifyingAddress: String,
        safeAddress: String,
        txs: List<SafeTx>,
        signingData: ApprovalRequestDetailsV2.SigningData.EthereumTransaction
    ): ByteArray {
        return EvmTransactionUtil.computeSafeTransactionHash(
            chainId = signingData.chainId,
            safeAddress = verifyingAddress,
            to = safeAddress,
            value = BigInteger.ZERO,
            data = getPolicyUpdateExecutionFromModuleData(safeAddress, txs),
            nonce = signingData.safeNonce.toBigInteger()
        )
    }

    fun getNameUpdateExecutionFromModuleData(
        safeAddress: String,
        newName: String
    ): ByteArray {
        val data = setNameHash(newName)
        return execTransactionFromModuleTx(
            safeAddress,
            BigInteger.ZERO,
            data,
            Operation.CALL
        )
    }

    fun getNameUpdateExecutionFromModuleDataSafeHash(
        verifyingAddress: String,
        safeAddress: String,
        newName: String,
        signingData: ApprovalRequestDetailsV2.SigningData.EthereumTransaction
    ): ByteArray {
        return EvmTransactionUtil.computeSafeTransactionHash(
            chainId = signingData.chainId,
            safeAddress = verifyingAddress,
            to = safeAddress,
            value = BigInteger.ZERO,
            data = getNameUpdateExecutionFromModuleData(safeAddress, newName),
            nonce = signingData.safeNonce.toBigInteger()
        )
    }

    private fun getPolicyChangeDataList(changes: List<SafeTx>): List<ByteArray> {
        return changes.map { change ->
            when (change) {
                is SafeTx.SwapOwner -> swapOwnerTx(change.prev, change.old, change.new)
                is SafeTx.AddOwnerWithThreshold -> addOwnerWithThresholdTx(change.owner, change.threshold.toBigInteger())
                is SafeTx.RemoveOwner -> removeOwnerTx(change.prev, change.owner, change.threshold.toBigInteger())
                is SafeTx.ChangeThreshold -> changeThresholdTx(change.threshold.toBigInteger())
            }
        }
    }

    private fun setGuardTx(guardAddress: EvmAddress): ByteArray {
        // setGuard(address)
        val dataBuf = ByteBuffer.allocate(4 + 32)
        dataBuf.put(Hex.decode("e19a9dd9"))
        dataBuf.putPadded(Hex.decode(guardAddress.clean()))
        return dataBuf.array()
    }

    private fun setNameHash(name: String): ByteArray {
        // setNameHash(bytes32)
        val dataBuf = ByteBuffer.allocate(4 + 32)
        dataBuf.put(Hex.decode("3afbdcf4"))
        dataBuf.putPadded(Hash.sha3(name.toByteArray()))
        return dataBuf.array()
    }

    private fun updateWhitelistTx(addsOrRemoves: List<ByteArray>): ByteArray {
        // updateWhitelist(bytes[])
        val dataBuf = ByteBuffer.allocate(4 + 32 * 2 +  32 * addsOrRemoves.size)
        dataBuf.put(Hex.decode("7aaea4f6"))
        dataBuf.putPadded(BigInteger("32").toByteArray())  // data offset
        dataBuf.putPadded(addsOrRemoves.size.toBigInteger().toByteArray())
        addsOrRemoves.forEach {
            dataBuf.put(it)
        }
        return dataBuf.array()
    }

    private fun execTransactionFromModuleTx(to: EvmAddress, value: BigInteger, data: ByteArray, operation: Operation): ByteArray {
        // execTransactionFromModule(address,uint256,bytes,unit256)
        val mod = data.size.mod(32)
        val padding = if (mod > 0) 32 - mod else 0
        val dataBuf = ByteBuffer.allocate(4 + 32*5 + data.size + padding)
        dataBuf.put(Hex.decode("468721a7"))
        dataBuf.putPadded(Hex.decode(to.clean()))
        dataBuf.putPadded(value.toByteArray())
        dataBuf.putPadded(BigInteger("128").toByteArray())  // data offset
        dataBuf.putPadded(operation.toByteArray())
        dataBuf.putPadded(data.size.toBigInteger().toByteArray())
        dataBuf.put(data)
        if (padding > 0) { // pad dynamic data to 32 byte boundary if required
            dataBuf.putPadded(ByteArray(0), 32 - mod)
        }
        return dataBuf.array()
    }

    private fun addOwnerWithThresholdTx(owner: EvmAddress, threshold: BigInteger): ByteArray {
        // addOwnerWithThreshold(address,uint256)
        val dataBuf = ByteBuffer.allocate(4 + 32 * 2)
        dataBuf.put(Hex.decode("0d582f13"))
        dataBuf.putPadded(Hex.decode(owner.clean()))
        dataBuf.putPadded(threshold.toByteArray())
        return dataBuf.array()
    }

    private fun removeOwnerTx(prev: EvmAddress, owner: EvmAddress, threshold: BigInteger): ByteArray {
        // removeOwner(address,address,uint256)
        val dataBuf = ByteBuffer.allocate(4 + 32 * 3)
        dataBuf.put(Hex.decode("f8dc5dd9"))
        dataBuf.putPadded(Hex.decode(prev.clean()))
        dataBuf.putPadded(Hex.decode(owner.clean()))
        dataBuf.putPadded(threshold.toByteArray())
        return dataBuf.array()
    }

    private fun swapOwnerTx(prev: EvmAddress, old: EvmAddress, new: EvmAddress): ByteArray {
        // swapOwner(address,address,address)
        val dataBuf = ByteBuffer.allocate(4 + 32 * 3)
        dataBuf.put(Hex.decode("e318b52b"))
        dataBuf.putPadded(Hex.decode(prev.clean()))
        dataBuf.putPadded(Hex.decode(old.clean()))
        dataBuf.putPadded(Hex.decode(new.clean()))
        return dataBuf.array()
    }

    private fun changeThresholdTx(threshold: BigInteger): ByteArray {
        // changeThreshold(uint256)
        val dataBuf = ByteBuffer.allocate(4 + 32)
        dataBuf.put(Hex.decode("694e80c3"))
        dataBuf.putPadded(threshold.toByteArray())
        return dataBuf.array()
    }

    private fun multiSendTx(data:  ByteArray): ByteArray {
        // multiSend(bytes)
        val mod = data.size.mod(32)
        val padding = if (mod > 0) 32 - mod else 0
        val dataBuf = ByteBuffer.allocate(4 + 32 * 2 + data.size + padding)
        dataBuf.put(Hex.decode("8d80ff0a"))
        dataBuf.putPadded(BigInteger("32").toByteArray())  // data offset
        dataBuf.putPadded(data.size.toBigInteger().toByteArray())  // data offset
        dataBuf.put(data)
        if (padding > 0) {
            dataBuf.putPadded(ByteArray(0), 32 - mod)
        }
        return dataBuf.array()
    }

    private fun encodeTransaction(address: ByteArray, data: ByteArray): ByteArray {
        val buffer = ByteBuffer.allocate(1 + 20 + 32 + 32 + data.size)
        buffer.put(0)
        buffer.put(address)
        buffer.put(ByteArray(32))
        val valueBytes = ByteArray(4)
        ByteBuffer.wrap(valueBytes).order(ByteOrder.BIG_ENDIAN).putInt(data.size)
        buffer.put(valueBytes.pad(32))
        buffer.put(data)
        return buffer.array()
    }
}