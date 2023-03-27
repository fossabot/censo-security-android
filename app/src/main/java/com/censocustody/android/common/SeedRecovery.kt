package com.censocustody.android.common

import com.censocustody.android.common.SecretSharerUtils.dotProduct
import org.bitcoinj.core.Base58
import org.bouncycastle.jce.ECNamedCurveTable
import java.math.BigInteger
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.MessageDigest

data class Point(val x: BigInteger, val y: BigInteger) {
    constructor(x: String, y: String) : this(BigInteger(x), BigInteger(y))
    constructor(x: BigInteger, y: String) : this(x, BigInteger(y))
    constructor(x: String, y: BigInteger) : this(BigInteger(x), y)
}

typealias Vector = Array<BigInteger>
typealias Matrix = Array<Vector>

val ORDER = BigInteger("13407807929942597099574024998205846127479365820592393377723561443721764030073546976801874298166903427690031858186486050853753882811946569946433649006083527")
val rnd = java.security.SecureRandom()

object SecretSharerUtils {
    fun randomFieldElement(order: BigInteger): BigInteger {
        var randomNumber: BigInteger
        BigInteger(order.bitLength(), rnd)
        do {
            randomNumber = BigInteger(order.bitLength(), rnd)
        } while (randomNumber >= order)
        return randomNumber
    }

    fun vandermonde(participants: List<BigInteger>, threshold: Int, order: BigInteger): Matrix {
        return Matrix(participants.size) { p ->
            Vector(threshold) { t ->
                participants[p].modPow(t.toBigInteger(), order)
            }
        }
    }

    fun dotProduct(matrix: Matrix, vector: Vector, order: BigInteger): Vector {
        val result = Vector(matrix.size) { BigInteger.ZERO }
        matrix.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                result[i] = (result[i] + (value * vector[j])).mod(order)
            }
        }
        return result
    }

    fun recoverSecret(shares: List<Point>, order: BigInteger): BigInteger {
        val van = vandermonde(shares.map { it.x }, shares.size, order)
        val(lu, p) = decomposeLUP(van, order)
        val inverse = invertLUP(lu, p, order)
        return addShares(shares.map { it.y }, inverse[0].asList(), order)
    }

    fun addShares(shares: List<BigInteger>, weights: List<BigInteger>, order: BigInteger): BigInteger {
        return shares.mapIndexed { i, s -> s * weights[i] }.reduce { a, b -> (a + b).mod(order) }
    }

    /*
     *  Division in integers modulus p means finding the inverse of the
     *  denominator modulo p and then multiplying the numerator by this
     *  inverse (Note: inverse of A is B such that A*B % p == 1). This can
     *  be computed via the extended Euclidean algorithm
     *  http://en.wikipedia.org/wiki/Modular_multiplicative_inverse#Computation
     */

    private fun divMod(numerator: BigInteger, denominator: BigInteger, order: BigInteger): BigInteger {
        val inverse = extendedGCD(denominator, order)
        return numerator * inverse
    }

    private fun extendedGCD(aIn: BigInteger, bIn: BigInteger): BigInteger {
        var a = BigInteger(1, aIn.toByteArray())
        var b = BigInteger(1, bIn.toByteArray())
        var x = BigInteger.ZERO
        var lastX = BigInteger.ONE
        var y = BigInteger.ONE
        var lastY = BigInteger.ZERO
        while (b != BigInteger.ZERO) {
            val quotient = a.div(b)
            a = b.also {
                b = a.mod(b)
            }
            lastX = x.also {
                x = lastX - quotient * x
            }
            lastY = y.also {
                y = lastY - quotient * x
            }
        }
        return lastX
    }

    // below inspired by https://en.wikipedia.org/wiki/LU_decomposition#C_code_example
    /*
     * Decomposes matrix into an LUP factorization, returning LU as a single matrix,
     * and P as a vector
     */
    fun decomposeLUP(matrix: Matrix, order: BigInteger): Pair<Matrix, Vector> {
        val n = matrix.size
        matrix.forEach { row -> require(row.size == n) { "Matrix must be square" } }
        val lu = matrix.clone()
        // Unit permutation matrix, p[i] initialized with i
        val p = Vector(n) { i -> BigInteger.valueOf(i.toLong()) }

        (0 until n).forEach { i ->
            var maxA = BigInteger.ZERO
            var iMax = i
            (i until n).forEach { k ->
                val absA = matrix[k][i].abs()
                if (absA > maxA) {
                    maxA = absA
                    iMax = k
                }
            }

            require(maxA > BigInteger.ZERO) { "Matrix is degenerate" }

            if (iMax != i) {
                // pivoting P
                p[i] = p[iMax].also {
                    p[iMax] = p[i]
                }

                // pivoting rows of A
                lu[i] = lu[iMax].also {
                    lu[iMax] = lu[i]
                }
            }

            (i + 1 until n).forEach { j ->
                lu[j][i] = divMod(lu[j][i], lu[i][i], order).mod(order)
                (i + 1 until n).forEach { k ->
                    lu[j][k] = (lu[j][k] - lu[j][i] * lu[i][k]).mod(order)
                }
            }
        }

        return Pair(lu, p)
    }

    /*
     * Takes an LU matrix and a P permutation vector, returns the inverse matrix
     */
    fun invertLUP(lu: Matrix, p: Vector, order: BigInteger): Matrix {
        val n = lu.size
        val inverse = Matrix(n) { Vector(n) { BigInteger.ZERO } }
        (0 until n).forEach { j ->
            (0 until n).forEach { i ->
                inverse[i][j] = if (p[i].toInt() == j) BigInteger.ONE else BigInteger.ZERO
                (0 until i).forEach { k ->
                    inverse[i][j] = inverse[i][j] - lu[i][k] * inverse[k][j]
                    inverse[i][j] = inverse[i][j].mod(order)
                }
            }
            ((n - 1) downTo 0).forEach { i ->
                ((i + 1) until n).forEach { k ->
                    inverse[i][j] = inverse[i][j] - lu[i][k] * inverse[k][j]
                    inverse[i][j] = inverse[i][j].mod(order)
                }
                inverse[i][j] = divMod(inverse[i][j], lu[i][i], order)
                inverse[i][j] = inverse[i][j].mod(order)
            }
        }
        return inverse
    }
}

