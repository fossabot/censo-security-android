package com.strikeprotocols.mobile

import com.google.gson.JsonParser
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.EncryptionManagerImpl
import com.strikeprotocols.mobile.data.SecurePreferences
import com.strikeprotocols.mobile.data.StrikeKeyPair
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.WalletSigner.Companion.WALLET_TYPE_SOLANA
import com.strikeprotocols.mobile.data.models.approval.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class ApprovalRequestSigningTest {

    private val deserializer = WalletApprovalDeserializer()

    @Mock
    lateinit var securePreferences: SecurePreferences

    private lateinit var encryptionManager: EncryptionManager

    private val exampleNonces =
        listOf(
            Nonce("GfNRNaKxa1dND25d5TcMnVUewqQEoXKtXmF1DDGHPBVH"),
            Nonce("GfNRNaKxa1dND25d5TcMnVUewqQEoXKtXmF1DDGHPBVH")
        )

    private val userEmail = "floater@test887123.com"

    private lateinit var keyPair: StrikeKeyPair

    private lateinit var approverPublicKey: String

    private lateinit var encryptedPrivateKey: ByteArray
    private lateinit var decyptionKey: ByteArray

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        encryptionManager = EncryptionManagerImpl(securePreferences)
        keyPair = encryptionManager.createKeyPair()

        decyptionKey = encryptionManager.generatePassword()
        encryptedPrivateKey = encryptionManager.encrypt(
            message = keyPair.privateKey,
            generatedPassword = decyptionKey
        )

        whenever(securePreferences.retrievePrivateKey(userEmail)).then {
            BaseWrapper.encode(encryptedPrivateKey)
        }

        whenever(securePreferences.retrieveGeneratedPassword(userEmail)).then {
            BaseWrapper.encode(decyptionKey)
        }

        approverPublicKey = BaseWrapper.encode(keyPair.publicKey)
    }

    private fun generateSignBalanceAccountCreationSignableData(): ByteArray {
        val balanceAccountCreationWalletApproval =
            deserializer.parseData(JsonParser.parseString(balanceAccountCreationJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = balanceAccountCreationWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = balanceAccountCreationWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey)

        println("BalanceAccount Signed Data: $signableData")

        return signableData
    }

    private fun generateLoginApprovalSignableData(): ByteArray {
        val loginApprovalWalletApproval =
            deserializer.parseData(JsonParser.parseString(loginApprovalJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = loginApprovalWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = loginApprovalWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey)

        println("Login Approval: $signableData")

        return signableData
    }

    private fun generateSignWithdrawalRequestSignableData(): ByteArray {
        val withdrawalRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(withdrawalRequestJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = withdrawalRequestWalletApproval.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestType = withdrawalRequestWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey)

        println("WithdrawalRequest Signed Data: $signableData")

        return signableData
    }

    private fun generateSignConversionRequestSignableData(): ByteArray {
        val conversionRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(conversionRequestJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = conversionRequestWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = conversionRequestWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey)

        println("ConversionRequest Signed Data: $signableData")

        return signableData
    }

    private fun generateSignSignersUpdateSignableData(): ByteArray {
        val signersUpdateWalletApproval =
            deserializer.parseData(JsonParser.parseString(signersUpdateJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = signersUpdateWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = signersUpdateWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey)

        println("SignersUpdate Signed Data: $signableData")

        return signableData
    }

    private fun generateSignersUpdateRemovalSignableData(): ByteArray {
        val signersUpdateRemovalWalletApproval =
            deserializer.parseData(JsonParser.parseString(signersUpdateRemovalJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = signersUpdateRemovalWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = signersUpdateRemovalWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey)

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
    fun testBalanceAccountCreationSignableDataIsNotNull() {
        val balanceAccountCreationSignableData = generateSignBalanceAccountCreationSignableData()
        assertNotNull(balanceAccountCreationSignableData)
    }

    @Test
    fun testLoginApprovalIsNotNull() {
        val loginApprovalSignableData = generateLoginApprovalSignableData()
        assertNotNull(loginApprovalSignableData)
        assertEquals(loginApprovalSignableData.toString(Charsets.UTF_8), EXAMPLE_JWT_TOKEN)
    }

    @Test
    fun generateSignatureForBalanceAccountCreation() {
        val balanceAccountCreationWalletApproval =
            deserializer.parseData(JsonParser.parseString(balanceAccountCreationJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = balanceAccountCreationWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = balanceAccountCreationWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, userEmail = userEmail
        )

        println("Signature from balance account creation: $signature")

        assertNotNull(signature)
    }

    @Test
    fun generateSignatureForWithdrawalRequest() {
        val withdrawalRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(withdrawalRequestJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = withdrawalRequestWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = withdrawalRequestWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, userEmail = userEmail
        )

        println("Signature from withdrawal request: $signature")

        assertNotNull(signature)
    }

    @Test
    fun generateSignatureForConversionRequest() {
        val conversionRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(conversionRequestJson.trim()))


        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = conversionRequestWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = conversionRequestWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, userEmail = userEmail
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
            requestType = signersUpdateWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, userEmail = userEmail
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
            requestType = signersUpdateRemovalWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, userEmail = userEmail
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
            requestType = loginApprovalWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, userEmail = userEmail
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
            requestType = dappTransactionWalletApproval.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("Bf1znCzN6V7tczCXfBXZvfBGFimSV79kZD513E2ZiCap")),
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, userEmail = userEmail
        )

        println("Signature from dapp transaction: $signature")

        assertNotNull(signature)
    }
}
