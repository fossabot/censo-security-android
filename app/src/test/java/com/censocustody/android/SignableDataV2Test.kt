package com.censocustody.android

import com.censocustody.android.common.wrapper.toHexString
import com.censocustody.android.data.cryptography.SignableDataResult
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
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
    fun testPolygonOps() {
        testEvmOps<SignableDataResult.Polygon>(
            Gson().fromJson(ClassLoader.getSystemResource("polygon-test-cases.json").readText().trimEnd('\n'), TestCases::class.java).testCases
        )
    }

    private inline fun <reified T: SignableDataResult.Evm> testEvmOps(testCases: List<TestCase>) {
        testCases.forEach {
            println(it)
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

    fun exampleDAppJson(): ApprovalRequestV2 {
        val dappJson =
            """{"id":"2f39b06d-152c-4889-b7b1-df27ae01718e","submitDate":"2023-05-31T14:47:35.546+00:00","submitterName":"Shanat Barua","submitterEmail":"sbarua+recovery@strikeprotocols.com","approvalTimeoutInSeconds":3600,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"EthereumDAppRequest","wallet":{"identifier":"9c0dc4a4-4557-4fab-8c65-9624310abc8d","name":"Trading","address":"0x0b95297b97e4c7cb4c450f784db723640a40a158"},"fee":{"value":"0.0115360833","nativeValue":"0.011536083275368285","usdEquivalent":"21.44"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"},"dappInfo":{"name":"Balancer","url":"https://app.balancer.fi","description":"Balancer stETH Stable Pool — a Balancer MetaStable pool","icons":["https://app.balancer.fi/favicon.ico"]},"dappParams":{"type":"EthSendTransaction","simulationResult":{"type":"Success","balanceChanges":[{"amount":{"value":"0.017773622245620843","nativeValue":"0.017773622245620843","usdEquivalent":"37.21"},"symbolInfo":{"symbol":"WSTETH:0x7f39c581f595b53c5cb19bd0b3f8da6c935e2ca0","description":"Lido wstETH","imageUrl":"https://static.alchemyapi.io/images/assets/12409.png","tokenInfo":{"type":"ERC20","contractAddress":"0x7f39c581f595b53c5cb19bd0b3f8da6c935e2ca0"}}},{"amount":{"value":"-0.020023987243304243","nativeValue":"-0.020023987243304243","usdEquivalent":"-37.19"},"symbolInfo":{"symbol":"stETH:0xae7ab96520de3a18e5e111b5eaab095312d7fe84","description":"Lido Staked ETH","imageUrl":"https://static.alchemyapi.io/images/assets/8085.png","tokenInfo":{"type":"ERC20","contractAddress":"0xae7ab96520de3a18e5e111b5eaab095312d7fe84"}}}],"tokenAllowancesV2":[]},"transaction":{"from":"0x0b95297b97e4c7cb4c450f784db723640a40a158","to":"0x7f39c581f595b53c5cb19bd0b3f8da6c935e2ca0","value":"0x0","data":"0xea598cb0000000000000000000000000000000000000000000000000004723b5d69fc533"}},"signingData":{"type":"ethereum","transaction":{"safeNonce":34,"chainId":1,"vaultAddress":null,"orgVaultAddress":null,"contractAddresses":[]}}},"vaultName":"New York","initiationOnly":false}"""

        return ApprovalRequestV2Deserializer().toObjectWithParsedDetails(dappJson)
    }
}