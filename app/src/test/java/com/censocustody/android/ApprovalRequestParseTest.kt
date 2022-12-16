package com.censocustody.android

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.censocustody.android.data.models.approval.*
import org.junit.Assert.*
import org.junit.Test

class ParseApprovalRequestTypes {

    private val mockUriWrapper = MockUriWrapper()

    @Test
    fun parseAllApprovalRequestTypes() {
        val allApprovalRequests = getFullListOfApprovalItems()

        assertEquals(16, allApprovalRequests.size)

        allApprovalRequests.forEach { approvalRequest ->
            assertNotNull(approvalRequest)
            assertNotNull(approvalRequest.details)

            val details = approvalRequest.details

            if (details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails) {
                assertNotEquals(details.requestType, ApprovalRequestDetails.UnknownApprovalType)
            } else if (details is SolanaApprovalRequestDetails.ApprovalRequestDetails) {
                assertNotEquals(details.requestType, ApprovalRequestDetails.UnknownApprovalType)
            }
        }
    }

    @Test
    fun testSerializingAndDeserializingWalletApproval() {
        val allApprovalRequests = getFullListOfApprovalItems()

        allApprovalRequests.forEach { walletApproval ->
            val details = walletApproval.details

            if (details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails) {
                assertNotEquals(details.requestType, ApprovalRequestDetails.UnknownApprovalType)

                val asString = ApprovalRequest.toJson(walletApproval, mockUriWrapper)
                assertNotNull(asString)

                val parsedApprovalRequest = ApprovalRequest.fromJson(asString)
                assertEquals(walletApproval, parsedApprovalRequest)

                val parsedDetails =
                    parsedApprovalRequest.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
                assertNotNull(parsedDetails.multisigOpInitiation)
                assert(parsedDetails.requestType !is ApprovalRequestDetails.UnknownApprovalType)
            } else if (details is SolanaApprovalRequestDetails.ApprovalRequestDetails) {
                assertNotEquals(details.requestType, ApprovalRequestDetails.UnknownApprovalType)

                val asString = ApprovalRequest.toJson(walletApproval, mockUriWrapper)
                assertNotNull(asString)

                val parsedApprovalRequest = ApprovalRequest.fromJson(asString)
                assertEquals(walletApproval, parsedApprovalRequest)

                val parsedDetails =
                    parsedApprovalRequest.details as SolanaApprovalRequestDetails.ApprovalRequestDetails
                assert(parsedDetails.requestType !is ApprovalRequestDetails.UnknownApprovalType)

                if(details.requestType is ApprovalRequestDetails.DAppTransactionRequest) {
                    println(details.requestType)
                }
            }
        }
    }

    @Test
    fun testSignDataWithPlainStringApprovalParsing() {
        val deserializer = ApprovalRequestDeserializer()
        val signDataAsJsonElement: JsonElement = JsonParser.parseString(
            signDataWithPlainStringInitiationJson.trim())
        val signDataApproval = deserializer.parseData(signDataAsJsonElement)

        assertNotNull(signDataApproval)
        assertNotNull(signDataApproval.details)

        val details = signDataApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
        assertNotEquals(details.requestType, ApprovalRequestDetails.UnknownApprovalType)
        assertEquals(
            details.requestType::class.java,
            ApprovalRequestDetails.SignData::class.java
        )
        assertNull((details.requestType as ApprovalRequestDetails.SignData).signingData.base64DataToSign)
    }

    @Test
    fun testLoginApprovalParsing() {
        val deserializer = ApprovalRequestDeserializer()
        val signersAsJsonElement: JsonElement = JsonParser.parseString(loginApprovalJson.trim())
        val loginApprovalWalletApproval = deserializer.parseData(signersAsJsonElement)

        assertNotNull(loginApprovalWalletApproval)
        assertNotNull(loginApprovalWalletApproval.details)

        val details =
            loginApprovalWalletApproval.details as SolanaApprovalRequestDetails.ApprovalRequestDetails
        assertNotEquals(details.requestType, ApprovalRequestDetails.UnknownApprovalType)
        assertEquals(
            details.requestType::class.java,
            ApprovalRequestDetails.LoginApprovalRequest::class.java
        )

        val type = details.requestType as ApprovalRequestDetails.LoginApprovalRequest

        assertNotNull(type.jwtToken)
        assertEquals(type.type, ApprovalType.LOGIN_TYPE.value)
    }