class SecretSharer(val secret: BigInteger, val threshold: Int, val participants: List<BigInteger>, val order: BigInteger = ORDER) {
    val shards = getShares(participants, threshold, secret)

    fun vandermonde(participants: List<BigInteger>, threshold: Int) = SecretSharerUtils.vandermonde(participants, threshold, order)
    fun dotProduct(matrix: Matrix, vector: Vector) = dotProduct(matrix, vector, order)
    fun invertLUP(lu: Matrix, p: Vector) = SecretSharerUtils.invertLUP(lu, p, order)
    fun decomposeLUP(matrix: Matrix) = SecretSharerUtils.decomposeLUP(matrix, order)
    fun addShards(shares: List<BigInteger>, weights: List<BigInteger>) = SecretSharerUtils.addShares(shares, weights, order)
    fun recoverSecret(shares: List<Point>) = SecretSharerUtils.recoverSecret(shares, order)

    fun getShares(participants: List<BigInteger>, threshold: Int, secret: BigInteger): List<Point> {
        if (threshold > participants.size) {
            throw Exception("secret would be irrecoverable.")
        }
        val vec = Vector(threshold) { if (it == 0) secret else SecretSharerUtils.randomFieldElement(order) }
        val rowShares = dotProduct(vandermonde(participants, threshold), vec)
        return participants.mapIndexed { i, p ->
            Point(p, rowShares[i])
        }
    }

    fun getReshares(newParticipants: List<BigInteger>, newThreshold: Int): List<List<Point>> {
        // generate the reshares from the first threshold participants
        return participants.take(threshold).indices.map { i ->
            getShares(newParticipants, newThreshold, shards[i].y)
        }
    }
}

fun String.toParticipantIdAsBigInteger() = BigInteger(1, EcdsaUtils.getCompressedKeyBytesFromBase58(this, EcdsaUtils.r1Curve))

fun String.toParticipantIdAsHexString() = toParticipantIdAsBigInteger().toByteArrayNoSign(32).toHexString().lowercase()

object EcdsaUtils {
    const val curve = "secp256k1"
    const val r1Curve = "prime256v1"

    private val Algorithm = "SHA256withECDSA"
    private val keyLength = 64
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

    fun getECPublicKey(hexKey: String, curveName: String): ECPublicKey {
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

    fun sha256(input: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(input)
    }

    fun getECPublicKeyFromBase58(base58Key: String, curveName: String): ECPublicKey {
        val bytes = Base58.decode(base58Key)
        return if (bytes.size == 33 || bytes.size == 32) {
            getECPublicKeyFromCompressedBytes(bytes.toHexString(), curveName)
        } else {
            getECPublicKey(bytes.toHexString(), curveName)
        }
    }

    fun getECPublicKeyFromCompressedBytes(hexKey: String, curveName: String): ECPublicKey {
        val spec = ECNamedCurveTable.getParameterSpec(curveName)
        val pubPoint = spec.curve.decodePoint(Hex.decode(hexKey))
        return getECPublicKey(pubPoint.getEncoded(false).toHexString(), curveName)
    }
}

fun BigInteger.toByteArrayNoSign(): ByteArray {
    val byteArray = this.toByteArray()
    return if (byteArray[0].compareTo(0) == 0) {
        byteArray.slice(IntRange(1, byteArray.size - 1)).toByteArray()
    } else byteArray
}