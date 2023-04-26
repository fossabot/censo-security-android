package com.censocustody.android.common.evm

import com.censocustody.android.common.wrapper.pad
import com.censocustody.android.common.wrapper.toHexString
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import org.web3j.crypto.Hash
import java.nio.ByteBuffer
import java.nio.ByteOrder


fun String.clean(): String {
    return if (this.startsWith("0x", ignoreCase = true)) this.drop(2) else this
}

data class EvmDestination(
    val name: String,
    val address: String
)

data class WhitelistSettings(
    val whitelistEnabled: Boolean,
    val dappsEnabled: Boolean
)

fun EvmDestination.nameHash() = Hash.sha256(name.toByteArray()).take(12).toByteArray().toHexString()
fun EvmDestination.nameHashAndAddress() = nameHash() + address.clean().lowercase()

class EvmWhitelistHelper(val addresses: List<EvmAddress>, val targetDestinations: List<EvmDestination>) {
    private val targetAddresses = targetDestinations.map { it.address.lowercase() }
    private val currentAddresses = addresses.map { it.lowercase() }

    companion object {

        fun getTargetGuardAddress(currentGuardAddress: EvmAddress, whitelistEnabled: Boolean?, dappsEnabled: Boolean?, guardAddresses: List<ApprovalRequestDetailsV2.SigningData.ContractNameAndAddress>): EvmAddress {
            val currentSetting = getCurrentSettingsForGuardName(
                guardAddresses.find { it.address.lowercase() == currentGuardAddress.lowercase() }?.name ?: GnosisSafeConstants.censoGuard
            )
            val targetSettings = WhitelistSettings(whitelistEnabled ?: currentSetting.whitelistEnabled, dappsEnabled ?: currentSetting.dappsEnabled)
            return guardAddresses.find { it.name == getGuardNameForTargetSettings(targetSettings) }?.address ?: currentGuardAddress

        }
        fun getCurrentSettingsForGuardName(guardName: String): WhitelistSettings {
            return when (guardName) {
                GnosisSafeConstants.censoGuard -> WhitelistSettings(whitelistEnabled = false, dappsEnabled = true)
                GnosisSafeConstants.censoTransfersOnlyGuard -> WhitelistSettings(whitelistEnabled = false, dappsEnabled = false)
                GnosisSafeConstants.censoTransfersOnlyWhitelistingGuard -> WhitelistSettings(whitelistEnabled = true, dappsEnabled = false)
                GnosisSafeConstants.censoWhitelistingGuard -> WhitelistSettings(whitelistEnabled = true, dappsEnabled = true)
                else -> throw Exception("Unable to determine current guard")
            }
        }

        fun getGuardNameForTargetSettings(targetSettings: WhitelistSettings): String {
            return when (targetSettings) {
                WhitelistSettings(whitelistEnabled = false, dappsEnabled = true) -> GnosisSafeConstants.censoGuard
                WhitelistSettings(whitelistEnabled = false, dappsEnabled = false) -> GnosisSafeConstants.censoTransfersOnlyGuard
                WhitelistSettings(whitelistEnabled = true, dappsEnabled = false) -> GnosisSafeConstants.censoTransfersOnlyWhitelistingGuard
                else -> GnosisSafeConstants.censoWhitelistingGuard
            }
        }
    }

    private fun addedAddresses(): List<EvmWhitelistInstruction> {
        return (targetAddresses.toSet() - currentAddresses.toSet()).sorted().mapNotNull { address ->
            targetDestinations.find { it.address.lowercase() == address }?.nameHashAndAddress()
        }
    }

    private fun removedAddresses(): List<EvmWhitelistInstruction> {
        val sequenceList = mutableListOf<MutableList<String>>()
        val removedAddresses = currentAddresses.toSet() - targetAddresses.toSet()
        if (removedAddresses.isNotEmpty()) {
            currentAddresses.forEach { address ->
                if (removedAddresses.contains(address)) {
                    if (sequenceList.isNotEmpty() && prevAddress(address) in sequenceList.last()) {
                        sequenceList.last().add(address)
                    } else {
                        sequenceList.add(mutableListOf(address))
                    }
                }
            }
        }
        return sequenceList.map { intTo12ByteHex(it.size) + prevAddress(it[0]).clean() }
    }

    fun allChanges(): List<EvmWhitelistInstruction> {
        return removedAddresses() + addedAddresses()
    }

    private fun prevAddress(address: String): String {
        var prev: String? = null
        currentAddresses.forEach { addr ->
            if (addr == address) {
                return prev ?: GnosisSafeConstants.sentinelAddress
            }
            prev = addr
        }
        throw Exception("Address not found")
    }

    private fun intTo12ByteHex(value: Int): String {
        val valueBytes = ByteArray(4)
        ByteBuffer.wrap(valueBytes).order(ByteOrder.BIG_ENDIAN).putInt(value)
        return valueBytes.pad(12).toHexString()
    }
}
