package com.censocustody.android.common.evm
import java.math.BigInteger

typealias EvmAddress = String
typealias OwnerAddress = String
typealias EvmWhitelistInstruction = String

object GnosisSafeConstants {

    val multiSendCallOnlyAddress = "0x40A2aCCbd92BCA938b02010E17A5b8929b49130D"
    val gnosisSafeAddress = "0xd9Db270c1B5E3Bd161E8c8503c55cEABeE709552"
    val gnosisSafeProxyFactoryAddress = "0xa6B71E26C5e0845f74c812102Ca7114b6a896AB2"
    val signMessageLibAddress = "0xA65387F16B013cf2Af4605Ad8aA5ec25a2cbA3a2"
    val addressZero = "0x000000000000000000000000000000000000000000"

    val sentinelAddress = "0x0000000000000000000000000000000000000001"

    val guardStorageSlot = BigInteger("4a204f620c8c5ccdca3fd54d003badd85ba500436a431f0cbda4f558c93c34c8", 16)

    val censoGuard = "CensoGuard"
    val censoTransfersOnlyGuard = "CensoTransfersOnlyGuard"
    val censoTransfersOnlyWhitelistingGuard = "CensoTransfersOnlyWhitelistingGuard"
    val censoWhitelistingGuard = "CensoWhitelistingGuard"
    val censoFallbackHandler = "CensoFallbackHandler"
    val censoRecoveryGuard = "CensoRecoveryGuard"
    val censoRecoveryFallbackHandler = "CensoRecoveryFallbackHandler"
    val censoSetup = "CensoSetup"

    val gnosisSafeProxyBinary = "0x608060405234801561001057600080fd5b506040516101e63803806101e68339818101604052602081101561003357600080fd5b8101908080519060200190929190505050600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff1614156100ca576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260228152602001806101c46022913960400191505060405180910390fd5b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505060ab806101196000396000f3fe608060405273ffffffffffffffffffffffffffffffffffffffff600054167fa619486e0000000000000000000000000000000000000000000000000000000060003514156050578060005260206000f35b3660008037600080366000845af43d6000803e60008114156070573d6000fd5b3d6000f3fea2646970667358221220d1429297349653a4918076d650332de1a1068c5f3e07c5c82360c277770b955264736f6c63430007060033496e76616c69642073696e676c65746f6e20616464726573732070726f7669646564"

}