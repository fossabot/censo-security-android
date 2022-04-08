package com.strikeprotocols.mobile

import com.google.gson.JsonParser
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.EncryptionManagerImpl
import com.strikeprotocols.mobile.data.SecurePreferences
import com.strikeprotocols.mobile.data.StrikeKeyPair
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.ApprovalDispositionRequest
import com.strikeprotocols.mobile.data.models.approval.WalletApprovalDeserializer
import junit.framework.Assert.assertNotNull
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

    private val exampleBlockHash = "GfNRNaKxa1dND25d5TcMnVUewqQEoXKtXmF1DDGHPBVH"

    private val userEmail = "floater@test887123.com"

    private lateinit var keyPair: StrikeKeyPair

    private lateinit var approverPublicKey: String

    private lateinit var encryptedPrivateKey: String
    private lateinit var decyptionKey: ByteArray

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        encryptionManager = EncryptionManagerImpl(securePreferences)
        keyPair = encryptionManager.createKeyPair()

        decyptionKey = encryptionManager.generatePassword()
        encryptedPrivateKey = encryptionManager.encrypt(
            message = BaseWrapper.encodeToUTF8(keyPair.privateKey),
            generatedPassword = decyptionKey
        )

        whenever(securePreferences.retrievePrivateKey(userEmail)).then {
            encryptedPrivateKey
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
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = balanceAccountCreationWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey)

        println("BalanceAccount Signed Data: $signableData")

        return signableData
    }

    private fun generateSignWithdrawalRequestSignableData(): ByteArray {
        val withdrawalRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(withdrawalRequestJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = withdrawalRequestWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
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
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = conversionRequestWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
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
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = signersUpdateWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
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
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = signersUpdateRemovalWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
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
    fun generateSignatureForBalanceAccountCreation() {
        val balanceAccountCreationWalletApproval =
            deserializer.parseData(JsonParser.parseString(balanceAccountCreationJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = balanceAccountCreationWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
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
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = withdrawalRequestWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
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
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = conversionRequestWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
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
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestType = signersUpdateWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
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
            requestId = UUID.randomUUID().toString(),
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestType = signersUpdateRemovalWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
            email = userEmail
        )

        val signature = encryptionManager.signApprovalDispositionMessage(
            signable = approvalDispositionRequest, userEmail = userEmail
        )

        println("Signature from signers update removal: $signature")

        assertNotNull(signature)
    }
}