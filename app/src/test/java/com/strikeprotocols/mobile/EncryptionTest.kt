package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManagerImpl
import com.strikeprotocols.mobile.data.SecurePreferences
import com.strikeprotocols.mobile.data.StrikeKeyPair
import org.junit.Assert.assertEquals
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

        val keyPair = encryptionManager.createKeyPair()

        val signData = encryptionManager.signData(data = data, privateKey = keyPair.privateKey)

        val verifyData = encryptionManager.verifyData(
            data = data,
            signature = signData,
            publicKey = keyPair.publicKey
        )

        assertTrue(verifyData)

        val generatedPassword = encryptionManager.generatePassword()
        val encryptedPrivateKey = encryptionManager.encrypt(
            message = keyPair.privateKey, generatedPassword = generatedPassword)

        val verifiedPair = encryptionManager.verifyKeyPair(
            encryptedPrivateKey = BaseWrapper.encode(encryptedPrivateKey),
            publicKey = BaseWrapper.encode(keyPair.publicKey),
            symmetricKey = BaseWrapper.encode(generatedPassword)
        )

        assertTrue(verifiedPair)
    }

    @Test
    fun verifyDataWithEncryptedPrivateAndDecryptionKey() {
        val decryptionKey = BaseWrapper.decode("7mcTo1D27AwncHSqXamvk7zsa21LHCyYFP7BUnN2STwnHWoePZ8W654mkCgq")
        val encryptedKey = BaseWrapper.decode("igPGMATwJEXBXiwVA6bGm547iZ3fGvEcWrwjnjF5fLXCV5BXzRVha4dNat3c3BbyfD2iyoZ3iKyNiwyojx")

        MockitoAnnotations.openMocks(this)

        val encryptionManager = EncryptionManagerImpl(securePreferences)

        val publicKey = encryptionManager.regeneratePublicKey(
            encryptedPrivateKey = BaseWrapper.encode(encryptedKey),
            decryptionKey = BaseWrapper.encode(decryptionKey)
        )

        val publicKeyHardcoded = "BrEiGBArXzSXkNmyTKiNb754qqqHHYi26Dh4F2ypnJR5"

        assertEquals(publicKey, publicKeyHardcoded)

        val decryptedPrivateKey = encryptionManager.decrypt(
            encryptedMessage = encryptedKey,
            generatedPassword = decryptionKey
        )

        val keyPair = StrikeKeyPair(
            privateKey = decryptedPrivateKey,
            publicKey = BaseWrapper.decode(publicKey)
        )

        val signData = encryptionManager.signData(data = data, privateKey = keyPair.privateKey)

        val verifyData = encryptionManager.verifyData(
            data = data,
            signature = signData,
            publicKey = keyPair.publicKey
        )

        assertTrue(verifyData)

        val encryptedPrivateKey = encryptionManager.encrypt(
            message = keyPair.privateKey, generatedPassword = decryptionKey)

        val verifiedPair = encryptionManager.verifyKeyPair(
            encryptedPrivateKey = BaseWrapper.encode(encryptedPrivateKey),
            publicKey = BaseWrapper.encode(keyPair.publicKey),
            symmetricKey = BaseWrapper.encode(decryptionKey)
        )

        assertTrue(verifiedPair)
    }
}