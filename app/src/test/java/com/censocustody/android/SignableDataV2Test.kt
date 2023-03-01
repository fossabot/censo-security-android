package com.censocustody.android

import com.censocustody.android.common.toHexString
import com.censocustody.android.data.SignableDataResult
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
    fun testLoginOp() {
        val loginApprovalWalletApproval = deserializer.toObjectWithParsedDetails(exampleRequests[0].trim())
        val disposition = ApprovalDispositionRequestV2(
            "guid",
            ApprovalDisposition.APPROVE,
            loginApprovalWalletApproval.details,
            "email"
        )
        assertEquals(
            disposition.retrieveSignableData(),
            listOf(
                SignableDataResult.Device(
                    "*****".toByteArray(),
                    "*****".toByteArray()
                )
            )
        )
    }

    @Test
    fun testEthereumOps() {
        val testCases = Gson().fromJson(ClassLoader.getSystemResource("ethereum-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases

        testCases.forEach {
            println(it.request)
            val details = deserializer.toObjectWithParsedDetails(it.request).details
            val disposition = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.APPROVE,
                details,
                "email"
            )
            val ethSignableDataResult = disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Ethereum>().first()
            assertEquals(
                ethSignableDataResult.dataToSign.toHexString().lowercase(),
                it.hash
            )
            // special cases where there is also offchain
            when (details) {
                is ApprovalRequestDetailsV2.EthereumTransferPolicyUpdate -> {
                    val dataToSend = details.toJson().toByteArray()
                    assertEquals(
                        ethSignableDataResult.offchain,
                        SignableDataResult.Offchain(
                            dataToSend = dataToSend,
                            dataToSign = Hash.sha256(dataToSend)
                        )
                    )
                }
                is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
                    val dataToSend = details.toJson().toByteArray()
                    assertEquals(
                        disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Offchain>().first(),
                        SignableDataResult.Offchain(
                            dataToSend = dataToSend,
                            dataToSign = Hash.sha256(dataToSend)
                        )
                    )
                }
                is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate -> {
                    val dataToSend = details.toJson().toByteArray()
                    assertEquals(
                        disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Offchain>().first(),
                        SignableDataResult.Offchain(
                            dataToSend = dataToSend,
                            dataToSign = Hash.sha256(dataToSend)
                        )
                    )
                }
                is ApprovalRequestDetailsV2.VaultNameUpdate -> {
                    val dataToSend = details.toJson().toByteArray()
                    assertEquals(
                        disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Offchain>().first(),
                        SignableDataResult.Offchain(
                            dataToSend = dataToSend,
                            dataToSign = Hash.sha256(dataToSend)
                        )
                    )
                }
                else -> {}
            }
        }
    }

    @Test
    fun testPolgygonOps() {
        val testCases = Gson().fromJson(ClassLoader.getSystemResource("polygon-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases

        testCases.forEach {
            println(it.request)
            val details = deserializer.toObjectWithParsedDetails(it.request).details
            val disposition = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.APPROVE,
                details,
                "email"
            )
            val ethSignableDataResult = disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Polygon>().first()
            assertEquals(
                ethSignableDataResult.dataToSign.toHexString().lowercase(),
                it.hash
            )
            // special cases where there is also offchain
            when (details) {
                is ApprovalRequestDetailsV2.PolygonTransferPolicyUpdate -> {
                    val dataToSend = details.toJson().toByteArray()
                    assertEquals(
                        ethSignableDataResult.offchain,
                        SignableDataResult.Offchain(
                            dataToSend = dataToSend,
                            dataToSign = Hash.sha256(dataToSend)
                        )
                    )
                }
                is ApprovalRequestDetailsV2.VaultPolicyUpdate -> {
                    val dataToSend = details.toJson().toByteArray()
                    assertEquals(
                        disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Offchain>().first(),
                        SignableDataResult.Offchain(
                            dataToSend = dataToSend,
                            dataToSign = Hash.sha256(dataToSend)
                        )
                    )
                }
                is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate -> {
                    val dataToSend = details.toJson().toByteArray()
                    assertEquals(
                        disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Offchain>().first(),
                        SignableDataResult.Offchain(
                            dataToSend = dataToSend,
                            dataToSign = Hash.sha256(dataToSend)
                        )
                    )
                }
                is ApprovalRequestDetailsV2.OrgAdminPolicyUpdate -> {
                    val dataToSend = details.toJson().toByteArray()
                    assertEquals(
                        disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Offchain>().first(),
                        SignableDataResult.Offchain(
                            dataToSend = dataToSend,
                            dataToSign = Hash.sha256(dataToSend)
                        )
                    )
                }
                else -> {}
            }
        }
    }

    @Test
    fun testOffchainOps() {
        val testCases = Gson().fromJson(ClassLoader.getSystemResource("offchain-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases

        testCases.forEach {
            println(it.request)
            val details = deserializer.toObjectWithParsedDetails(it.request).details
            val disposition = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.APPROVE,
                details,
                "email"
            )
            val dataToSend = details.toJson().toByteArray()
            assertEquals(
                disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Offchain>().first(),
                SignableDataResult.Offchain(
                    dataToSend = dataToSend,
                    dataToSign = Hash.sha256(dataToSend)
                )
            )
        }
    }

    @Test
    fun testBitcoinOps() {
        val testCases = Gson().fromJson(ClassLoader.getSystemResource("bitcoin-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases

        testCases.forEach {
            println(it.request)
            val disposition = ApprovalDispositionRequestV2(
                "guid",
                ApprovalDisposition.APPROVE,
                deserializer.toObjectWithParsedDetails(it.request).details,
                "email"
            )
            assertEquals(
                disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Bitcoin>().first().dataToSign.map { it.toHexString().lowercase() },
                it.hashes
            )
        }
    }

    @Test
    fun testDeviceKeyOps() {
        val testCases = Gson().fromJson(ClassLoader.getSystemResource("device-key-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases

        testCases.forEach {
            println(it.request)
            val request = deserializer.toObjectWithParsedDetails(it.request)
            val disposition = ApprovalDispositionRequestV2(
                request.id,
                ApprovalDisposition.APPROVE,
                request.details,
                "email"
            )
            val dataToSend = when (request.details) {
                is ApprovalRequestDetailsV2.Login -> (request.details as ApprovalRequestDetailsV2.Login).jwtToken
                is ApprovalRequestDetailsV2.PasswordReset -> request.id
                else -> ""
            }
            assertEquals(
                disposition.retrieveSignableData().filterIsInstance<SignableDataResult.Device>().first(),
                SignableDataResult.Device(
                    dataToSend = dataToSend.toByteArray(),
                    dataToSign = dataToSend.toByteArray()
                )
            )
        }
    }


}