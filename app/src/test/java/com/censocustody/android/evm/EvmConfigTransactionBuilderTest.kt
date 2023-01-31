package com.censocustody.android.evm

import com.censocustody.android.common.evm.*
import com.censocustody.android.common.toHexString
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.evm.EvmConfigTransactionBuilder
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert.*
import org.junit.Test
import org.web3j.crypto.Keys

class EvmConfigTransactionBuilderTest {

    private val addresses = listOf(
        Keys.toChecksumAddress("0x6e01af3913026660fcebb93f054345eccd972252"),
        Keys.toChecksumAddress("0x6e01af3913026660fcebb93f054345eccd972253"),
        Keys.toChecksumAddress("0x6e01af3913026660fcebb93f054345eccd972254"),
        Keys.toChecksumAddress("0x6e01af3913026660fcebb93f054345eccd972255")
    )
    private val destinations = listOf(
        EvmDestination(name = "", address = addresses[0]),
        EvmDestination(name = "", address = addresses[1]),
        EvmDestination(name = "", address = addresses[2]),
        EvmDestination(name = "", address = addresses[3])
    )
    private val nameHashes = destinations.map { it.nameHash() }

    private val cleanAddresses = addresses.map { it.clean().lowercase() }

    private val vaultAddress = Keys.toChecksumAddress("0x6e01af3913026660fcebb93f054345eccd972260")
    private val walletAddress = Keys.toChecksumAddress("0x6e01af3913026660fcebb93f054345eccd972261")
    private val guardAddress = Keys.toChecksumAddress("0x6e01af3913026660fcebb93f054345eccd972262")
    private val signingData = ApprovalRequestDetailsV2.SigningData.EthereumTransaction(
        chainId = 31337,
        safeNonce = 10L,
        vaultAddress = vaultAddress
    )

    @Test
    fun `test change guard`() {
        val bytes = EvmConfigTransactionBuilder.getSetGuardExecutionFromModuleData(
            walletAddress, guardAddress
        )
        assertEquals(
            "468721a70000000000000000000000006e01af3913026660fcebb93f054345eccd9722610000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000024e19a9dd90000000000000000000000006e01af3913026660fcebb93f054345eccd97226200000000000000000000000000000000000000000000000000000000",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "f6eb42d06a737180fca879e87d6963d6030c0d40d0fdd091a4dbc4a97def0775",
            EvmConfigTransactionBuilder.getSetGuardExecutionFromModuleDataSafeHash(walletAddress, guardAddress, signingData).toHexString().lowercase()
        )
    }


    @Test
    fun `test address list changes`() {
        val addressesToAdd = listOf(
            Hex.decode(nameHashes[2] + cleanAddresses[2]),
            Hex.decode(nameHashes[3] + cleanAddresses[3])
        )
        val bytes = EvmConfigTransactionBuilder.getUpdateWhitelistExecutionFromModuleData(
            walletAddress, addressesToAdd
        )
        assertEquals(
            "468721a70000000000000000000000006e01af3913026660fcebb93f054345eccd97226100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000847aaea4f600000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000002e3b0c44298fc1c149afbf4c86e01af3913026660fcebb93f054345eccd972254e3b0c44298fc1c149afbf4c86e01af3913026660fcebb93f054345eccd97225500000000000000000000000000000000000000000000000000000000",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "7b93aed2b9c9b9028885fcaa0425748007cebacde75ee6df57f60314d683bf97",
            EvmConfigTransactionBuilder.getUpdateWhitelistExecutionFromModuleDataSafeHash(walletAddress, addressesToAdd, signingData).toHexString().lowercase()
        )
    }

    @Test
    fun `test vault policy change - add owner`() {
        val txs = listOf(SafeTx.AddOwnerWithThreshold(addresses[0], 2))
        val (bytes, isMultiSend) = EvmConfigTransactionBuilder.getPolicyUpdateData(
            vaultAddress, txs
        )
        assertFalse(isMultiSend)
        assertEquals(
            "0d582f130000000000000000000000006e01af3913026660fcebb93f054345eccd9722520000000000000000000000000000000000000000000000000000000000000002",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "24befecdd62a870d007522cdc00e757452dd07a23cdb987848ad85e602af073d",
            EvmConfigTransactionBuilder.getPolicyUpdateDataSafeHash(txs, signingData).toHexString().lowercase()
        )
    }

