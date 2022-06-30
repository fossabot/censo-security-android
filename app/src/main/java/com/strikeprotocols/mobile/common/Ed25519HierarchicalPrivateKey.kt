package com.strikeprotocols.mobile.common

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.math.ec.rfc8032.Ed25519
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import kotlin.experimental.or


class Ed25519HierarchicalPrivateKey(
    val data: ByteArray,
) {
    private val privateKeyParams: Ed25519PrivateKeyParameters = Ed25519PrivateKeyParameters(data, 0)
    private val chainCode: KeyParameter = KeyParameter(data, 32, 32)

    companion object {
        fun fromRootSeed(rootSeed: ByteArray): Ed25519HierarchicalPrivateKey {
            var derivedKey = Ed25519HierarchicalPrivateKey(
                HMac(SHA512Digest()).let { hmacSha512 ->
                    hmacSha512.init(KeyParameter("ed25519 seed".toByteArray(StandardCharsets.UTF_8)))
                    hmacSha512.update(rootSeed, 0, rootSeed.size)
                    ByteArray(hmacSha512.macSize).also { hmacSha512.doFinal(it, 0) }
                }
            )

            // BIP-44 path with the Solana coin-type
            // https://github.com/satoshilabs/slips/blob/master/slip-0044.md
            for (index in intArrayOf(44, 501, 0, 0)) {
                derivedKey = derivedKey.derive(index)
            }
            return derivedKey
        }
    }

    val privateKeyBytes: ByteArray = privateKeyParams.encoded
    val publicKeyBytes: ByteArray = privateKeyParams.generatePublicKey().encoded

    fun derive(index: Int): Ed25519HierarchicalPrivateKey {
        // SLIP-10 child key derivation
        // https://github.com/satoshilabs/slips/blob/master/slip-0010.md#master-key-generation
        return Ed25519HierarchicalPrivateKey(HMac(SHA512Digest()).let { hmacSha512 ->
            hmacSha512.init(chainCode)
            hmacSha512.update(0.toByte())
            hmacSha512.update(privateKeyParams.encoded, 0, Ed25519.SECRET_KEY_SIZE)

            // write the index in big-endian order, setting the 31st bit to mark it "hardened"
            val indexBytes = ByteArray(4)
            ByteBuffer.wrap(indexBytes).order(ByteOrder.BIG_ENDIAN).putInt(index)
            indexBytes[0] = indexBytes[0] or 128.toByte()
            hmacSha512.update(indexBytes, 0, indexBytes.size)
            ByteArray(64).also { hmacSha512.doFinal(it, 0) }
        })
    }
}