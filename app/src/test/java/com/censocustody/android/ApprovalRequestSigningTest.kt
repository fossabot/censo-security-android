package com.censocustody.android

import androidx.biometric.BiometricPrompt.CryptoObject
import cash.z.ecc.android.bip39.Mnemonics
import com.google.gson.JsonParser
import com.nhaarman.mockitokotlin2.*
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.data.*
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.Nonce
import com.censocustody.android.data.models.approval.*
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

    private lateinit var keyPair: TestKeyPair
    private lateinit var rootSeed: ByteArray

    private lateinit var approverPublicKey: String

    val phrase: String = "beach ready language deny critic reason unveil neck ability heart cloth flock security retire misery toast twin crane genius frequent path retire truck child"

    val fakeCipherText = BaseWrapper.decode("ciphertext")

    private val mockEncryptionManager = mock<EncryptionManager>()
    private val cipherMock = mock<Cipher>()
    private val cryptoMock = mock<CryptoObject>()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        encryptionManager = EncryptionManagerImpl(securePreferences, cryptographyManager)

        keyPair = createSolanaKeyPairFromMnemonic(Mnemonics.MnemonicCode(phrase = phrase))
        rootSeed = createRootSeedFromMnemonic(Mnemonics.MnemonicCode(phrase = phrase))

        whenever(securePreferences.retrieveV3RootSeed(userEmail)).then {
            EncryptedData(initializationVector = byteArrayOf(), ciphertext = fakeCipherText)
        }

        whenever(cryptographyManager.decryptData(fakeCipherText, cipherMock)).then { rootSeed }

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
        val loginApprovalWalletApproval =
            deserializer.parseData(JsonParser.parseString(loginApprovalJson.trim()))

        val disposition =
            if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY
        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = loginApprovalWalletApproval.id!!,
            approvalDisposition = disposition,
            requestType = loginApprovalWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val loginApprovalSignableData =
            approvalDispositionRequest.retrieveSignableData(approverPublicKey).first()

        whenever(mockEncryptionManager.retrieveRootSeed(any(), any())).thenReturn(BaseWrapper.encode(rootSeed))

        whenever(mockEncryptionManager.signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest,
            email = userEmail,
            cipher = cipherMock,
        )).then { SignedPayload(signature = "someSignature", payload = "somePayload")  }
        val apiBody = approvalDispositionRequest.convertToApiBody(mockEncryptionManager, cryptoMock)
        assertEquals(disposition, apiBody.approvalDisposition)
        assertEquals(
            ApprovalSignature.NoChainSignature(signature = "someSignature", signedData = "somePayload"),
            apiBody.signatureInfo
        )

        verify(mockEncryptionManager, times(1)).signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest,
            email = userEmail,
            cipher = cipherMock
        )

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

        val signature = encryptionManager.signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), email = userEmail
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
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), email = userEmail
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
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), email = userEmail
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

        val signature = encryptionManager.signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), email = userEmail
        )

        println("Signature from withdrawal request: $signature")

        assertNotNull(signature)

        whenever(mockEncryptionManager.signSolanaApprovalDispositionMessage(signable = approvalDispositionRequest, email = userEmail, cipher = cipherMock))
            .thenReturn(SignedPayload(signature = "someSignature", payload = ""))
        val apiBody = approvalDispositionRequest.convertToApiBody(mockEncryptionManager, cryptoMock)
        assertEquals(disposition, apiBody.approvalDisposition)
        assertEquals(
            ApprovalSignature.SolanaSignature("someSignature", exampleNonces.first().value, "57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"),
            apiBody.signatureInfo
        )

        verify(mockEncryptionManager, times(1)).signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest,
            cipher = cipherMock,
            email = userEmail,
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
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), childKeyIndex = 0, email = userEmail
        )

        val expectedSignatures = listOf(
            SignedPayload(signature = "MEQCIFDhGGa9+2pPFV4atq6yudbZxcbU79zu2PSab9WMPILtAiBu2qiHXvkfyJjbTtr3HqeHwVEjdmDXSTdEX1egEiS0gQ==", payload = "OdGGt+Yh9Fp1wjHVGwpbmVJnjWOdFnZpeC+F9l+HG8g="),
            SignedPayload(signature = "MEUCIQDzkwPOaeeVDaDloncG7TWxKyC8q+dbkN8mJuRfTUnBXwIgGaS7GijUZ5SXpW7uC7GF0QLiR0zNDbKxB83rBLJQHRM=", payload = "AnxjVJFcoqK4y3URV/UMI4F9jIER5bb2cDI+TtkMtMc="),
            SignedPayload(signature = "MEUCIQCwiImi6BFRD81JT/p1c38hHpo4ovmyspSMOLXg0vA3tAIgCqAdk51t2QfLIHVMh22TGlxyVYTdqW4obTdP3jI8sQg=", payload = "m73uZw7PDXkA5VzKxDEqdT+AMT2vwGvVcjzlRyd4a6E=")
        )

        assertEquals(expectedSignatures, signatures)

        whenever(mockEncryptionManager.signBitcoinApprovalDispositionMessage(
            signable = approvalDispositionRequest, cipher = cipherMock, email = userEmail, childKeyIndex = 0)).thenReturn(expectedSignatures)
        val apiBody = approvalDispositionRequest.convertToApiBody(mockEncryptionManager, cryptoMock)
        assertEquals(disposition, apiBody.approvalDisposition)
        assertEquals(ApprovalSignature.BitcoinSignatures(expectedSignatures.map { it.signature}), apiBody.signatureInfo)

        verify(mockEncryptionManager, times(1)).signBitcoinApprovalDispositionMessage(
            signable = approvalDispositionRequest,
            cipher = cipherMock,
            email = userEmail,
            childKeyIndex = 0
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

        val signature = encryptionManager.signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), email = userEmail
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

        val signature = encryptionManager.signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), email = userEmail
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

        val signature = encryptionManager.signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), email = userEmail
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

        val signature = encryptionManager.signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), email = userEmail
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

        val signature = encryptionManager.signSolanaApprovalDispositionMessage(
            signable = approvalDispositionRequest, rootSeed = BaseWrapper.encode(rootSeed), email = userEmail
        )

        println("Signature from dapp transaction: $signature")

        assertNotNull(signature)
    }
}