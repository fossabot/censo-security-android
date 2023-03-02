package com.censocustody.android

import com.censocustody.android.data.ECIESManager
import junit.framework.Assert.assertEquals
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Test
import java.security.KeyPairGenerator

class ECIESTest {

    @Test
    fun encryptAndDecryptFlow() {
        val eciesManager = ECIESManager()

        val kpg = KeyPairGenerator.getInstance(ECIESManager.EC, BouncyCastleProvider())
        val secp256r1 = ECNamedCurveTable.getParameterSpec(ECIESManager.SECP_256_R1)
        kpg.initialize(secp256r1)

        val keyPair = kpg.generateKeyPair()
        val publicKey = eciesManager.shortenedDevicePublicKey(keyPair.public.encoded)

        val plainTextValues = listOf(
            "Whatsupppppppppp",
            "a decently long one that runs on for quite a long time and let us see about this and that over there, and this is getting more drawn out and then we are seeing what to do around here also",
            "jkghf4536789guihjvbn  @$%#^$&%*^",
            "097865432678,hmgfdshjKJHGFDSFHJjkhgfjvbmn!@#$%^&*KLJHGFDCVBNMJHMN"
        )

        for (plainText in plainTextValues) {
            val encryptedData = eciesManager.encryptMessage(
                textToEncrypt = plainText,
                publicKeyBytes = publicKey
            )

            val decryptedData = eciesManager.decryptMessage(
                cipherData = encryptedData,
                privateKey = keyPair.private
            )

            assertEquals(plainText, decryptedData)
        }
    }
}