    @Test
    fun `test vault policy change - remove owner`() {
        val txs = listOf(SafeTx.RemoveOwner(addresses[0], addresses[1], 2))
        val (bytes, isMultiSend) = EvmConfigTransactionBuilder.getPolicyUpdateData(
            vaultAddress, txs
        )
        assertFalse(isMultiSend)
        assertEquals(
            "f8dc5dd90000000000000000000000006e01af3913026660fcebb93f054345eccd9722520000000000000000000000006e01af3913026660fcebb93f054345eccd9722530000000000000000000000000000000000000000000000000000000000000002",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "046eb5adcfa5c851629e450f368acba9806c87e4d6408813eba7afc81a390368",
            EvmConfigTransactionBuilder.getPolicyUpdateDataSafeHash(txs, signingData).toHexString().lowercase()
        )
    }

    @Test
    fun `test vault policy change - swap owner`() {
        val txs = listOf(SafeTx.SwapOwner(addresses[0], addresses[1], addresses[2]))
        val (bytes, isMultiSend) = EvmConfigTransactionBuilder.getPolicyUpdateData(
            vaultAddress, listOf(SafeTx.SwapOwner(addresses[0], addresses[1], addresses[2]))
        )
        assertFalse(isMultiSend)
        assertEquals(
            "e318b52b0000000000000000000000006e01af3913026660fcebb93f054345eccd9722520000000000000000000000006e01af3913026660fcebb93f054345eccd9722530000000000000000000000006e01af3913026660fcebb93f054345eccd972254",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "209645fd1c01854cc1de79fa303547c08f24db5201ae0854d1b2e6d9c16004b3",
            EvmConfigTransactionBuilder.getPolicyUpdateDataSafeHash(txs, signingData).toHexString().lowercase()
        )
    }

    @Test
    fun `test vault policy change - change threshold`() {
        val txs = listOf(SafeTx.ChangeThreshold(5))
        val (bytes, isMultiSend) = EvmConfigTransactionBuilder.getPolicyUpdateData(
            vaultAddress, txs
        )
        assertFalse(isMultiSend)
        assertEquals(
            "694e80c30000000000000000000000000000000000000000000000000000000000000005",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "0c49daaae601de5348ab37760f0de0affc0e4870069ad66e387d1aaead179ca4",
            EvmConfigTransactionBuilder.getPolicyUpdateDataSafeHash(txs, signingData).toHexString().lowercase()
        )
    }
    @Test
    fun `test vault policy change - multisend`() {
        val txs = listOf(
            SafeTx.AddOwnerWithThreshold(addresses[0], 2),
            SafeTx.RemoveOwner(addresses[0], addresses[1], 2)
        )
        val (bytes, isMultiSend) = EvmConfigTransactionBuilder.getPolicyUpdateData(
            vaultAddress, txs
        )
        assertTrue(isMultiSend)
        assertEquals(
            "8d80ff0a00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000152006e01af3913026660fcebb93f054345eccd972260000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000440d582f130000000000000000000000006e01af3913026660fcebb93f054345eccd9722520000000000000000000000000000000000000000000000000000000000000002006e01af3913026660fcebb93f054345eccd97226000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000064f8dc5dd90000000000000000000000006e01af3913026660fcebb93f054345eccd9722520000000000000000000000006e01af3913026660fcebb93f054345eccd97225300000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "04ef6db23f440eecbe4d5e47947677b160758b730821bf4adb98acecd17b3d1d",
            EvmConfigTransactionBuilder.getPolicyUpdateDataSafeHash(txs, signingData).toHexString().lowercase()
        )
    }

