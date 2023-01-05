package com.censocustody.android.common

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.data.EncryptionManagerImpl.Companion.DATA_CHECK
import com.censocustody.android.data.CensoPrivateKey
import org.bitcoinj.core.Base58
import org.bitcoinj.core.ECKey
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequenceGenerator
import org.bouncycastle.asn1.DLSequence
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.util.Properties
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPublicKeySpec
import kotlin.experimental.or

data class ChildPathNumber(
    val index: Int,
    val hardened: Boolean
) {
    companion object {
        fun fromValue(i: Int): ChildPathNumber {
            return ChildPathNumber(
                i and 0x7FFFFFFF,
                i and 0x80000000.toInt() != 0

            )
        }
    }
    fun toBytes(): ByteArray {
        val indexBytes = ByteArray(4)
        ByteBuffer.wrap(indexBytes).order(ByteOrder.BIG_ENDIAN).putInt(index)
        if (hardened) {
            indexBytes[0] = indexBytes[0] or 128.toByte()
        }
        return indexBytes
    }
}

class Secp256k1HierarchicalKey(
    val privateKey: ECPrivateKey?,
    private val chainCode: KeyParameter,
    val depth: Int,
    private val childPathNumber: ChildPathNumber? = null,
    private val parentFingerprint: Int = 0,
    private val publicKey: ByteArray? = null
) : CensoPrivateKey {
    companion object {
        private const val curveName = "secp256k1"
        private val bcProvider = BouncyCastleProvider()
        private val factory = KeyFactory.getInstance("EC", bcProvider)
        private val spec = ECNamedCurveTable.getParameterSpec(curveName)
        private const val bip32HeaderP2PKHpub: Int = 0x0488b21e // The 4 byte header that serializes in base58 to "xpub".
        private const val bip32HeaderP2PKHpriv: Int = 0x0488ade4 // The 4 byte header that serializes in base58 to "xprv"
        private const val bip32HeaderP2PKHpubTest: Int = 0x043587cf // The 4 byte header that serializes in base58 to "tpub".
        private const val bip32HeaderP2PKHprivTest: Int = 0x04358394 // The 4 byte header that serializes in base58 to "tprv"

        val bitcoinDerivationPath = listOf(
            ChildPathNumber(44, true),
            ChildPathNumber(0, true),
            ChildPathNumber(0, true),
            ChildPathNumber(0, false)
        )

        val ethereumDerivationPath = listOf(
            ChildPathNumber(44, true),
            ChildPathNumber(60, true),
            ChildPathNumber(0, true),
            ChildPathNumber(0, false)
        )

        val censoDerivationPath = listOf(
            ChildPathNumber(44, true),
            ChildPathNumber(16743510, true),  // Utils.sha256("censo").substring(0 until 6).toInt(16)
            ChildPathNumber(0, true),
            ChildPathNumber(0, false)
        )

        fun getECPrivateKey(privateKeyBytes: ByteArray): ECPrivateKey {
            return factory.generatePrivate(ECPrivateKeySpec(BigInteger(1, privateKeyBytes), spec)) as ECPrivateKey
        }

        private fun derivableKey(deriveData: ByteArray): Secp256k1HierarchicalKey {
            return Secp256k1HierarchicalKey(
                getECPrivateKey(deriveData.slice(0..31).toByteArray()),
                KeyParameter(deriveData, 32, 32),
                0,
                null
            )
        }

        fun fromExtendedKey(base58ExtendedKey: String) : Secp256k1HierarchicalKey {
            return deserialize(BitcoinUtils.verifyChecksum(Base58.decode(base58ExtendedKey)))
        }

        private fun deserialize(
            serializedKey: ByteArray
        ): Secp256k1HierarchicalKey {
            val buffer = ByteBuffer.wrap(serializedKey)
            val header = buffer.int
            val pub = header == bip32HeaderP2PKHpub || header == bip32HeaderP2PKHpubTest
            val priv = header == bip32HeaderP2PKHpriv || header == bip32HeaderP2PKHprivTest
            if (!priv && !pub) {
                throw Exception("not a known key type")
            }
            val depth = buffer.get().toInt() and 0xFF // convert signed byte to positive int since depth cannot be negative
            val parentFingerprint = buffer.int
            val i = buffer.int
            val childPathNumber = if (i != 0) ChildPathNumber.fromValue(i) else null

            val chainCode = ByteArray(32)
            buffer.get(chainCode)
            val data = ByteArray(33)
            buffer.get(data)
            return if (pub) {
                Secp256k1HierarchicalKey(null, KeyParameter(chainCode), depth, childPathNumber, parentFingerprint, data)
            } else {
                Secp256k1HierarchicalKey(getECPrivateKey(data.slice(1..32).toByteArray()), KeyParameter(chainCode), depth, childPathNumber, parentFingerprint)
            }
        }

        fun fromSeedPhrase(
            seedPhrase: String,
            pathList: List<ChildPathNumber>
        ): Secp256k1HierarchicalKey {
            return fromRootSeed(Mnemonics.MnemonicCode(seedPhrase).toSeed(), pathList)
        }

        fun fromRootSeed(
            rootSeed: ByteArray,
            pathList: List<ChildPathNumber>
        ): Secp256k1HierarchicalKey {
            val hmacSha512 = HMac(SHA512Digest())
            hmacSha512.init(KeyParameter("Bitcoin seed".toByteArray(StandardCharsets.UTF_8)))
            hmacSha512.update(rootSeed, 0, rootSeed.size)
            val derivedState = ByteArray(hmacSha512.macSize)
            hmacSha512.doFinal(derivedState, 0)

            var derivedKey = derivableKey(derivedState)
            pathList.forEach { path ->
                derivedKey = derivedKey.derive(path)
            }
            derivedKey.signData(DATA_CHECK)
            return derivedKey
        }

        fun toDERBytes(r: BigInteger, s: BigInteger): ByteArray {
            // Usually 70-72 bytes.
            val bos = ByteArrayOutputStream(72)
            val seq = DERSequenceGenerator(bos)
            seq.addObject(ASN1Integer(r))
            seq.addObject(ASN1Integer(s))
            seq.close()
            return bos.toByteArray()
        }

        fun decodeFromDER(bytes: ByteArray): Pair<BigInteger, BigInteger> {
            var decoder: ASN1InputStream? = null
            return try {
                // BouncyCastle by default is strict about parsing ASN.1 integers. We relax this check, because some
                // Bitcoin signatures would not parse.
                Properties.setThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer", true)
                decoder = ASN1InputStream(bytes)
                val seqObj = decoder.readObject() ?: throw Exception("Reached past end of ASN.1 stream.")
                if (seqObj !is DLSequence) throw Exception("Read unexpected class: " + seqObj.javaClass.name)
                val seq = seqObj
                val r: ASN1Integer
                val s: ASN1Integer
                try {
                    r = seq.getObjectAt(0) as ASN1Integer
                    s = seq.getObjectAt(1) as ASN1Integer
                } catch (e: ClassCastException) {
                    throw Exception(e)
                }
                // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
                // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
                Pair(r.positiveValue, s.positiveValue)
            } catch (e: IOException) {
                throw Exception(e)
            } finally {
                if (decoder != null) try {
                    decoder.close()
                } catch (x: IOException) {
                }
                Properties.removeThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer")
            }
        }
    }

    private fun getPublicKeyFromPrivate(): ByteArray? {
        return privateKey?.let { getPublicKeyFromPrivate(it) }
    }

    private fun getECPublicKeyFromPrivate(privateKey: ECPrivateKey): ECPublicKey {
        val q = privateKey.parameters.g.multiply(privateKey.d)
        return factory.generatePublic(org.bouncycastle.jce.spec.ECPublicKeySpec(q, privateKey.parameters)) as ECPublicKey
    }

    private fun getPublicKeyFromPrivate(privateKey: ECPrivateKey): ByteArray {
        return getECPublicKeyFromPrivate(privateKey).q.getEncoded(true)
    }

    override fun getPublicKeyBytes(): ByteArray {
        return publicKey ?: getPublicKeyFromPrivate() ?: throw Exception("Cannot calculate public key")
    }

    private fun getIdentifier(): ByteArray {
        return BitcoinUtils.sha256Hash160(getPublicKeyBytes())
    }

    private fun getFingerPrint(): Int {
        return ByteBuffer.wrap(getIdentifier().slice(0..3).toByteArray()).int
    }

    private fun getPrivateKeyBytes(): ByteArray {
        return (privateKey as BCECPrivateKey).d.toByteArrayNoSign(32)
    }

    override fun signData(data: ByteArray): ByteArray {
        val ecdsaSigner = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
        val privKey = ECPrivateKeyParameters(privateKey!!.d, ECKey.CURVE)
        ecdsaSigner.init(true, privKey)
        val (r, s) = ecdsaSigner.generateSignature(data)
        val signature = toDERBytes(r, if (s > ECKey.HALF_CURVE_ORDER) ECKey.CURVE.n - s else s)

        val verifiedSignature = verifySignature(data = data, signature = signature)

        if (!verifiedSignature) {
            throw Exception("Invalid Signature")
        }

        return signature
    }

    override fun verifySignature(data: ByteArray, signature: ByteArray): Boolean {
        val (r, s) = decodeFromDER(signature)
        val ecdsaSigner = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
        val pubKey = ECPublicKeyParameters(getECPublicKey().q, ECKey.CURVE)
        ecdsaSigner.init(false, pubKey)
        return ecdsaSigner.verifySignature(data, r, s)
    }

    fun derive(path: ChildPathNumber): Secp256k1HierarchicalKey {
        val hmacSha512 = HMac(SHA512Digest())
        hmacSha512.init(chainCode)
        val buffer = ByteBuffer.allocate(37)
        val parentPrivKeyBytes = privateKey?.let { getPrivateKeyBytes() }
        if (path.hardened) {
            parentPrivKeyBytes?.let {
                buffer.put(0.toByte())
                buffer.put(it)
            } ?: throw Exception("cannot derive a hardened child with no private key")
        } else {
            buffer.put(getPublicKeyBytes())
        }
        // put the index path in
        buffer.put(path.toBytes())
        hmacSha512.update(buffer.array(), 0, buffer.position())
        val output = ByteArray(64)
        hmacSha512.doFinal(output, 0)

        // calculate the child private key.
        val privKeyInt = BigInteger(1, output.slice(0..31).toByteArray())
        parentPrivKeyBytes?.let {
            val newPrivKey = privKeyInt.add(BigInteger(1, parentPrivKeyBytes)).mod(spec.n)

            return Secp256k1HierarchicalKey(
                getECPrivateKey(newPrivKey.toByteArray()),
                KeyParameter(output, 32, 32),
                this.depth + 1,
                path,
                getFingerPrint()
            )
        } ?: run {
            val pubPoint = spec.curve.decodePoint(getPublicKeyFromPrivate(getECPrivateKey(privKeyInt.toByteArray()))).add(
                spec.curve.decodePoint(getPublicKeyBytes())
            )
            return Secp256k1HierarchicalKey(
                null,
                KeyParameter(output, 32, 32),
                this.depth + 1,
                path,
                getFingerPrint(),
                pubPoint.getEncoded(true)
            )
        }
    }

    fun getECPublicKey(): ECPublicKey {
        val pubPoint = spec.curve.decodePoint(getPublicKeyBytes())
        val params = AlgorithmParameters.getInstance("EC", bcProvider).apply {
            init(ECGenParameterSpec(curveName))
        }
        val pubECSpec = ECPublicKeySpec(
            java.security.spec.ECPoint(pubPoint.xCoord.toBigInteger(), pubPoint.yCoord.toBigInteger()),
            params.getParameterSpec(
                ECParameterSpec::class.java
            )
        )
        return factory.generatePublic(pubECSpec) as ECPublicKey
    }

    private fun serialize(isPrivate: Boolean, isMainNet: Boolean): ByteArray {
        val buffer = ByteBuffer.allocate(78).order(ByteOrder.BIG_ENDIAN)
        if (isPrivate) {
            buffer.putInt(if (isMainNet) bip32HeaderP2PKHpriv else bip32HeaderP2PKHprivTest)
        } else {
            buffer.putInt(if (isMainNet) bip32HeaderP2PKHpub else bip32HeaderP2PKHpubTest)
        }
        buffer.put(depth.toByte())
        buffer.putInt(parentFingerprint)
        if (childPathNumber != null) {
            buffer.put(childPathNumber.toBytes())
        } else {
            buffer.putInt(0)
        }
        buffer.put(chainCode.key)
        if (isPrivate) {
            buffer.put(0.toByte())
            buffer.put(getPrivateKeyBytes())
        } else {
            buffer.put(getPublicKeyBytes())
        }
        return buffer.array()
    }

    private fun serializeToBase58ExtendedKey(isPrivate: Boolean, isMainNet: Boolean): String {
        return Base58.encode(BitcoinUtils.addChecksum(serialize(isPrivate, isMainNet = isMainNet)))
    }

    fun getBase58ExtendedPublicKey(isMainNet: Boolean = true): String {
        return serializeToBase58ExtendedKey(false, isMainNet)
    }

    fun getBase58ExtendedPrivateKey(isMainNet: Boolean = true): String {
        return serializeToBase58ExtendedKey(true, isMainNet)
    }

    fun getBase58UncompressedPublicKey(): String {
        return privateKey?.let { Base58.encode(getECPublicKeyFromPrivate(it).q.getEncoded(false)) }
            ?: throw Exception("No private key to derive public key ")
    }

    override fun toString(): String {
        return "Secp256k1HierarchicalKey(pubKey=${getPublicKeyBytes().toHexString()}, chainCode=${chainCode.key.toHexString()}, depth=$depth, childPath=$childPathNumber, parentFingerprint=$parentFingerprint)".trimMargin()
    }
}
