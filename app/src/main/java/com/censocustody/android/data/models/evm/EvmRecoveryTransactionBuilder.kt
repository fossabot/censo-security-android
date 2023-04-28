package com.censocustody.android.data.models.evm

import com.censocustody.android.common.evm.*
import com.censocustody.android.data.models.OrgAdminRecoveryRequest
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import java.math.BigInteger


object EvmRecoveryTransactionBuilder {

    fun getRecoveryDataSafeHash(adminRecoveryTxs: OrgAdminRecoveryRequest.AdminRecoveryTxs, signingData: ApprovalRequestDetailsV2.SigningData.EthereumTransaction): ByteArray {
        return EvmTransactionUtil.computeSafeTransactionHash(
            chainId = signingData.chainId,
            safeAddress = adminRecoveryTxs.recoveryContractAddress,
            to = adminRecoveryTxs.orgVaultSafeAddress,
            value = BigInteger.ZERO,
            data = getRecoveryContractExecutionFromModuleData(
                adminRecoveryTxs.orgVaultSafeAddress,
                adminRecoveryTxs.oldOwnerAddress,
                adminRecoveryTxs.newOwnerAddress,
                adminRecoveryTxs.txs
            ),
            nonce = signingData.safeNonce.toBigInteger()
        )
    }

    private fun getRecoveryContractExecutionFromModuleData(orgVaultAddress: String, old: String, new: String, changes: List<OrgAdminRecoveryRequest.RecoverySafeTx>): ByteArray {
        val (data, isMultiSend) = getRecoveryUpdateData(orgVaultAddress, old, new, changes)
        return EvmConfigTransactionBuilder.execTransactionFromModuleTx(
            if (isMultiSend) GnosisSafeConstants.multiSendCallOnlyAddress else orgVaultAddress,
            BigInteger.ZERO,
            data,
            if (isMultiSend) Operation.DELEGATECALL else Operation.CALL
        )
    }

    private fun getRecoveryUpdateData(orgVaultAddress: String, old: String, new: String, changes: List<OrgAdminRecoveryRequest.RecoverySafeTx>): PolicyUpdateData {
        val encodedFunctionCalls = getRecoveryFunctionCalls(orgVaultAddress, old, new, changes)
        return when {
            encodedFunctionCalls.isEmpty() -> PolicyUpdateData(ByteArray(0), false)
            encodedFunctionCalls.size == 1 -> PolicyUpdateData(encodedFunctionCalls[0].second, false)
            else -> {
                PolicyUpdateData(
                    EvmConfigTransactionBuilder.multiSendTx(
                        encodedFunctionCalls.map {
                            val normalizedAddress = EvmTransactionUtil.normalizeAddress(it.first)
                            EvmConfigTransactionBuilder.encodeTransaction(normalizedAddress, it.second)
                        }.reduce { array, next -> array + next },
                    ),
                    true
                )
            }
        }
    }

    private fun getRecoveryFunctionCalls(orgVaultAddress: String, old: String, new: String, changes: List<OrgAdminRecoveryRequest.RecoverySafeTx>): List<Pair<String, ByteArray>> {
        return changes.map { change ->
            Pair(
                when (change) {
                    is OrgAdminRecoveryRequest.RecoverySafeTx.OrgVaultSwapOwner -> orgVaultAddress
                    is OrgAdminRecoveryRequest.RecoverySafeTx.VaultSwapOwner -> change.vaultSafeAddress
                    is OrgAdminRecoveryRequest.RecoverySafeTx.WalletSwapOwner -> change.vaultSafeAddress
                },
                when (change) {
                    is OrgAdminRecoveryRequest.RecoverySafeTx.OrgVaultSwapOwner ->
                        EvmConfigTransactionBuilder.swapOwnerTx(change.prev, old, new)

                    is OrgAdminRecoveryRequest.RecoverySafeTx.VaultSwapOwner ->
                        EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleData(
                            change.vaultSafeAddress,
                            EvmConfigTransactionBuilder.swapOwnerTx(change.prev, old, new)
                        )

                    is OrgAdminRecoveryRequest.RecoverySafeTx.WalletSwapOwner -> {
                        EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleData(
                            change.walletSafeAddress,
                            EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleData(change.walletSafeAddress, EvmConfigTransactionBuilder.swapOwnerTx(change.prev, old, new))
                        )
                    }
                }
            )
        }
    }
}