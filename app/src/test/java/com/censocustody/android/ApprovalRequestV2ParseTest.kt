package com.censocustody.android

import com.censocustody.android.data.models.approval.*
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2Deserializer
import org.junit.Assert.*
import org.junit.Test


fun getFullListOfApprovalItems(): List<ApprovalRequestV2> {
    val deserializer = ApprovalRequestV2Deserializer()

    val allApprovalRequests = mutableListOf<ApprovalRequestV2>()
    exampleRequests.forEach {
        val approval = deserializer.toObjectWithParsedDetails(it)
        allApprovalRequests.add(approval)
    }

    return allApprovalRequests
}

class ParseApprovalRequestV2Types {

    private val mockUriWrapper = MockUriWrapper()

    @Test
    fun parseAllApprovalRequestTypes() {
        val allApprovalRequests = getFullListOfApprovalItems()

        assertEquals(exampleRequests.size, allApprovalRequests.size)

        allApprovalRequests.forEach { approvalRequest ->
            assertNotNull(approvalRequest)
            assertNotNull(approvalRequest.details)

            assertTrue(approvalRequest.details !is ApprovalRequestDetailsV2.UnknownApprovalType)
        }
    }


    @Test
    fun testLoginApprovalParsing() {
        val deserializer = ApprovalRequestV2Deserializer()
        val loginApprovalWalletApproval = deserializer.toObjectWithParsedDetails(exampleRequests[0].trim())

        assertNotNull(loginApprovalWalletApproval)
        assertNotNull(loginApprovalWalletApproval.details)

        val details = loginApprovalWalletApproval.details as ApprovalRequestDetailsV2.Login
        assertNotEquals(details, ApprovalRequestDetails.UnknownApprovalType)
        assertEquals(
            details::class.java,
            ApprovalRequestDetailsV2.Login::class.java
        )

        assertNotNull(details.jwtToken)
    }

    @Test
    fun testSerializingAndDeserializingLoginApproval() {
        val deserializer = ApprovalRequestV2Deserializer()
        val loginApprovalWalletApproval = deserializer.toObjectWithParsedDetails(exampleRequests[0].trim())

        assertEquals(
            loginApprovalWalletApproval.details::class.java,
            ApprovalRequestDetailsV2.Login::class.java
        )

        val asString = ApprovalRequestV2.toJson(loginApprovalWalletApproval, mockUriWrapper)
        assertNotNull(asString)

        val parsedApprovalRequest = ApprovalRequestV2.fromJson(asString)
        assertEquals(loginApprovalWalletApproval, parsedApprovalRequest)

        assert(parsedApprovalRequest.details !is ApprovalRequestDetailsV2.UnknownApprovalType)
        assert(parsedApprovalRequest.details is ApprovalRequestDetailsV2.Login)

        val details = parsedApprovalRequest.details as ApprovalRequestDetailsV2.Login

        assertNotNull(details.jwtToken)
    }

    @Test
    fun testUnknownRequest() {
        val unknownRequest = """
        {"id":"316d0247-a1cb-47d9-b447-04ba544967e8","submitDate":"2023-01-31T17:04:24.359+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"SomeGarbageRequest","wallet":{"identifier":"98a36396-d4b5-4c7b-9789-a55d8d7e0c40","name":"Ethereum Wallet 1","address":"0xF0e86822Cf7bD35588235C8Eef342767C4d4F2Ec"},"amount":{"value":"1","nativeValue":"1","usdEquivalent":null},"symbolInfo":{"symbol":"GGSG","description":"0x2bddb61d0bf888de57eb5060d7f69317694431ff at ethereum","imageUrl":"https://www.arweave.net/r_K7MRot4iVWht_QKp9wiPpfCvC39bXi9cQsEn0B6WY?ext=jpeg","tokenInfo":{"type":"ERC721","contractAddress":"0x2BDDb61d0bF888De57EB5060d7F69317694431fF","tokenId":"5055"},"nftMetadata":{"name":"Galactic Gecko #5055"}},"fee":{"value":"0.0006353371","nativeValue":"0.0006353371","usdEquivalent":"2.82"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"},"destination":{"name":"0x6E01aF3913026660Fcebb93f054345eCCd972251","address":"0x6E01aF3913026660Fcebb93f054345eCCd972251"},"signingData":{"type":"ethereum","transaction":{"safeNonce":3,"chainId":31337,"priorityFee":5000000000,"vaultAddress":null,"contractAddresses":[]}}},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent()

        val deserializer = ApprovalRequestV2Deserializer()
        val unknownApproval = deserializer.toObjectWithParsedDetails(unknownRequest)

        assertEquals(
            unknownApproval.details::class.java,
            ApprovalRequestDetailsV2.UnknownApprovalType::class.java
        )

    }

    @Test
    fun testParseVaultPolicy() {
        val deserializer = ApprovalRequestV2Deserializer()
        val vaultPolicyApproval = deserializer.toObjectWithParsedDetails(vaultPolicyJson)

        assertNotNull(
            vaultPolicyApproval.details.toJson(),
        )
    }
}