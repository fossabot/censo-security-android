package com.strikeprotocols.mobile

import com.google.gson.JsonParser
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.EncryptionManagerImpl
import com.strikeprotocols.mobile.data.SecurePreferences
import com.strikeprotocols.mobile.data.StrikeKeyPair
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.WalletApprovalDeserializer
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class InitiationRequestSigningTest {

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

    //region Test creating api body from initiation request
    @Test
    fun fullBalanceAccountIntitiationApiBody() {
        val initiationRequest = generateBalanceAccountInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager)

        //todo: when we receive valid dataAccountCreationInfo, then we can validate that data also
        assert(apiBody.approvalDisposition == initiationRequest.approvalDisposition)
        assert(apiBody.recentBlockhash == exampleBlockHash)
        assert(apiBody.initiatorSignature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.signature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.publicKey == initiationRequest.opAccountPublicKey().toBase58())
    }

    @Test
    fun signersUpdateIntitiationApiBody() {
        val initiationRequest = generateSignersUpdateInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager)

        //todo: when we receive valid dataAccountCreationInfo, then we can validate that data also
        assert(apiBody.approvalDisposition == initiationRequest.approvalDisposition)
        assert(apiBody.recentBlockhash == exampleBlockHash)
        assert(apiBody.initiatorSignature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.signature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.publicKey == initiationRequest.opAccountPublicKey().toBase58())
    }

    @Test
    fun withdrawalRequestIntitiationApiBody() {
        val initiationRequest = generateWithdrawalRequestInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager)

        //todo: when we receive valid dataAccountCreationInfo, then we can validate that data also
        assert(apiBody.approvalDisposition == initiationRequest.approvalDisposition)
        assert(apiBody.recentBlockhash == exampleBlockHash)
        assert(apiBody.initiatorSignature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.signature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.publicKey == initiationRequest.opAccountPublicKey().toBase58())
    }

    @Test
    fun dAppTransactionIntitiationApiBody() {
        val initiationRequest = generateDAppTransactionInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager)

        //todo: when we receive valid dataAccountCreationInfo, then we can validate that data also
        assert(apiBody.approvalDisposition == initiationRequest.approvalDisposition)
        assert(apiBody.recentBlockhash == exampleBlockHash)
        assert(apiBody.initiatorSignature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.signature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.publicKey == initiationRequest.opAccountPublicKey().toBase58())
    }

    @Test
    fun conversionRequestIntitiationApiBody() {
        val initiationRequest = generateConversionRequestInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager)

        //todo: when we receive valid dataAccountCreationInfo, then we can validate that data also
        assert(apiBody.approvalDisposition == initiationRequest.approvalDisposition)
        assert(apiBody.recentBlockhash == exampleBlockHash)
        assert(apiBody.initiatorSignature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.signature.isNotEmpty())
        assert(apiBody.opAccountSignatureInfo.publicKey == initiationRequest.opAccountPublicKey().toBase58())
    }
    //endregion

    //region Testing Signable data
    @Test
    fun testGenerateBalanceAccountInitiationSignableData() {
        val initiationRequest = generateBalanceAccountInitiationSignableData()

        val simpleSignableData =
            initiationRequest.retrieveSignableData(approverPublicKey)
        val supplyInstructionSignableData =
            initiationRequest.signableSupplyInstructions(approverPublicKey)

        assertNotNull(simpleSignableData)
        assertNotNull(supplyInstructionSignableData)
    }

    @Test
    fun testGenerateSignersUpdateInitiationSignableData() {
        val initiationRequest = generateSignersUpdateInitiationSignableData()

        val simpleSignableData =
            initiationRequest.retrieveSignableData(approverPublicKey)
        val supplyInstructionSignableData =
            initiationRequest.signableSupplyInstructions(approverPublicKey)

        assertNotNull(simpleSignableData)
        assertNotNull(supplyInstructionSignableData)
    }

    @Test
    fun testGenerateWithdrawalRequestInitiationSignableData() {
        val initiationRequest = generateWithdrawalRequestInitiationSignableData()

        val simpleSignableData =
            initiationRequest.retrieveSignableData(approverPublicKey)
        val supplyInstructionSignableData =
            initiationRequest.signableSupplyInstructions(approverPublicKey)

        assertNotNull(simpleSignableData)
        assertNotNull(supplyInstructionSignableData)
    }

    @Test
    fun testGenerateDAppTransactionInitiationSignableData() {
        val initiationRequest = generateDAppTransactionInitiationSignableData()

        val simpleSignableData =
            initiationRequest.retrieveSignableData(approverPublicKey)
        val supplyInstructionSignableData =
            initiationRequest.signableSupplyInstructions(approverPublicKey)

        assertNotNull(simpleSignableData)
        assertNotNull(supplyInstructionSignableData)
    }

    @Test
    fun testGenerateConversionRequestInitiationSignableData() {
        val initiationRequest = generateConversionRequestInitiationSignableData()

        val simpleSignableData =
            initiationRequest.retrieveSignableData(approverPublicKey)
        val supplyInstructionSignableData =
            initiationRequest.signableSupplyInstructions(approverPublicKey)

        assertNotNull(simpleSignableData)
        assertNotNull(supplyInstructionSignableData)
    }
    //endregion

    //region create initiation requests
    private fun generateBalanceAccountInitiationSignableData() : InitiationRequest {
        val multiSigBalanceAccountCreationWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSigWithBalanceAccountCreationJson.trim()))

        val initiation =
            (multiSigBalanceAccountCreationWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

         return InitiationRequest(
            requestId = multiSigBalanceAccountCreationWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigBalanceAccountCreationWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
            email = userEmail
        )
    }

    private fun generateSignersUpdateInitiationSignableData() : InitiationRequest {
        val multiSigSignersUpdateWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSigWithSignersUpdateJson.trim()))

        val initiation =
            (multiSigSignersUpdateWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigSignersUpdateWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigSignersUpdateWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
            email = userEmail
        )
    }

    private fun generateWithdrawalRequestInitiationSignableData() : InitiationRequest {
        val multiSigWithdrawalRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSigWithWithdrawalRequestJson.trim()))

        val initiation =
            (multiSigWithdrawalRequestWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigWithdrawalRequestWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigWithdrawalRequestWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
            email = userEmail
        )
    }

    private fun generateDAppTransactionInitiationSignableData() : InitiationRequest {
        val multiSigDAppTransactionWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSignWithDAppRequestJson.trim()))

        val initiation =
            (multiSigDAppTransactionWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigDAppTransactionWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigDAppTransactionWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
            email = userEmail
        )
    }

    private fun generateConversionRequestInitiationSignableData() : InitiationRequest {
        val multiSigConversionRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSigWithConversionRequestJson.trim()))

        val initiation =
            (multiSigConversionRequestWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigConversionRequestWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigConversionRequestWalletApproval.getSolanaApprovalRequestType(),
            blockhash = exampleBlockHash,
            email = userEmail
        )
    }
    //endregion

}
