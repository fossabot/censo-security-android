package com.censocustody.android.common.util

import com.censocustody.android.common.wrapper.toHexString
import org.bitcoinj.core.Base58
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
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

    private fun getECPublicKeyFromCompressedBytes(hexKey: String, curveName: String): ECPublicKey {
        val spec = ECNamedCurveTable.getParameterSpec(curveName)
        val pubPoint = spec.curve.decodePoint(Hex.decode(hexKey))
        return getECPublicKey(pubPoint.getEncoded(false).toHexString(), curveName)
    }
}