package com.strikeprotocols.mobile

import cash.z.ecc.android.bip39.Mnemonics
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.CryptographyManager
import com.strikeprotocols.mobile.data.EncryptionManagerImpl
import com.strikeprotocols.mobile.data.SecurePreferences
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class EncryptionTest {

    @Mock
    lateinit var securePreferences: SecurePreferences

    @Mock
    lateinit var cryptographyManager: CryptographyManager

    val data = BaseWrapper.decode("VerificationCheck")

    @Test
    fun testCanVerifyKeyPair() {
        MockitoAnnotations.openMocks(this)

        val encryptionManager = EncryptionManagerImpl(securePreferences, cryptographyManager)

        val phrase = encryptionManager.generatePhrase()
        val keyPair = createSolanaKeyPairFromMnemonic(Mnemonics.MnemonicCode(phrase = phrase))

        val signData = encryptionManager.signDataWithSolana(data = data, privateKey = keyPair.privateKey)

        val verifyData = encryptionManager.verifyData(
            data = data,
            signature = signData,
            publicKey = keyPair.publicKey
        )

        assertTrue(verifyData)

        val verifiedPair = encryptionManager.verifySolanaKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = BaseWrapper.encode(keyPair.publicKey),
        )

        assertTrue(verifiedPair)
    }

    @Test
    fun verifyRegeneratedPublicKeyMatchesOriginalPrivateKey() {
        MockitoAnnotations.openMocks(this)

        val encryptionManager = EncryptionManagerImpl(securePreferences, cryptographyManager)

        val phrase = encryptionManager.generatePhrase()
        val keyPair = createSolanaKeyPairFromMnemonic(Mnemonics.MnemonicCode(phrase = phrase))

        val regeneratedPublicKey = encryptionManager.regenerateSolanaPublicKey(
            privateKey = BaseWrapper.encode(keyPair.privateKey)
        )

        val signData = encryptionManager.signDataWithSolana(data = data, privateKey = keyPair.privateKey)

        val verifyData = encryptionManager.verifyData(
            data = data,
            signature = signData,
            publicKey = BaseWrapper.decode(regeneratedPublicKey)
        )

        assertTrue(verifyData)

        val verifiedPair = encryptionManager.verifySolanaKeyPair(
            privateKey = BaseWrapper.encode(keyPair.privateKey),
            publicKey = regeneratedPublicKey,
        )

        assertTrue(verifiedPair)
    }
}
