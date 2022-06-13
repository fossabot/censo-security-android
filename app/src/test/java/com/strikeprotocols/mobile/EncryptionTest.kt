package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManagerImpl
import com.strikeprotocols.mobile.data.SecurePreferences
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class EncryptionTest {

    @Mock
    lateinit var securePreferences: SecurePreferences

    val data = BaseWrapper.decode("VerificationCheck")

    @Test
    fun testCanVerifyKeyPair() {
        MockitoAnnotations.openMocks(this)

        val encryptionManager = EncryptionManagerImpl(securePreferences)

        val phrase = encryptionManager.generatePhrase()
        val keyPair = encryptionManager.createKeyPair(phrase)

        val signData = encryptionManager.signData(data = data, privateKey = keyPair.privateKey)

        val verifyData = encryptionManager.verifyData(
            data = data,
            signature = signData,
            publicKey = keyPair.publicKey
        )

        assertTrue(verifyData)

        val verifiedPair = encryptionManager.verifyKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = BaseWrapper.encode(keyPair.publicKey),
        )

        assertTrue(verifiedPair)
    }

    @Test
    fun verifyRegeneratedPublicKeyMatchesOriginalPrivateKey() {
        MockitoAnnotations.openMocks(this)

        val encryptionManager = EncryptionManagerImpl(securePreferences)

        val phrase = encryptionManager.generatePhrase()
        val keyPair = encryptionManager.createKeyPair(phrase)

        val regeneratedPublicKey = encryptionManager.regeneratePublicKey(
            mainKey = BaseWrapper.encode(keyPair.privateKey)
        )

        val signData = encryptionManager.signData(data = data, privateKey = keyPair.privateKey)

        val verifyData = encryptionManager.verifyData(
            data = data,
            signature = signData,
            publicKey = BaseWrapper.decode(regeneratedPublicKey)
        )

        assertTrue(verifyData)

        val verifiedPair = encryptionManager.verifyKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = regeneratedPublicKey,
        )

        assertTrue(verifiedPair)
    }
}
