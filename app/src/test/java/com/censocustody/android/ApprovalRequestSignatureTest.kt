package com.censocustody.android

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.wrapper.BaseWrapper
import com.censocustody.android.data.cryptography.*
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2Deserializer
import com.censocustody.android.data.models.approvalV2.ApprovalSignature
import com.censocustody.android.data.storage.KeyStorage
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class ApprovalRequestSignatureTest {

    private val deserializer = ApprovalRequestV2Deserializer()

    @Mock
    lateinit var keyStorage: KeyStorage

    lateinit var encryptionManager: EncryptionManagerImpl

    lateinit var rootSeed: ByteArray

    lateinit var allKeys: AllKeys

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        encryptionManager = EncryptionManagerImpl(
            keyStorage = keyStorage,
            cryptographyManager = CryptographyManagerImpl()
        )

        val phrase = encryptionManager.generatePhrase()

        rootSeed = Mnemonics.MnemonicCode(phrase).toSeed()

        allKeys = encryptionManager.createAllKeys(rootSeed)

        whenever(keyStorage.retrieveRootSeed(any())).then {
            BaseWrapper.decode(BaseWrapper.encode(rootSeed))
        }

        println(rootSeed)
    }

    @Test
    fun testRetrieveCorrectSigningDataFromRequests() {
        for (location in testDataLocations) {
            val testData = parseTestData(location)

            for (testItem in testData) {

                val dispositionRequest =
                    ApprovalDispositionRequestV2(
                        requestId = UUID.randomUUID().toString().replace("-", ""),
                        approvalDisposition = ApprovalDisposition.APPROVE,
                        requestType = testItem.request.details,
                        email = "",
                        recoveryShards = emptyList(),
                        reShareShards = emptyList()
                    )

                val signableData = dispositionRequest.retrieveSignableData()

                val signatures = encryptionManager.signApprovalDisposition(
                    email = "",
                    dataToSign = signableData
                )

                val combinedData = signableData.zip(signatures)

                for (data in combinedData) {
                    when (val approvalSignature = data.second) {
                        is ApprovalSignature.PolygonSignature -> {
                            val polygonSignableData = data.first as SignableDataResult.Polygon
                            val polygonVerified = allKeys.ethereumKey.verifySignature(
                                signature = BaseWrapper.decodeFromBase64(approvalSignature.signature),
                                data = polygonSignableData.dataToSign
                            )
                            assert(polygonVerified)
                        }
                        is ApprovalSignature.EthereumSignature -> {
                            val ethereumSignableData = data.first as SignableDataResult.Ethereum
                            val ethereumVerified = allKeys.ethereumKey.verifySignature(
                                signature = BaseWrapper.decodeFromBase64(approvalSignature.signature),
                                data = ethereumSignableData.dataToSign
                            )
                            assert(ethereumVerified)
                        }
                        is ApprovalSignature.OffChainSignature -> {
                            val offChainSignableData = data.first as SignableDataResult.Offchain
                            val polygonVerified = allKeys.censoKey.verifySignature(
                                signature = BaseWrapper.decodeFromBase64(approvalSignature.signature),
                                data = offChainSignableData.dataToSign
                            )
                            assert(polygonVerified)
                        }
                        is ApprovalSignature.BitcoinSignatures -> {
                            val bitcoinSignableData = data.first as SignableDataResult.Bitcoin

                            val combinedBitcoinData =
                                bitcoinSignableData.dataToSign.zip(approvalSignature.signatures)

                            for (bitcoinData in combinedBitcoinData) {
                                val bitcoinVerified = allKeys.bitcoinKey
                                    .derive(
                                        path = ChildPathNumber(
                                            index = bitcoinSignableData.childKeyIndex,
                                            hardened = false
                                        )
                                    ).verifySignature(
                                        signature = BaseWrapper.decodeFromBase64(bitcoinData.second),
                                        data = bitcoinData.first
                                    )
                                assert(bitcoinVerified)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun parseTestData(location: String): List<TestCaseItem> {
        val testCasesJson = ClassLoader.getSystemResource(location).readText()
        val jsonObject = Gson().fromJson(testCasesJson, TestCaseData::class.java)
        return jsonObject.testCases.map {
            TestCaseItem(
                deserializer.toObjectWithParsedDetails(it.request),
                it.hashes,
                it.hash
            )
        }
    }
}

val testDataLocations = listOf(
    "bitcoin-test-cases.json",
    "device-key-test-cases.json",
    "ethereum-test-cases.json",
    "offchain-test-cases.json",
    "polygon-test-cases.json"
)

data class TestCaseData(
    val testCases: List<TestCase>
)

data class TestCase(
    val request: String,
    val hashes: List<String>?,
    val hash: String?
)

data class TestCaseItem(
    val request: ApprovalRequestV2,
    val hashes: List<String>?,
    val hash: String?
)