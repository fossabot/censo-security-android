package com.censocustody.android.common.evm
import java.math.BigInteger

typealias EvmAddress = String
typealias OwnerAddress = String
typealias EvmWhitelistInstruction = String

object GnosisSafeConstants {

    val multiSendCallOnlyAddress = "0x40A2aCCbd92BCA938b02010E17A5b8929b49130D"
    val addressZero = "0x000000000000000000000000000000000000000000"

    val sentinelAddress = "0x0000000000000000000000000000000000000001"

    val guardStorageSlot = BigInteger("4a204f620c8c5ccdca3fd54d003badd85ba500436a431f0cbda4f558c93c34c8", 16)

    val censoGuard = "CensoGuard"
    val censoTransfersOnlyGuard = "CensoTransfersOnlyGuard"
    val censoTransfersOnlyWhitelistingGuard = "CensoTransfersOnlyWhitelistingGuard"
    val censoWhitelistingGuard = "CensoWhitelistingGuard"
    val censoFallbackHandler = "CensoFallbackHandler"

}