    @Test
    fun `test wallet policy change - add owner`() {
        val txs = listOf(SafeTx.AddOwnerWithThreshold(addresses[0], 2))
        val bytes = EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleData(
            walletAddress, txs
        )
        assertEquals(
            "468721a70000000000000000000000006e01af3913026660fcebb93f054345eccd97226100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000440d582f130000000000000000000000006e01af3913026660fcebb93f054345eccd972252000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "b5818ccdbbd8ea23d2af5d345b4c968d6565ed216349b64818fc977f8d0f66e9",
            EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(walletAddress, txs, signingData).toHexString().lowercase()
        )
    }

    @Test
    fun `test wallet policy change - remove owner`() {
        val txs = listOf(SafeTx.RemoveOwner(addresses[0], addresses[1], 2))
        val bytes = EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleData(
            walletAddress, txs
        )
        assertEquals(
            "468721a70000000000000000000000006e01af3913026660fcebb93f054345eccd9722610000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000064f8dc5dd90000000000000000000000006e01af3913026660fcebb93f054345eccd9722520000000000000000000000006e01af3913026660fcebb93f054345eccd972253000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "c6e2dd950ce4f5da473e2b303136d00097b95287a9f9c26e7c0f89a5c2e0d017",
            EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(walletAddress, txs, signingData).toHexString().lowercase()
        )
    }

    @Test
    fun `test wallet policy change - swap owner`() {
        val txs = listOf(SafeTx.SwapOwner(addresses[0], addresses[1], addresses[2]))
        val bytes = EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleData(
            walletAddress, txs
        )
        assertEquals(
            "468721a70000000000000000000000006e01af3913026660fcebb93f054345eccd9722610000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000064e318b52b0000000000000000000000006e01af3913026660fcebb93f054345eccd9722520000000000000000000000006e01af3913026660fcebb93f054345eccd9722530000000000000000000000006e01af3913026660fcebb93f054345eccd97225400000000000000000000000000000000000000000000000000000000",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "b2520eb52a950da57d80cb2ddc0f47bb7a5a545c2f6ad30669063823407f1318",
            EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(walletAddress, txs, signingData).toHexString().lowercase()
        )
    }

    @Test
    fun `test wallet policy change - change threshold`() {
        val txs = listOf(SafeTx.ChangeThreshold(5))
        val bytes = EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleData(
            walletAddress, txs
        )
        assertEquals(
            "468721a70000000000000000000000006e01af3913026660fcebb93f054345eccd9722610000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000024694e80c3000000000000000000000000000000000000000000000000000000000000000500000000000000000000000000000000000000000000000000000000",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "a9ff0670e3923384a0ec571acaa7cbdfa664dff96463a9f33b88f41ce3055999",
            EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(walletAddress, txs, signingData).toHexString().lowercase()
        )
    }

    @Test
    fun `test wallet policy change - multisend`() {
        val txs = listOf(
            SafeTx.AddOwnerWithThreshold(addresses[0], 2),
            SafeTx.RemoveOwner(addresses[0], addresses[1], 2)
        )
        val bytes = EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleData(
            walletAddress, txs
        )
        assertEquals(
            "468721a700000000000000000000000040a2accbd92bca938b02010e17a5b8929b49130d00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000001a48d80ff0a00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000152006e01af3913026660fcebb93f054345eccd972261000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000440d582f130000000000000000000000006e01af3913026660fcebb93f054345eccd9722520000000000000000000000000000000000000000000000000000000000000002006e01af3913026660fcebb93f054345eccd97226100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000064f8dc5dd90000000000000000000000006e01af3913026660fcebb93f054345eccd9722520000000000000000000000006e01af3913026660fcebb93f054345eccd9722530000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
            bytes.toHexString().lowercase()
        )
        assertEquals(
            "ef3d0fbdefa12a1460200dbf73ea20fc64150f13029153759e593319f993799e",
            EvmConfigTransactionBuilder.getPolicyUpdateExecutionFromModuleDataSafeHash(walletAddress, txs, signingData).toHexString().lowercase()
        )
    }
}
