package com.censocustody.android

import com.censocustody.android.data.cryptography.ECIESManager
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.crypto.AEADBadTagException
import org.junit.Assert.assertEquals

class ECIESTest {

    @Test
    fun encryptAndDecryptFlow() {
        val keyPair = createSecp256R1KeyPair()
        val publicKey = ECIESManager.extractUncompressedPublicKey(keyPair.public.encoded)

        val plainTextValues = listOf(
            "Whatsupppppppppp",
            "a decently long one that runs on for quite a long time and let us see about this and that over there, and this is getting more drawn out and then we are seeing what to do around here also",
            "jkghf4536789guihjvbn  @$%#^$&%*^",
            "097865432678,hmgfdshjKJHGFDSFHJjkhgfjvbmn!@#$%^&*KLJHGFDCVBNMJHMN"
        )

        for (plainText in plainTextValues) {
            val encryptedData = ECIESManager.encryptMessage(
                dataToEncrypt = plainText.toByteArray(Charsets.UTF_8),
                publicKeyBytes = publicKey
            )

            val decryptedData = ECIESManager.decryptMessage(
                cipherData = encryptedData,
                privateKey = keyPair.private
            )

            assertEquals(plainText, String(decryptedData, Charsets.UTF_8))
        }
    }

    @Test(expected = AEADBadTagException::class)
    fun cannotDecryptWithWrongKey() {
        val keyPair = createSecp256R1KeyPair()
        val publicKey = ECIESManager.extractUncompressedPublicKey(keyPair.public.encoded)

        val otherKeyPair = createSecp256R1KeyPair()

        val plainText = "Whatsupppppppppp"

        val encryptedData = ECIESManager.encryptMessage(
            dataToEncrypt = plainText.toByteArray(Charsets.UTF_8),
            publicKeyBytes = publicKey
        )

        ECIESManager.decryptMessage(
            cipherData = encryptedData,
            privateKey = otherKeyPair.private
        )
    }

    private fun createSecp256R1KeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("EC", BouncyCastleProvider())
        val secp256r1 = ECNamedCurveTable.getParameterSpec("secp256r1")
        kpg.initialize(secp256r1)

        return kpg.generateKeyPair()
    }
}