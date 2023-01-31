package com.censocustody.android.evm

import com.censocustody.android.common.evm.*

import org.web3j.crypto.Keys
import org.junit.Assert.*
import org.junit.Test

class EvmWhitelistHelperTest {

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

    @Test
    fun testNameHash() {
        val destination = EvmDestination(name = "hello world", address = addresses[0])
        assertEquals(destination.nameHash(), "b94d27b9934d3e08a52e52d7")
        assertEquals(destination.nameHashAndAddress(), "b94d27b9934d3e08a52e52d7" + cleanAddresses[0])
    }

    @Test
    fun `test single address added`() {
        assertEquals(
            EvmWhitelistHelper(addresses.slice(0 until 3), destinations).allChanges(),
            listOf(
                nameHashes[3] + cleanAddresses[3]
            )
        )
    }

    @Test
    fun `test multiple addresses added`() {
        assertEquals(
            EvmWhitelistHelper(addresses.slice(0 until 2), destinations).allChanges(),
            listOf(
                nameHashes[2] + cleanAddresses[2],
                nameHashes[3] + cleanAddresses[3]
            )
        )
    }

    @Test
    fun `test single address removed`() {
        assertEquals(
            EvmWhitelistHelper(addresses, destinations.slice(0 until 3)).allChanges(),
            listOf(
                "000000000000000000000001" + cleanAddresses[2]
            )
        )
    }

    @Test
    fun `test multiple contiguous addresses removed`() {
        assertEquals(
            EvmWhitelistHelper(addresses, destinations.slice(0 until 2)).allChanges(),
            listOf(
                "000000000000000000000002" + cleanAddresses[1]
            )
        )
    }

    @Test
    fun `test multiple non contiguous addresses removed`() {
        assertEquals(
            EvmWhitelistHelper(addresses, listOf(destinations[1])).allChanges(),
            listOf(
                "000000000000000000000001" + GnosisSafeConstants.sentinelAddress.clean(),
                "000000000000000000000002" + cleanAddresses[1]
            )
        )
    }

    @Test
    fun `test adds and removes`() {
        assertEquals(
            EvmWhitelistHelper(addresses.slice(0 until 3), destinations.slice(1 until 4)).allChanges(),
            listOf(
                "000000000000000000000001" + GnosisSafeConstants.sentinelAddress.clean(),
                nameHashes[3] + cleanAddresses[3]
            )
        )
    }
}
