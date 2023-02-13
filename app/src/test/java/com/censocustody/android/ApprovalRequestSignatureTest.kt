package com.censocustody.android

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.common.KeyStorage
import com.censocustody.android.data.*
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2Deserializer
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*
import javax.crypto.Cipher

class ApprovalRequestSignatureTest {

    private val deserializer = ApprovalRequestV2Deserializer()

    @Mock
    lateinit var keyStorage: KeyStorage

    @Mock
    lateinit var cipher: Cipher

    lateinit var encryptionManager : EncryptionManager

    lateinit var rootSeed: ByteArray

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        encryptionManager = EncryptionManagerImpl(
            keyStorage = keyStorage,
            cryptographyManager = CryptographyManagerImpl()
        )

        val phrase = encryptionManager.generatePhrase()

        rootSeed = Mnemonics.MnemonicCode(phrase).toSeed()

        whenever(keyStorage.retrieveRootSeed(any(), any())).then {
            BaseWrapper.decode(BaseWrapper.encode(rootSeed))
        }

        println(rootSeed)
    }

    @Test
    fun testRetrieveCorrectSigningDataFromRequests() {
        for (location in testDataLocations) {
            val testData = parseTestData(location)

            for (testItem in testData) {
                println(testItem)

                val dispositionRequest =
                    ApprovalDispositionRequestV2(
                        requestId = UUID.randomUUID().toString().replace("-", ""),
                        approvalDisposition = ApprovalDisposition.APPROVE,
                        requestType = testItem.request.details,
                        email = ""
                    )

                val signableData = dispositionRequest.retrieveSignableData()

                val signatures = encryptionManager.signApprovalDisposition(
                    email = "",
                    cipher = cipher,
                    dataToSign = signableData
                )

                println(signatures)
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