package com.censocustody.android

import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.common.toHexString
import com.censocustody.android.data.SignableDataResult
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2Deserializer
import com.google.gson.Gson
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Test
import java.util.UUID

class ApprovalRequestV2SigningTest {

    private val deserializer = ApprovalRequestV2Deserializer()

    @Test
    fun testRetrieveCorrectSigningDataFromRequests() = try {
        for (location in testDataLocations) {
            val testData = parseTestData(location)

            for (testItem in testData) {
                println()
                println("----------TEST ITEM------------")

                val dispositionRequest =
                    ApprovalDispositionRequestV2(
                        requestId = UUID.randomUUID().toString().replace("-", ""),
                        approvalDisposition = ApprovalDisposition.APPROVE,
                        requestType = testItem.request.details,
                        email = ""
                    )

                val signableData = dispositionRequest.retrieveSignableData()

                for (signable in signableData) {
                    when (signable) {
                        is SignableDataResult.Bitcoin -> {
                            println("BITCOIN")
                            for (dataToSign in signable.dataToSign) {
                                assert(dataToSign.toHexString() in testItem.hashes!!)
                            }
                        }
                        is SignableDataResult.Device -> {
                            println("DEVICE")
                        }
                        is SignableDataResult.Ethereum -> {
                            println("ETHEREUM")
                            assert(testItem.hash == signable.dataToSign.toHexString())
                        }
                        is SignableDataResult.Offchain -> {
                            println("OFFCHAIN")
                            testItem.hash?.let {
                                assertEquals(testItem.hash, signable.dataToSign.toHexString())
                            }
                        }
                        is SignableDataResult.Polygon -> {
                            println("POLYGON")
                        }
                    }
                }
                println("----------TEST ITEM------------")
                println()
            }
        }
    } catch (e: Exception) {
        println("----------RECEIVED EXCEPTION-------------")
        println(e)
        println("----------END EXCEPTION-------------")
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
    "offchain-test-cases.json"
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