package com.strikeprotocols.mobile

import cash.z.ecc.android.bip39.Mnemonics
import com.google.gson.JsonParser
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.EncryptionManagerImpl
import com.strikeprotocols.mobile.data.SecurePreferences
import com.strikeprotocols.mobile.data.StrikeKeyPair
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
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

    private val exampleNonces =
        listOf(
            Nonce("GfNRNaKxa1dND25d5TcMnVUewqQEoXKtXmF1DDGHPBVH"),
            Nonce("GfNRNaKxa1dND25d5TcMnVUewqQEoXKtXmF1DDGHPBVH")
        )

    private val userEmail = "floater@test887123.com"

    private lateinit var phrase: String
    private lateinit var keyPair: StrikeKeyPair

    private lateinit var approverPublicKey: String

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        encryptionManager = EncryptionManagerImpl(securePreferences)
        phrase = encryptionManager.generatePhrase()
        keyPair = encryptionManager.createKeyPair(Mnemonics.MnemonicCode(phrase = phrase))

        whenever(securePreferences.retrievePrivateKey(userEmail)).then {
            BaseWrapper.encode(keyPair.privateKey)
        }

        approverPublicKey = BaseWrapper.encode(keyPair.publicKey)
    }

    //region Test creating api body from initiation request
    @Test
    fun fullBalanceAccountIntitiationApiBody() {
        val initiationRequest = generateBalanceAccountInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager)

        assert(apiBody.approvalDisposition == initiationRequest.approvalDisposition)
        assert(apiBody.nonce == exampleNonces.first().value)
        assert(apiBody.initiatorSignature.isNotEmpty())
        assert(
            apiBody.nonceAccountAddress == initiationRequest.requestType.nonceAccountAddresses()
                .first()
        )
        assert(apiBody.opAccountAddress == initiationRequest.opAccountPublicKey().toBase58())
        assert(apiBody.opAccountSignature.isNotEmpty())
    }

    @Test
    fun signersUpdateIntitiationApiBody() {
        val initiationRequest = generateSignersUpdateInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager)

        assert(apiBody.approvalDisposition == initiationRequest.approvalDisposition)
        assert(apiBody.nonce == exampleNonces.first().value)
        assert(apiBody.initiatorSignature.isNotEmpty())
        assert(
            apiBody.nonceAccountAddress == initiationRequest.requestType.nonceAccountAddresses()
                .first()
        )
        assert(apiBody.opAccountAddress == initiationRequest.opAccountPublicKey().toBase58())
        assert(apiBody.opAccountSignature.isNotEmpty())
    }

    @Test
    fun withdrawalRequestIntitiationApiBody() {
        val initiationRequest = generateWithdrawalRequestInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager)

        assert(apiBody.approvalDisposition == initiationRequest.approvalDisposition)
        assert(apiBody.nonce == exampleNonces.first().value)
        assert(apiBody.initiatorSignature.isNotEmpty())
        assert(
            apiBody.nonceAccountAddress == initiationRequest.requestType.nonceAccountAddresses()
                .first()
        )
        assert(apiBody.opAccountAddress == initiationRequest.opAccountPublicKey().toBase58())
        assert(apiBody.opAccountSignature.isNotEmpty())
    }

    @Test
    fun conversionRequestIntitiationApiBody() {
        val initiationRequest = generateConversionRequestInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager)

        assert(apiBody.approvalDisposition == initiationRequest.approvalDisposition)
        assert(apiBody.nonce == exampleNonces.first().value)
        assert(apiBody.initiatorSignature.isNotEmpty())
        assert(
            apiBody.nonceAccountAddress == initiationRequest.requestType.nonceAccountAddresses()
                .first()
        )
        assert(apiBody.opAccountAddress == initiationRequest.opAccountPublicKey().toBase58())
        assert(apiBody.opAccountSignature.isNotEmpty())
    }
    //endregion

    //region Testing Signable data
    @Test
    fun testGenerateBalanceAccountInitiationSignableData() {
        val initiationRequest = generateBalanceAccountInitiationSignableData()

        val simpleSignableData =
            initiationRequest.retrieveSignableData(approverPublicKey)

        assertNotNull(simpleSignableData)
    }

    @Test
    fun testGenerateSignersUpdateInitiationSignableData() {
        val initiationRequest = generateSignersUpdateInitiationSignableData()

        val simpleSignableData =
            initiationRequest.retrieveSignableData(approverPublicKey)

        assertNotNull(simpleSignableData)
    }

    @Test
    fun testGenerateWithdrawalRequestInitiationSignableData() {
        val initiationRequest = generateWithdrawalRequestInitiationSignableData()

        val simpleSignableData =
            initiationRequest.retrieveSignableData(approverPublicKey)

        assertNotNull(simpleSignableData)
    }

    @Test
    fun testGenerateConversionRequestInitiationSignableData() {
        val initiationRequest = generateConversionRequestInitiationSignableData()

        val simpleSignableData =
            initiationRequest.retrieveSignableData(approverPublicKey)

        assertNotNull(simpleSignableData)
    }
    //endregion

    //region create initiation requests
    private fun generateBalanceAccountInitiationSignableData(): InitiationRequest {
        val multiSigBalanceAccountCreationWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSigWithBalanceAccountCreationJson.trim()))

        val initiation =
            (multiSigBalanceAccountCreationWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigBalanceAccountCreationWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigBalanceAccountCreationWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )
    }

    private fun generateSignersUpdateInitiationSignableData(): InitiationRequest {
        val multiSigSignersUpdateWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSigWithSignersUpdateJson.trim()))

        val initiation =
            (multiSigSignersUpdateWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigSignersUpdateWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigSignersUpdateWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )
    }

    private fun generateWithdrawalRequestInitiationSignableData(): InitiationRequest {
        val multiSigWithdrawalRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSigWithWithdrawalRequestJson.trim()))

        val initiation =
            (multiSigWithdrawalRequestWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigWithdrawalRequestWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigWithdrawalRequestWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )
    }

    private fun generateDAppTransactionInitiationSignableData(): InitiationRequest {
        val multiSigDAppTransactionWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSignWithDAppRequestJson.trim()))

        val initiation =
            (multiSigDAppTransactionWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigDAppTransactionWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigDAppTransactionWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )
    }

    private fun generateConversionRequestInitiationSignableData(): InitiationRequest {
        val multiSigConversionRequestWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSigWithConversionRequestJson.trim()))

        val initiation =
            (multiSigConversionRequestWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigConversionRequestWalletApproval.id!!,
            initiation = initiation,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            requestType = multiSigConversionRequestWalletApproval.getSolanaApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )
    }
    //endregion

}
