package com.strikeprotocols.mobile

import cash.z.ecc.android.bip39.Mnemonics
import com.google.gson.JsonParser
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.StoredKeyData
import com.strikeprotocols.mobile.data.models.StoredKeyData.Companion.SOLANA_KEY
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequestDeserializer
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*
import javax.crypto.Cipher

class InitiationRequestSigningTest {

    private val deserializer = ApprovalRequestDeserializer()

    @Mock
    lateinit var securePreferences: SecurePreferences

    @Mock
    lateinit var cryptographyManager: CryptographyManager

    @Mock
    lateinit var cipher: Cipher

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

    private lateinit var storedKeyData: StoredKeyData

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        encryptionManager = EncryptionManagerImpl(securePreferences, cryptographyManager)
        phrase = encryptionManager.generatePhrase()
        keyPair = encryptionManager.createKeyPair(Mnemonics.MnemonicCode(phrase = phrase))

        storedKeyData = StoredKeyData(
            initVector = BaseWrapper.encode(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
            encryptedKeysData = ""
        )

        whenever(securePreferences.retrieveEncryptedStoredKeys(userEmail)).then {
            storedKeyData.toJson()
        }

        whenever(cryptographyManager.decryptData(any(), any())).then {
            StoredKeyData.mapToJson(hashMapOf(
                SOLANA_KEY to BaseWrapper.encode(keyPair.privateKey)
            )).toByteArray()
        }

        approverPublicKey = BaseWrapper.encode(keyPair.publicKey)
    }

    //region Test creating api body from initiation request
    @Test
    fun fullBalanceAccountIntitiationApiBody() {
        val initiationRequest = generateWalletCreationInitiationSignableData()

        val apiBody = initiationRequest.convertToApiBody(encryptionManager, cipher)

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

        val apiBody = initiationRequest.convertToApiBody(encryptionManager, cipher)

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

        val apiBody = initiationRequest.convertToApiBody(encryptionManager, cipher)

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

        val apiBody = initiationRequest.convertToApiBody(encryptionManager, cipher)

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
        val initiationRequest = generateWalletCreationInitiationSignableData()

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
    private fun generateWalletCreationInitiationSignableData(): InitiationRequest {
        val multiSigWalletCreationWalletApproval =
            deserializer.parseData(JsonParser.parseString(multiSigWithWalletCreationJson.trim()))

        val initiation =
            (multiSigWalletCreationWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails).multisigOpInitiation

        return InitiationRequest(
            requestId = multiSigWalletCreationWalletApproval.id!!,
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            initiation = initiation,
            requestType = multiSigWalletCreationWalletApproval.getApprovalRequestType(),
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
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            initiation = initiation,
            requestType = multiSigSignersUpdateWalletApproval.getApprovalRequestType(),
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
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            initiation = initiation,
            requestType = multiSigWithdrawalRequestWalletApproval.getApprovalRequestType(),
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
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            initiation = initiation,
            requestType = multiSigDAppTransactionWalletApproval.getApprovalRequestType(),
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
            approvalDisposition = if (Random().nextBoolean()) ApprovalDisposition.APPROVE else ApprovalDisposition.DENY,
            initiation = initiation,
            requestType = multiSigConversionRequestWalletApproval.getApprovalRequestType(),
            nonces = exampleNonces,
            email = userEmail
        )
    }
    //endregion

}
