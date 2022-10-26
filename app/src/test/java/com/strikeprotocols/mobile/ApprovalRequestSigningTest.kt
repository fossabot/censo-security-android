package com.strikeprotocols.mobile

import cash.z.ecc.android.bip39.Mnemonics
import com.google.gson.JsonParser
import com.nhaarman.mockitokotlin2.*
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.StoredKeyData
import com.strikeprotocols.mobile.data.models.approval.*
import org.bitcoinj.core.Base58
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*
import javax.crypto.Cipher

class ApprovalRequestSigningTest {

    private val deserializer = ApprovalRequestDeserializer()

    @Mock
    lateinit var securePreferences: SecurePreferences

    @Mock
    lateinit var cryptographyManager: CryptographyManager

    private lateinit var encryptionManager: EncryptionManager

    private val exampleNonces =
        listOf(
            Nonce("GfNRNaKxa1dND25d5TcMnVUewqQEoXKtXmF1DDGHPBVH"),
            Nonce("GfNRNaKxa1dND25d5TcMnVUewqQEoXKtXmF1DDGHPBVH")
        )

    private val userEmail = "floater@test887123.com"

    private lateinit var keyPair: StrikeKeyPair

    private lateinit var base58EncodedPrivateKey: String

    private lateinit var approverPublicKey: String

    private lateinit var phrase: String

    private val mockEncryptionManager = mock<EncryptionManager>()
    private val cipherMock = mock<Cipher>()

    val bitcoinExtendedPrivateKey = "tprv8igBmKYoTNej2GHV2ZvfQ3eJM9yAeDoMs8pTDqJLR1EzCHJc42QrxLGuh6Hh5b248yzeC5DAWyby76b9rbhL7L7GJuAeXY1k7yiYyjajcW4"
    val ethereumExtendedPrivateKey = "tprv8igBmKYoTNej2GHV2ZvfQ3eJM9yAeDoMs8pTDqJLR1EzCHJc42QrxLGuh6Hh5b248yzeC5DAWyby76b9rbhL7L7GJuAeXY1k7yiYyjajcW4"

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        encryptionManager = EncryptionManagerImpl(securePreferences, cryptographyManager)

        phrase = encryptionManager.generatePhrase()

        keyPair = encryptionManager.createKeyPair(Mnemonics.MnemonicCode(phrase = phrase))

        base58EncodedPrivateKey = BaseWrapper.encode(keyPair.privateKey)

        whenever(securePreferences.retrieveEncryptedStoredKeys(userEmail)).then {
            BaseWrapper.encode(keyPair.privateKey)
        }

        approverPublicKey = BaseWrapper.encode(keyPair.publicKey)

