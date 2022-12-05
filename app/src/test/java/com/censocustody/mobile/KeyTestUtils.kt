package com.strikeprotocols.mobile

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.strikeprotocols.mobile.common.Ed25519HierarchicalPrivateKey

fun createSolanaKeyPairFromMnemonic(mnenomic: Mnemonics.MnemonicCode): TestKeyPair {
    val rootSeed = mnenomic.toSeed()
    val solanaHierarchicalKey = Ed25519HierarchicalPrivateKey.fromRootSeed(rootSeed)

    return TestKeyPair(
        privateKey = solanaHierarchicalKey.privateKeyBytes,
        publicKey = solanaHierarchicalKey.getPublicKeyBytes(),
    )
}

fun createRootSeedFromMnemonic(mnenomic: Mnemonics.MnemonicCode)  = mnenomic.toSeed()

data class TestKeyPair(
    val privateKey: ByteArray,
    val publicKey: ByteArray,
)