    @Test
    fun testSerializingAndDeserializingLoginApproval() {
        val deserializer = ApprovalRequestDeserializer()
        val signersAsJsonElement: JsonElement = JsonParser.parseString(loginApprovalJson.trim())
        val loginApprovalWalletApproval = deserializer.parseData(signersAsJsonElement)

        val details =
            loginApprovalWalletApproval.details as SolanaApprovalRequestDetails.ApprovalRequestDetails

        assertNotEquals(details.requestType, ApprovalRequestDetails.UnknownApprovalType)

        val asString = ApprovalRequest.toJson(loginApprovalWalletApproval, mockUriWrapper)
        assertNotNull(asString)

        val parsedApprovalRequest = ApprovalRequest.fromJson(asString)
        assertEquals(loginApprovalWalletApproval, parsedApprovalRequest)

        val parsedDetails =
            parsedApprovalRequest.details as SolanaApprovalRequestDetails.ApprovalRequestDetails
        assert(parsedDetails.requestType !is ApprovalRequestDetails.UnknownApprovalType)
        assert(parsedDetails.requestType is ApprovalRequestDetails.LoginApprovalRequest)

        val type = parsedDetails.requestType as ApprovalRequestDetails.LoginApprovalRequest

        assertNotNull(type.jwtToken)
        assertEquals(type.type, ApprovalType.LOGIN_TYPE.value)
    }

    private fun getFullListOfApprovalItems(): List<ApprovalRequest> {
        val deserializer = ApprovalRequestDeserializer()

        val allApprovalRequests = mutableListOf<ApprovalRequest>()

        val signersAsJsonElement: JsonElement = JsonParser.parseString(signersUpdateJson.trim())
        val signersUpdateWalletApproval = deserializer.parseData(signersAsJsonElement)
        allApprovalRequests.add(signersUpdateWalletApproval)

        val multiSigWithWalletCreationJson: JsonElement =
            JsonParser.parseString(multiSigWithWalletCreationJson.trim())
        val multiSigWithWalletCreationApprovalRequest =
            deserializer.parseData(multiSigWithWalletCreationJson)
        allApprovalRequests.add(multiSigWithWalletCreationApprovalRequest)

        val solanaWalletCreationJson: JsonElement =
            JsonParser.parseString(solanaWalletCreationJson.trim())
        val solanaWalletCreationApprovalRequest =
            deserializer.parseData(solanaWalletCreationJson)
        allApprovalRequests.add(solanaWalletCreationApprovalRequest)

        val bitcoinWalletCreationJson: JsonElement =
            JsonParser.parseString(bitcoinWalletCreationJson.trim())
        val bitcoinWalletCreationApprovalRequest =
            deserializer.parseData(bitcoinWalletCreationJson)
        allApprovalRequests.add(bitcoinWalletCreationApprovalRequest)

        val ethereumWalletCreationJson: JsonElement =
            JsonParser.parseString(ethereumWalletCreationJson.trim())
        val ethereumWalletCreationApprovalRequest =
            deserializer.parseData(ethereumWalletCreationJson)
        allApprovalRequests.add(ethereumWalletCreationApprovalRequest)

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

        val bitcoinWithdrawalRequestJson: JsonElement =
            JsonParser.parseString(bitcoinWithdrawalRequestJson.trim())
        val bitcoinWithdrawalRequestWalletApproval = deserializer.parseData(bitcoinWithdrawalRequestJson)
        allApprovalRequests.add(bitcoinWithdrawalRequestWalletApproval)

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

        val dAppJson: JsonElement =
            JsonParser.parseString(dappTransactionJson.trim())
        val dAppWalletApproval = deserializer.parseData(dAppJson)
        allApprovalRequests.add(dAppWalletApproval)

        val acceptVaultInvitationJson: JsonElement =
            JsonParser.parseString(acceptVaultInvitationJson.trim())
        val acceptVaultInvitationApproval = deserializer.parseData(acceptVaultInvitationJson)
        allApprovalRequests.add(acceptVaultInvitationApproval)

        val passwordResetJson: JsonElement =
            JsonParser.parseString(passwordResetJson.trim())
        val passwordResetApproval = deserializer.parseData(passwordResetJson)
        allApprovalRequests.add(passwordResetApproval)

        return allApprovalRequests
    }
}