package com.strikeprotocols.mobile

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.strikeprotocols.mobile.data.models.approval.*
import org.junit.Assert.*
import org.junit.Test

class ParseApprovalRequestTypes {
    @Test
    fun parseAllApprovalRequestTypes() {
        val allApprovalRequests = getFullListOfApprovalItems()

        assertEquals(allApprovalRequests.size, 10)

        allApprovalRequests.forEach { approvalRequest ->
            assertNotNull(approvalRequest)
            assertNotNull(approvalRequest.details)

            val details = approvalRequest.details

            if (details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails) {
                assertNotEquals(details.requestType, SolanaApprovalRequestType.UnknownApprovalType)
            } else if (details is SolanaApprovalRequestDetails.ApprovalRequestDetails) {
                assertNotEquals(details.requestType, SolanaApprovalRequestType.UnknownApprovalType)
            }
        }
    }

    @Test
    fun testSerializingAndDeserializingWalletApproval() {
        val allApprovalRequests = getFullListOfApprovalItems()

        allApprovalRequests.forEach { walletApproval ->
            val details = walletApproval.details

            if (details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails) {
                assertNotEquals(details.requestType, SolanaApprovalRequestType.UnknownApprovalType)

                val asString = WalletApproval.toJson(walletApproval)
                assertNotNull(asString)

                val parsedWalletApproval = WalletApproval.fromJson(asString)
                assertEquals(walletApproval, parsedWalletApproval)

                val parsedDetails =
                    parsedWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
                assertNotNull(parsedDetails.multisigOpInitiation)
                assert(parsedDetails.requestType !is SolanaApprovalRequestType.UnknownApprovalType)
            } else if (details is SolanaApprovalRequestDetails.ApprovalRequestDetails) {
                assertNotEquals(details.requestType, SolanaApprovalRequestType.UnknownApprovalType)

                val asString = WalletApproval.toJson(walletApproval)
                assertNotNull(asString)

                val parsedWalletApproval = WalletApproval.fromJson(asString)
                assertEquals(walletApproval, parsedWalletApproval)

                val parsedDetails =
                    parsedWalletApproval.details as SolanaApprovalRequestDetails.ApprovalRequestDetails
                assert(parsedDetails.requestType !is SolanaApprovalRequestType.UnknownApprovalType)
            }
        }
    }

    private fun getFullListOfApprovalItems(): List<WalletApproval> {
        val deserializer = WalletApprovalDeserializer()

        val allApprovalRequests = mutableListOf<WalletApproval>()

        val signersAsJsonElement: JsonElement = JsonParser.parseString(signersUpdateJson.trim())
        val signersUpdateWalletApproval = deserializer.parseData(signersAsJsonElement)
        allApprovalRequests.add(signersUpdateWalletApproval)

        val multiSigWithBalanceAccountCreationJson: JsonElement =
            JsonParser.parseString(multiSigWithBalanceAccountCreationJson.trim())
        val multiSigWithBalanceAccountCreationWalletApproval =
            deserializer.parseData(multiSigWithBalanceAccountCreationJson)
        allApprovalRequests.add(multiSigWithBalanceAccountCreationWalletApproval)

        val balanceAccountCreationJson: JsonElement =
            JsonParser.parseString(balanceAccountCreationJson.trim())
        val balanceAccountCreationWalletApproval =
            deserializer.parseData(balanceAccountCreationJson)
        allApprovalRequests.add(balanceAccountCreationWalletApproval)

        val multiSigWithSignersUpdateJson: JsonElement =
            JsonParser.parseString(multiSigWithSignersUpdateJson.trim())
        val multiSigWithSignersUpdateWalletApproval =
            deserializer.parseData(multiSigWithSignersUpdateJson)
        allApprovalRequests.add(multiSigWithSignersUpdateWalletApproval)

        val signersUpdateRemovalJson: JsonElement =
            JsonParser.parseString(signersUpdateRemovalJson.trim())
        val signersUpdateRemovalWalletApproval = deserializer.parseData(signersUpdateRemovalJson)
        allApprovalRequests.add(signersUpdateRemovalWalletApproval)

        val multiSigWithWithdrawalRequestJson: JsonElement =
            JsonParser.parseString(multiSigWithWithdrawalRequestJson.trim())
        val multiSigWithWithdrawalRequestWalletApproval =
            deserializer.parseData(multiSigWithWithdrawalRequestJson)
        allApprovalRequests.add(multiSigWithWithdrawalRequestWalletApproval)

        val withdrawalRequestJson: JsonElement =
            JsonParser.parseString(withdrawalRequestJson.trim())
        val withdrawalRequestWalletApproval = deserializer.parseData(withdrawalRequestJson)
        allApprovalRequests.add(withdrawalRequestWalletApproval)

        val multiSigWithConversionRequestJson: JsonElement =
            JsonParser.parseString(multiSigWithConversionRequestJson.trim())
        val multiSigWithConversionRequestWalletApproval =
            deserializer.parseData(multiSigWithConversionRequestJson)
        allApprovalRequests.add(multiSigWithConversionRequestWalletApproval)

        val conversionRequestJson: JsonElement =
            JsonParser.parseString(conversionRequestJson.trim())
        val conversionRequestWalletApproval = deserializer.parseData(conversionRequestJson)
        allApprovalRequests.add(conversionRequestWalletApproval)

        val multiSignWithDAppRequestJson: JsonElement =
            JsonParser.parseString(multiSignWithDAppRequestJson)
        val multiSignWithDAppRequestWalletApproval =
            deserializer.parseData(multiSignWithDAppRequestJson)
        allApprovalRequests.add(multiSignWithDAppRequestWalletApproval)

        return allApprovalRequests
    }
}