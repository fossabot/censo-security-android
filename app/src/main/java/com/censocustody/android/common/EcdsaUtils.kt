package com.censocustody.android.common

import org.bitcoinj.core.Base58
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec

object EcdsaUtils {
    const val curve = "secp256k1"
    const val r1Curve = "prime256v1"

    private const val keyLength = 64
    private val bcProvider = BouncyCastleProvider()

    fun getCompressedKeyBytesFromBase58(base58Key: String, curveName: String): ByteArray {
        val bytes = Base58.decode(base58Key)
        return when (bytes.size) {
            32 -> bytes
            33 -> bytes.slice(IntRange(1, 32)).toByteArray()
            else -> getECPublicKey(bytes.toHexString(), curveName).q.getEncoded(true)
                .slice(IntRange(1, 32)).toByteArray()
        }
    }

    private fun getECPublicKey(hexKey: String, curveName: String): ECPublicKey {
        // create a public key using the provided hex string and curve name
        val bytes = Hex.decode(hexKey)
        val startingOffset = if (bytes.size == keyLength + 1 && bytes[0].compareTo(4) == 0) 1 else 0
        val x = bytes.slice(IntRange(startingOffset, 31 + startingOffset)).toByteArray()
        val y = bytes.slice(IntRange(startingOffset + 32, 63 + startingOffset)).toByteArray()

        val pubPoint = ECPoint(BigInteger(1, x), BigInteger(1, y))
        val params = AlgorithmParameters.getInstance("EC", bcProvider).apply {
            init(ECGenParameterSpec(curveName))
        }
        val pubECSpec =
            ECPublicKeySpec(pubPoint, params.getParameterSpec(ECParameterSpec::class.java))
        return KeyFactory.getInstance("EC", bcProvider).generatePublic(pubECSpec) as ECPublicKey
    }

    fun getECPublicKeyFromBase58(base58Key: String, curveName: String): ECPublicKey {
        val bytes = Base58.decode(base58Key)
        return if (bytes.size == 33 || bytes.size == 32) {
            getECPublicKeyFromCompressedBytes(bytes.toHexString(), curveName)
        } else {
            getECPublicKey(bytes.toHexString(), curveName)
        }
    }

    fun getECPrivateKey(hexKey: String, curveName: String): ECPrivateKey {
        // create a private key using the provided hex string and curve name
        val privateKey = Hex.decode(hexKey)

        val factory = KeyFactory.getInstance("EC", bcProvider)
        val spec = ECNamedCurveTable.getParameterSpec(curveName)
        val ecPrivateKeySpec = ECPrivateKeySpec(BigInteger(1, privateKey), spec)
        return factory.generatePrivate(ecPrivateKeySpec) as ECPrivateKey
    }

    private fun getECPublicKeyFromCompressedBytes(hexKey: String, curveName: String): ECPublicKey {
        val spec = ECNamedCurveTable.getParameterSpec(curveName)
        val pubPoint = spec.curve.decodePoint(Hex.decode(hexKey))
        return getECPublicKey(pubPoint.getEncoded(false).toHexString(), curveName)
    }

    fun derivePublicKeyFromPrivateKey(privateKey: ECPrivateKey): ECPublicKey {
        val keyFactory = KeyFactory.getInstance("EC", bcProvider)
        val q = privateKey.parameters.g.multiply(privateKey.d)
        return keyFactory.generatePublic(
            org.bouncycastle.jce.spec.ECPublicKeySpec(
                q,
                privateKey.parameters
            )
        ) as ECPublicKey
    }

    fun derivePublicKeyFromPrivateKeyAsBase58(privateKey: ECPrivateKey, compressed: Boolean = false): String {
        return Base58.encode(derivePublicKeyFromPrivateKey(privateKey).q.getEncoded(compressed))
    }

    fun derivePublicKeyFromPrivateKeyAsHexString(privateKey: ECPrivateKey): String {
        return derivePublicKeyFromPrivateKey(privateKey).q.getEncoded(false).toHexString()
    }
}