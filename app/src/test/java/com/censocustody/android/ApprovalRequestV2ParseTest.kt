package com.censocustody.android

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.censocustody.android.data.models.approval.*
import com.censocustody.android.data.models.approvalV2.ApprovalRequestDetailsV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2Deserializer
import org.junit.Assert.*
import org.junit.Test

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

    private fun getFullListOfApprovalItems(): List<ApprovalRequestV2> {
        val deserializer = ApprovalRequestV2Deserializer()

        val allApprovalRequests = mutableListOf<ApprovalRequestV2>()
        exampleRequests.forEach {
            val approval = deserializer.toObjectWithParsedDetails(it)
            allApprovalRequests.add(approval)
        }


//        val multiSigWithWalletCreationJson: JsonElement =
//            JsonParser.parseString(multiSigWithWalletCreationJson.trim())
//        val multiSigWithWalletCreationApprovalRequest =
//            deserializer.parseData(multiSigWithWalletCreationJson)
//        allApprovalRequests.add(multiSigWithWalletCreationApprovalRequest)
//
//        val solanaWalletCreationJson: JsonElement =
//            JsonParser.parseString(solanaWalletCreationJson.trim())
//        val solanaWalletCreationApprovalRequest =
//            deserializer.parseData(solanaWalletCreationJson)
//        allApprovalRequests.add(solanaWalletCreationApprovalRequest)
//
//        val bitcoinWalletCreationJson: JsonElement =
//            JsonParser.parseString(bitcoinWalletCreationJson.trim())
//        val bitcoinWalletCreationApprovalRequest =
//            deserializer.parseData(bitcoinWalletCreationJson)
//        allApprovalRequests.add(bitcoinWalletCreationApprovalRequest)
//
//        val ethereumWalletCreationJson: JsonElement =
//            JsonParser.parseString(ethereumWalletCreationJson.trim())
//        val ethereumWalletCreationApprovalRequest =
//            deserializer.parseData(ethereumWalletCreationJson)
//        allApprovalRequests.add(ethereumWalletCreationApprovalRequest)
//
//        val multiSigWithSignersUpdateJson: JsonElement =
//            JsonParser.parseString(multiSigWithSignersUpdateJson.trim())
//        val multiSigWithSignersUpdateWalletApproval =
//            deserializer.parseData(multiSigWithSignersUpdateJson)
//        allApprovalRequests.add(multiSigWithSignersUpdateWalletApproval)
//
//        val signersUpdateRemovalJson: JsonElement =
//            JsonParser.parseString(signersUpdateRemovalJson.trim())
//        val signersUpdateRemovalWalletApproval = deserializer.parseData(signersUpdateRemovalJson)
//        allApprovalRequests.add(signersUpdateRemovalWalletApproval)
//
//        val multiSigWithWithdrawalRequestJson: JsonElement =
//            JsonParser.parseString(multiSigWithWithdrawalRequestJson.trim())
//        val multiSigWithWithdrawalRequestWalletApproval =
//            deserializer.parseData(multiSigWithWithdrawalRequestJson)
//        allApprovalRequests.add(multiSigWithWithdrawalRequestWalletApproval)
//
//        val withdrawalRequestJson: JsonElement =
//            JsonParser.parseString(withdrawalRequestJson.trim())
//        val withdrawalRequestWalletApproval = deserializer.parseData(withdrawalRequestJson)
//        allApprovalRequests.add(withdrawalRequestWalletApproval)
//
//        val bitcoinWithdrawalRequestJson: JsonElement =
//            JsonParser.parseString(bitcoinWithdrawalRequestJson.trim())
//        val bitcoinWithdrawalRequestWalletApproval = deserializer.parseData(bitcoinWithdrawalRequestJson)
//        allApprovalRequests.add(bitcoinWithdrawalRequestWalletApproval)
//
//        val ethereumWithdrawalRequestJson: JsonElement =
//            JsonParser.parseString(ethereumWithdrawalRequestJson.trim())
//        val ethereumWithdrawalRequestWalletApproval = deserializer.parseData(ethereumWithdrawalRequestJson)
//        allApprovalRequests.add(ethereumWithdrawalRequestWalletApproval)
//
//        val erc20WithdrawalRequestJson: JsonElement =
//            JsonParser.parseString(erc20WithdrawalRequestJson.trim())
//        val erc20WithdrawalRequestWalletApproval = deserializer.parseData(erc20WithdrawalRequestJson)
//        allApprovalRequests.add(erc20WithdrawalRequestWalletApproval)
//
//        val erc721WithdrawalRequestJson: JsonElement =
//            JsonParser.parseString(erc721WithdrawalRequestJson.trim())
//        val erc721WithdrawalRequestWalletApproval = deserializer.parseData(erc721WithdrawalRequestJson)
//        allApprovalRequests.add(erc721WithdrawalRequestWalletApproval)
//
//        val erc1155WithdrawalRequestJson: JsonElement =
//            JsonParser.parseString(erc1155WithdrawalRequestJson.trim())
//        val erc1155WithdrawalRequestWalletApproval = deserializer.parseData(erc1155WithdrawalRequestJson)
//        allApprovalRequests.add(erc1155WithdrawalRequestWalletApproval)
//
//        val multiSigWithConversionRequestJson: JsonElement =
//            JsonParser.parseString(multiSigWithConversionRequestJson.trim())
//        val multiSigWithConversionRequestWalletApproval =
//            deserializer.parseData(multiSigWithConversionRequestJson)
//        allApprovalRequests.add(multiSigWithConversionRequestWalletApproval)
//
//        val conversionRequestJson: JsonElement =
//            JsonParser.parseString(conversionRequestJson.trim())
//        val conversionRequestWalletApproval = deserializer.parseData(conversionRequestJson)
//        allApprovalRequests.add(conversionRequestWalletApproval)
//
//        val multiSignWithDAppRequestJson: JsonElement =
//            JsonParser.parseString(multiSignWithDAppRequestJson)
//        val multiSignWithDAppRequestWalletApproval =
//            deserializer.parseData(multiSignWithDAppRequestJson)
//        allApprovalRequests.add(multiSignWithDAppRequestWalletApproval)
//
//        val dAppJson: JsonElement =
//            JsonParser.parseString(dappTransactionJson.trim())
//        val dAppWalletApproval = deserializer.parseData(dAppJson)
//        allApprovalRequests.add(dAppWalletApproval)
//
//        val acceptVaultInvitationJson: JsonElement =
//            JsonParser.parseString(acceptVaultInvitationJson.trim())
//        val acceptVaultInvitationApproval = deserializer.parseData(acceptVaultInvitationJson)
//        allApprovalRequests.add(acceptVaultInvitationApproval)
//
//        val passwordResetJson: JsonElement =
//            JsonParser.parseString(passwordResetJson.trim())
//        val passwordResetApproval = deserializer.parseData(passwordResetJson)
//        allApprovalRequests.add(passwordResetApproval)

        return allApprovalRequests
    }
}