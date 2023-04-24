package com.censocustody.android

import com.censocustody.android.common.toHexString
import com.censocustody.android.data.cryptography.SignableDataResult
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2Deserializer
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test
import org.web3j.crypto.Hash

class SignableDataV2Test {
    private val deserializer = ApprovalRequestV2Deserializer()

    data class TestCase(
        val request: String,
        val hash: String = "",
        val hashes: List<String>? = null
    )

    data class TestCases(
        val testCases: List<TestCase>
    )

    @Test
    fun testEthereumOps() {
        testEvmOps<SignableDataResult.Ethereum>(
            Gson().fromJson(ClassLoader.getSystemResource("ethereum-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases
        )
    }

    @Test
    fun testPolgygonOps() {
        testEvmOps<SignableDataResult.Polygon>(
            Gson().fromJson(ClassLoader.getSystemResource("polygon-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases
        )
    }

    private inline fun <reified T: SignableDataResult.Evm> testEvmOps(testCases: List<TestCase>) {
        testCases.forEach {
            println(it.request)
            val details = deserializer.toObjectWithParsedDetails(it.request).details
            val approval = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.APPROVE,
                details,
                "email",
                emptyList(),
                emptyList()
            )

            val ethSignableDataResult = approval.retrieveSignableData().filterIsInstance<T>().first()
            assertEquals(
                it.hash,
                ethSignableDataResult.dataToSign.toHexString().lowercase()
            )

            val approvalOffchainSignaturePayload = ApprovalDispositionRequestV2.ApprovalRequestDetailsWithDisposition(
                details,
                ApprovalDisposition.APPROVE
            ).toJson().toByteArray()
            assertEquals(
                SignableDataResult.Offchain(
                    dataToSend = approvalOffchainSignaturePayload,
                    dataToSign = Hash.sha256(approvalOffchainSignaturePayload)
                ),
                ethSignableDataResult.offchain
            )

            val denial = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.DENY,
                details,
                "email",
                emptyList(),
                emptyList()
            )
            val denialSignableData = denial.retrieveSignableData()
            val denialOffchainSignaturePayload = ApprovalDispositionRequestV2.ApprovalRequestDetailsWithDisposition(
                details,
                ApprovalDisposition.DENY
            ).toJson().toByteArray()
            assertEquals(
                listOf(
                    SignableDataResult.Offchain(
                        dataToSend = denialOffchainSignaturePayload,
                        dataToSign = Hash.sha256(denialOffchainSignaturePayload)
                    )
                ),
                denialSignableData
            )
        }
    }

    @Test
    fun testOffchainOps() {
        val testCases = Gson().fromJson(ClassLoader.getSystemResource("offchain-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases

        testCases.forEach {
            println(it.request)
            val details = deserializer.toObjectWithParsedDetails(it.request).details
            val approval = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.APPROVE,
                details,
                "email",
                emptyList(),
                emptyList()
            )
            val approvalDataToSend = ApprovalDispositionRequestV2.ApprovalRequestDetailsWithDisposition(
                details,
                ApprovalDisposition.APPROVE
            ).toJson().toByteArray()
            assertEquals(
                approval.retrieveSignableData().filterIsInstance<SignableDataResult.Offchain>().first(),
                SignableDataResult.Offchain(
                    dataToSend = approvalDataToSend,
                    dataToSign = Hash.sha256(approvalDataToSend)
                )
            )

            val denial = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.DENY,
                details,
                "email",
                emptyList(),
                emptyList()
            )
            val denialDataToSend = ApprovalDispositionRequestV2.ApprovalRequestDetailsWithDisposition(
                details,
                ApprovalDisposition.DENY
            ).toJson().toByteArray()
            assertEquals(
                denial.retrieveSignableData().filterIsInstance<SignableDataResult.Offchain>().first(),
                SignableDataResult.Offchain(
                    dataToSend = denialDataToSend,
                    dataToSign = Hash.sha256(denialDataToSend)
                )
            )
        }
    }

    @Test
    fun testBitcoinOps() {
        val testCases = Gson().fromJson(ClassLoader.getSystemResource("bitcoin-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases

        testCases.forEach {
            println(it.request)
            val details = deserializer.toObjectWithParsedDetails(it.request).details
            val approval = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.APPROVE,
                details,
                "email",
                emptyList(),
                emptyList()
            )
            val bitcoinSignableDataResult = approval.retrieveSignableData().filterIsInstance<SignableDataResult.Bitcoin>().first()

            assertEquals(
                it.hashes,
                bitcoinSignableDataResult.dataToSign.map { it.toHexString().lowercase() },
            )

            val approvalOffchainSignaturePayload = ApprovalDispositionRequestV2.ApprovalRequestDetailsWithDisposition(
                details,
                ApprovalDisposition.APPROVE
            ).toJson().toByteArray()
            assertEquals(
                SignableDataResult.Offchain(
                    dataToSend = approvalOffchainSignaturePayload,
                    dataToSign = Hash.sha256(approvalOffchainSignaturePayload)
                ),
                bitcoinSignableDataResult.offchain
            )

            val denial = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.DENY,
                details,
                "email",
                emptyList(),
                emptyList()
            )
            val denialOffchainSignaturePayload = ApprovalDispositionRequestV2.ApprovalRequestDetailsWithDisposition(
                details,
                ApprovalDisposition.DENY
            ).toJson().toByteArray()

            assertEquals(
                listOf(
                    SignableDataResult.Offchain(
                        dataToSend = denialOffchainSignaturePayload,
                        dataToSign = Hash.sha256(denialOffchainSignaturePayload)
                    )
                ),
                denial.retrieveSignableData()
            )
        }
    }

    @Test
    fun testDeviceKeyOps() {
        val testCases = Gson().fromJson(ClassLoader.getSystemResource("device-key-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases

        testCases.forEach {
            println(it.request)
            val request = deserializer.toObjectWithParsedDetails(it.request)

            val approval = ApprovalDispositionRequestV2(
                request.id,
                ApprovalDisposition.APPROVE,
                request.details,
                "email",
                emptyList(),
                emptyList()
            )
            val approvalDataToSend = when (request.details) {
                is ApprovalRequestDetailsV2.Login -> "{\"token\":\"*****\",\"disposition\":\"Approve\"}"
                is ApprovalRequestDetailsV2.PasswordReset -> "{\"guid\":\"462e6540-7ef0-4efa-b844-532efcfa816d\",\"disposition\":\"Approve\"}"
                else -> ""
            }
            assertEquals(
                listOf(
                    SignableDataResult.Device(
                        dataToSend = approvalDataToSend.toByteArray(),
                        dataToSign = approvalDataToSend.toByteArray()
                    )
                ),
                approval.retrieveSignableData()
            )

            val denial = ApprovalDispositionRequestV2(
                request.id,
                ApprovalDisposition.DENY,
                request.details,
                "email",
                emptyList(),
                emptyList()
            )
            val denialDataToSend = when (request.details) {
                is ApprovalRequestDetailsV2.Login -> "{\"token\":\"*****\",\"disposition\":\"Deny\"}"
                is ApprovalRequestDetailsV2.PasswordReset -> "{\"guid\":\"462e6540-7ef0-4efa-b844-532efcfa816d\",\"disposition\":\"Deny\"}"
                else -> ""
            }
            assertEquals(
                listOf(
                    SignableDataResult.Device(
                        dataToSend = denialDataToSend.toByteArray(),
                        dataToSign = denialDataToSend.toByteArray()
                    )
                ),
                denial.retrieveSignableData()
            )
        }
    }
}