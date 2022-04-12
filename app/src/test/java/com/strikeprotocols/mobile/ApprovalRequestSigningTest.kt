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

    private val exampleBlockHash = "GfNRNaKxa1dND25d5TcMnVUewqQEoXKtXmF1DDGHPBVH"

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
            blockhash = exampleBlockHash,
            email = userEmail
        )

        val signableData = approvalDispositionRequest.retrieveSignableData(approverPublicKey)

        println("BalanceAccount Signed Data: $signableData")

        return signableData
    }

    private fun generateSignWithdrawalRequestSignableData(): ByteArray {
        val withdrawalJson = """{"id":"dc063f04-46e9-4933-9276-d37f676bc9a3","walletType":"Solana","submitDate":"2022-04-13T19:34:55.319+00:00","submitterName":"Ata Namvari","submitterEmail":"anamvari@blue.rock","approvalTimeoutInSeconds":86535,"numberOfDispositionsRequired":2,"numberOfApprovalsReceived":1,"numberOfDeniesReceived":0,"details":{"type":"WithdrawalRequest","account":{"identifier":"e175003a-659c-424b-8cbf-22cae783150b","name":"Reserves","accountType":"BalanceAccount","address":"2VYdyt85KMyTxMXcwngZowEMMZkk4z9ELy9RVMaUBRyM"},"symbolAndAmountInfo":{"symbolInfo":{"symbol":"SOL","symbolDescription":"Solana","tokenMintAddress":"11111111111111111111111111111111"},"amount":"2.000000000","nativeAmount":"2.000000000","usdEquivalent":"177.54"},"destination":{"name":"Transfers","address":"DHn5zByDkkv2sAs7YaWoQQMV95LmrjDm5ozDGCyau7xG"},"signingData":{"feePayer":"DUcwYFXaSp3te5B56KtdXwaQGAj39MSYFzRznf2J1g3p","walletProgramId":"4867yjdmvKAcwJWYheXBGJy68tciAoWCaJHCy3RX4CLD","multisigOpAccountAddress":"8anJfhBNFv9An5hXQGu1enuxHLeYR1JqXqbxUUoviDqp","walletAddress":"EPo4D4BfNJuJBhxGg9GE9LiXb5CrT4TWJW6FLpPnfW1Q"}}}
""".trim()


        val withdrawalRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(withdrawalJson.trim()))

        val approvalDispositionRequest = ApprovalDispositionRequest(
            requestId = withdrawalRequestWalletApproval.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
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
            requestId = conversionRequestWalletApproval.id!!,
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
            requestId = signersUpdateWalletApproval.id!!,
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
            requestId = signersUpdateRemovalWalletApproval.id!!,
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
            requestId = balanceAccountCreationWalletApproval.id!!,
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
            requestId = withdrawalRequestWalletApproval.id!!,
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
            requestId = conversionRequestWalletApproval.id!!,
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
            requestId = signersUpdateWalletApproval.id!!,
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
            requestId = signersUpdateRemovalWalletApproval.id!!,
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