        reset(mockEncryptionManager)
    }

    private fun generateSolanaWalletCreationSignableData(): ByteArray {
        val walletCreationApprovalRequest =
            deserializer.parseData(JsonParser.parseString(solanaWalletCreationJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = walletCreationApprovalRequest.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = walletCreationApprovalRequest.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey).first()

        println("Solana Wallet Creation: $signableData")

        return signableData
    }

    private fun generateBitcoinWalletCreationSignableData(): ByteArray {
        val walletCreationApprovalRequest =
            deserializer.parseData(JsonParser.parseString(bitcoinWalletCreationJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = walletCreationApprovalRequest.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = walletCreationApprovalRequest.getApprovalRequestType(),
            nonces = emptyList(),
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey).first()

        println("Bitcoin Wallet Creation: $signableData")

        return signableData
    }

    private fun generateEthereumWalletCreationSignableData(): ByteArray {
        val walletCreationApprovalRequest =
            deserializer.parseData(JsonParser.parseString(ethereumWalletCreationJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = walletCreationApprovalRequest.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = walletCreationApprovalRequest.getApprovalRequestType(),
            nonces = emptyList(),
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey).first()

        println("Ethereum Wallet Creation: $signableData")

        return signableData
    }

    private fun generateLoginApprovalSignableData(): ByteArray {
        val loginApprovalWalletApproval =
            deserializer.parseData(JsonParser.parseString(loginApprovalJson.trim()))

        val disposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY
        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = loginApprovalWalletApproval.id!!,
            approvalDisposition = disposition,
            requestType = loginApprovalWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey).first()

        println("Login Approval: $signableData")

        verifyNoChainApiBody(approvalDispositionRequest, disposition)

        return signableData
    }

    private fun verifyNoChainApiBody(approvalDispositionRequest: ApprovalDispositionRequest, disposition: ApprovalDisposition) {
        whenever(mockEncryptionManager.retrieveSavedKey(any(), any(), any())).thenReturn(Base58.decode(base58EncodedPrivateKey))
        whenever(mockEncryptionManager.signApprovalDispositionMessage(any(), any())).thenReturn(SignedPayload(signature = "someSignature", payload = "somePayload"))
        val apiBody = approvalDispositionRequest.convertToApiBody(mockEncryptionManager, cipherMock)
        assertEquals(disposition, apiBody.approvalDisposition)
        assertEquals(
            ApprovalSignature.NoChainSignature(signature = "someSignature", signedData = "somePayload"),
            apiBody.signatureInfo
        )

        verify(mockEncryptionManager, times(1)).retrieveSavedKey(
            "floater@test887123.com",
            cipherMock,
            StoredKeyData.SOLANA_KEY
        )

        verify(mockEncryptionManager, times(1)).signApprovalDispositionMessage(
            approvalDispositionRequest,
            base58EncodedPrivateKey,
        )
    }

    private fun generateSignWithdrawalRequestSignableData(): ByteArray {
        val withdrawalRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(withdrawalRequestJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = withdrawalRequestWalletApproval.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestType = withdrawalRequestWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey).first()

        println("WithdrawalRequest Signed Data: $signableData")

        return signableData
    }

    private fun generateSignConversionRequestSignableData(): ByteArray {
        val conversionRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(conversionRequestJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = conversionRequestWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = conversionRequestWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey).first()

        println("ConversionRequest Signed Data: $signableData")

        return signableData
    }

    private fun generateSignSignersUpdateSignableData(): ByteArray {
        val signersUpdateWalletApproval =
            deserializer.parseData(JsonParser.parseString(signersUpdateJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = signersUpdateWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = signersUpdateWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey).first()

        println("SignersUpdate Signed Data: $signableData")

        return signableData
    }

    private fun generateSignersUpdateRemovalSignableData(): ByteArray {
        val signersUpdateRemovalWalletApproval =
            deserializer.parseData(JsonParser.parseString(signersUpdateRemovalJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = signersUpdateRemovalWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = signersUpdateRemovalWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey).first()

        println("SignersUpdateRemoval Signed Data: $signableData")

        return signableData
    }

    @Test
    fun testSignersUpdateRemovalSignableDataIsNotNull() {
        val signersUpdateRemovalSignableData = generateSignersUpdateRemovalSignableData()
        assertNotNull(signersUpdateRemovalSignableData)
    }

    @Test
    fun testSignersUpdateSignableDataIsNotNull() {
        val signersUpdateSignableData = generateSignSignersUpdateSignableData()
        assertNotNull(signersUpdateSignableData)
    }

    @Test
    fun testConversionRequestSignableDataIsNotNull() {
        val conversionRequestSignableData = generateSignConversionRequestSignableData()
        assertNotNull(conversionRequestSignableData)
    }

    @Test
    fun testWithdrawalRequestSignableDataIsNotNull() {
        val withdrawalRequestSignableData = generateSignWithdrawalRequestSignableData()
        assertNotNull(withdrawalRequestSignableData)
    }

    @Test
    fun testSolanaWalletCreationSignableDataIsNotNull() {
        val walletCreationSignableData = generateSolanaWalletCreationSignableData()
        assertNotNull(walletCreationSignableData)
    }

    @Test
    fun testBitcoinWalletCreationSignableDataIsNotNull() {
        val walletCreationSignableData = generateBitcoinWalletCreationSignableData()
        assertNotNull(walletCreationSignableData)
    }

    @Test
    fun testEthereumWalletCreationSignableDataIsNotNull() {
        val walletCreationSignableData = generateEthereumWalletCreationSignableData()
        assertNotNull(walletCreationSignableData)
    }

    @Test
    fun testLoginApprovalIsNotNull() {
        val loginApprovalSignableData = generateLoginApprovalSignableData()
        assertNotNull(loginApprovalSignableData)
        assertEquals(loginApprovalSignableData.toString(Charsets.UTF_8), EXAMPLE_JWT_TOKEN)
    }

    @Test
    fun generateSignatureForSolanaWalletCreation() {
        val walletCreationApprovalRequest =
            deserializer.parseData(JsonParser.parseString(solanaWalletCreationJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = walletCreationApprovalRequest.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = walletCreationApprovalRequest.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, solanaKey = base58EncodedPrivateKey
        )

        println("Signature from Solana wallet creation: $signature")

        assertNotNull(signature)
    }

    @Test
    fun generateSignatureForBitcoinWalletCreation() {
        val walletCreationApprovalRequest =
            deserializer.parseData(JsonParser.parseString(bitcoinWalletCreationJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = walletCreationApprovalRequest.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = walletCreationApprovalRequest.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signatures = encryptionManager.signBitcoinApprovalDispositionMessage(
            signable = approvalDispositionRequest, bitcoinKey = bitcoinExtendedPrivateKey
        )

        assertEquals(1, signatures.size)
        val signature = signatures[0]

        println("Signature from Bitcoin wallet creation: $signature")

        assertNotNull(signature)
    }

    @Test
    fun generateSignatureForEthereumWalletCreation() {
        val walletCreationApprovalRequest =
            deserializer.parseData(JsonParser.parseString(ethereumWalletCreationJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = walletCreationApprovalRequest.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = walletCreationApprovalRequest.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signEthereumApprovalDispositionMessage(
            signable = approvalDispositionRequest, ethereumKey = ethereumExtendedPrivateKey
        )

        println("Signature from Ethereum wallet creation: $signature")

        assertNotNull(signature)
    }

    @Test
    fun generateSignatureForWithdrawalRequest() {
        val withdrawalRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(withdrawalRequestJson.trim()))

        val disposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY
        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = withdrawalRequestWalletApproval.id!!,
            approvalDisposition = disposition,
            requestType = withdrawalRequestWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, solanaKey = base58EncodedPrivateKey
        )

        println("Signature from withdrawal request: $signature")

        assertNotNull(signature)

        whenever(mockEncryptionManager.retrieveSavedKey(any(), any(), any())).thenReturn(Base58.decode(base58EncodedPrivateKey))
        whenever(mockEncryptionManager.signApprovalDispositionMessage(any(), any())).thenReturn(
            SignedPayload(signature = "someSignature", payload = ""))
        val apiBody = approvalDispositionRequest.convertToApiBody(mockEncryptionManager, cipherMock)
        assertEquals(disposition, apiBody.approvalDisposition)
        assertEquals(
            ApprovalSignature.SolanaSignature("someSignature", exampleNonces.first().value, "57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"),
            apiBody.signatureInfo
        )

        verify(mockEncryptionManager, times(1)).retrieveSavedKey(
            "floater@test887123.com",
            cipherMock,
            StoredKeyData.SOLANA_KEY
        )

        verify(mockEncryptionManager, times(1)).signApprovalDispositionMessage(
            approvalDispositionRequest,
            base58EncodedPrivateKey,
        )
    }

    @Test
    fun generateSignatureForBitcoinWithdrawalRequest() {
        val withdrawalRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(bitcoinWithdrawalRequestJson.trim()))

        val disposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY
        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = withdrawalRequestWalletApproval.id!!,
            approvalDisposition = disposition,
            requestType = withdrawalRequestWalletApproval.getApprovalRequestType(),
            nonces = listOf(),
            email = userEmail
        )

        val signatures = encryptionManager.signBitcoinApprovalDispositionMessage(
            signable = approvalDispositionRequest, bitcoinKey = bitcoinExtendedPrivateKey, childKeyIndex = 0
        )

        val expectedSignatures = listOf(
            SignedPayload(signature = "MEQCIBiSprONqD6ejJ+DnFMBO4J/XuBB0+g1AZbsVhAwVvOxAiAzxIvPIbILK1T3ansYRN64F16OTfxVBV6r+W878BWWUg==", payload = "OdGGt+Yh9Fp1wjHVGwpbmVJnjWOdFnZpeC+F9l+HG8g="),
            SignedPayload(signature = "MEQCIGrIRsSweu+vxPD7bf3cTQQeTKGK6dL3y0bhixPFLdGAAiAS5Ryvpch7KusXaAqMGkmkFt/IzgdpJ2LHTWzXSuPxOw==", payload = "AnxjVJFcoqK4y3URV/UMI4F9jIER5bb2cDI+TtkMtMc="),
            SignedPayload(signature = "MEMCH19ac8Hpx+fKnzyZSxjFR6uRHnTgyYOa3J995/jAor0CIDJT47QNi9BdGOQBLSApVwAWYxNPlgamvrSbkn7i5uI1", payload = "m73uZw7PDXkA5VzKxDEqdT+AMT2vwGvVcjzlRyd4a6E=")
        )

        assertEquals(
            expectedSignatures,
            signatures,
        )

        whenever(mockEncryptionManager.retrieveSavedKey(any(), any(), any())).thenReturn(Base58.decode(bitcoinExtendedPrivateKey))
        whenever(mockEncryptionManager.signBitcoinApprovalDispositionMessage(any(), any(), any())).thenReturn(expectedSignatures)
        val apiBody = approvalDispositionRequest.convertToApiBody(mockEncryptionManager, cipherMock)
        assertEquals(disposition, apiBody.approvalDisposition)
        assertEquals(ApprovalSignature.BitcoinSignatures(expectedSignatures.map { it.signature}), apiBody.signatureInfo)

        verify(mockEncryptionManager, times(1)).retrieveSavedKey(
            "floater@test887123.com",
            cipherMock,
            StoredKeyData.BITCOIN_KEY
        )

        verify(mockEncryptionManager, times(1)).signBitcoinApprovalDispositionMessage(
            approvalDispositionRequest,
            bitcoinExtendedPrivateKey,
            0
        )
    }

    @Test
    fun generateSignatureForConversionRequest() {
        val conversionRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(conversionRequestJson.trim()))


        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = conversionRequestWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = conversionRequestWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, solanaKey = base58EncodedPrivateKey
        )

        println("Signature from conversion request: $signature")

        assertNotNull(signature)
    }

    @Test
    fun generateSignatureForSignersUpdate() {
        val signersUpdateWalletApproval =
            deserializer.parseData(JsonParser.parseString(signersUpdateJson.trim()))


        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = signersUpdateWalletApproval.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestType = signersUpdateWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, solanaKey = base58EncodedPrivateKey
        )

        println("Signature from signers update: $signature")

        assertNotNull(signature)
    }

    @Test
    fun generateSignatureForSignersUpdateRemoval() {
        val signersUpdateRemovalWalletApproval =
            deserializer.parseData(JsonParser.parseString(signersUpdateRemovalJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = signersUpdateRemovalWalletApproval.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestType = signersUpdateRemovalWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, solanaKey = base58EncodedPrivateKey
        )

        println("Signature from signers update removal: $signature")

        assertNotNull(signature)
    }

    @Test
    fun generateSignatureForLoginApproval() {
        val loginApprovalWalletApproval =
            deserializer.parseData(JsonParser.parseString(loginApprovalJson.trim()))


        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = loginApprovalWalletApproval.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestType = loginApprovalWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, solanaKey = base58EncodedPrivateKey
        )

        println("Signature from login approval: $signature")

        assertNotNull(signature)
    }

    @Test
    fun generateSignatureForDappTransaction() {
        val dappTransactionWalletApproval =
            deserializer.parseData(JsonParser.parseString(dappTransactionJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = dappTransactionWalletApproval.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestType = dappTransactionWalletApproval.getApprovalRequestType(),
            nonces = listOf(Nonce("Bf1znCzN6V7tczCXfBXZvfBGFimSV79kZD513E2ZiCap")),
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, solanaKey = base58EncodedPrivateKey
        )

        println("Signature from dapp transaction: $signature")

        assertNotNull(signature)
    }
}

