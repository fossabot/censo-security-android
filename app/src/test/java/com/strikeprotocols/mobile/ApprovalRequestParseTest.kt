package com.strikeprotocols.mobile

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.strikeprotocols.mobile.common.MockedApprovals
import com.strikeprotocols.mobile.data.models.approval.*
import org.junit.Assert.*
import org.junit.Test

class ParseApprovalRequestTypes {

    private val mockUriWrapper = MockUriWrapper()

    @Test
    fun parseAllApprovalRequestTypes() {
        val allApprovalRequests = getFullListOfApprovalItems()

        assertEquals(allApprovalRequests.size, 21)

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

                val asString = WalletApproval.toJson(walletApproval, mockUriWrapper)
                assertNotNull(asString)

                val parsedWalletApproval = WalletApproval.fromJson(asString)
                assertEquals(walletApproval, parsedWalletApproval)

                val parsedDetails =
                    parsedWalletApproval.details as SolanaApprovalRequestDetails.MultiSignOpInitiationDetails
                assertNotNull(parsedDetails.multisigOpInitiation)
                assert(parsedDetails.requestType !is SolanaApprovalRequestType.UnknownApprovalType)
            } else if (details is SolanaApprovalRequestDetails.ApprovalRequestDetails) {
                assertNotEquals(details.requestType, SolanaApprovalRequestType.UnknownApprovalType)

                val asString = WalletApproval.toJson(walletApproval, mockUriWrapper)
                assertNotNull(asString)

                val parsedWalletApproval = WalletApproval.fromJson(asString)
                assertEquals(walletApproval, parsedWalletApproval)

                val parsedDetails =
                    parsedWalletApproval.details as SolanaApprovalRequestDetails.ApprovalRequestDetails
                assert(parsedDetails.requestType !is SolanaApprovalRequestType.UnknownApprovalType)
            }
        }
    }

    @Test
    fun testLoginApprovalParsing() {
        val deserializer = WalletApprovalDeserializer()
        val signersAsJsonElement: JsonElement = JsonParser.parseString(loginApprovalJson.trim())
        val loginApprovalWalletApproval = deserializer.parseData(signersAsJsonElement)

        assertNotNull(loginApprovalWalletApproval)
        assertNotNull(loginApprovalWalletApproval.details)

        val details =
            loginApprovalWalletApproval.details as SolanaApprovalRequestDetails.ApprovalRequestDetails
        assertNotEquals(details.requestType, SolanaApprovalRequestType.UnknownApprovalType)
        assertEquals(
            details.requestType::class.java,
            SolanaApprovalRequestType.LoginApprovalRequest::class.java
        )

        val type = details.requestType as SolanaApprovalRequestType.LoginApprovalRequest

        assertNotNull(type.jwtToken)
        assertEquals(type.type, ApprovalType.LOGIN_TYPE.value)
    }

    @Test
    fun testSerializingAndDeserializingLoginApproval() {
        val deserializer = WalletApprovalDeserializer()
        val signersAsJsonElement: JsonElement = JsonParser.parseString(loginApprovalJson.trim())
        val loginApprovalWalletApproval = deserializer.parseData(signersAsJsonElement)

        val details =
            loginApprovalWalletApproval.details as SolanaApprovalRequestDetails.ApprovalRequestDetails

        assertNotEquals(details.requestType, SolanaApprovalRequestType.UnknownApprovalType)

        val asString = WalletApproval.toJson(loginApprovalWalletApproval, mockUriWrapper)
        assertNotNull(asString)

        val parsedWalletApproval = WalletApproval.fromJson(asString)
        assertEquals(loginApprovalWalletApproval, parsedWalletApproval)

        val parsedDetails =
            parsedWalletApproval.details as SolanaApprovalRequestDetails.ApprovalRequestDetails
        assert(parsedDetails.requestType !is SolanaApprovalRequestType.UnknownApprovalType)
        assert(parsedDetails.requestType is SolanaApprovalRequestType.LoginApprovalRequest)

        val type = parsedDetails.requestType as SolanaApprovalRequestType.LoginApprovalRequest

        assertNotNull(type.jwtToken)
        assertEquals(type.type, ApprovalType.LOGIN_TYPE.value)
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

        val dAppJson: JsonElement =
            JsonParser.parseString(dAppJson.trim())
        val dAppWalletApproval = deserializer.parseData(dAppJson)
        allApprovalRequests.add(dAppWalletApproval)

        val balanceAccountPolicyChangeJson: JsonElement =
            JsonParser.parseString(balanceAccountPolicyChangeJson.trim())
        val balanceAccountPolicyChangeWalletApproval = deserializer.parseData(balanceAccountPolicyChangeJson)
        allApprovalRequests.add(balanceAccountPolicyChangeWalletApproval)

        val balanceAccountSettingsChangeOneJsonElement: JsonElement =
            JsonParser.parseString(balanceAccountSettingsChangeOneJson.trim())
        val balanceAccountSettingsChangeOneWalletApproval = deserializer.parseData(balanceAccountSettingsChangeOneJsonElement)
        allApprovalRequests.add(balanceAccountSettingsChangeOneWalletApproval)

        val balanceAccountSettingsChangeTwoJson: JsonElement =
            JsonParser.parseString(balanceAccountSettingsChangeTwoJson.trim())
        val balanceAccountSettingsChangeTwoWalletApproval = deserializer.parseData(balanceAccountSettingsChangeTwoJson)
        allApprovalRequests.add(balanceAccountSettingsChangeTwoWalletApproval)

        val balanceAccountNameChangeJson: JsonElement =
            JsonParser.parseString(balanceAccountNameChangeJson.trim())
        val balanceAccountNameChangeWalletApproval = deserializer.parseData(balanceAccountNameChangeJson)
        allApprovalRequests.add(balanceAccountNameChangeWalletApproval)

        val walletConfigPolicyChangeJson: JsonElement =
            JsonParser.parseString(walletConfigPolicyChangeJson.trim())
        val walletConfigPolicyChangeWalletApproval = deserializer.parseData(walletConfigPolicyChangeJson)
        allApprovalRequests.add(walletConfigPolicyChangeWalletApproval)

        val splTokenAccountCreationJson: JsonElement =
            JsonParser.parseString(splTokenAccountCreationJson.trim())
        val splTokenAccountCreationWalletApproval = deserializer.parseData(splTokenAccountCreationJson)
        allApprovalRequests.add(splTokenAccountCreationWalletApproval)

        val wrapConversionJson: JsonElement =
            JsonParser.parseString(wrapConversionJson.trim())
        val wrapConversionWalletApproval = deserializer.parseData(wrapConversionJson)
        allApprovalRequests.add(wrapConversionWalletApproval)

        val addressBookAddJson: JsonElement =
            JsonParser.parseString(addressBookAddJson.trim())
        val addressBookAddWalletApproval = deserializer.parseData(addressBookAddJson)
        allApprovalRequests.add(addressBookAddWalletApproval)

        val balanceAccountWhitelistingUpdateJson: JsonElement =
            JsonParser.parseString(balanceAccountWhitelistingUpdateJson.trim())
        val balanceAccountWhitelistingUpdateWalletApproval = deserializer.parseData(balanceAccountWhitelistingUpdateJson)
        allApprovalRequests.add(balanceAccountWhitelistingUpdateWalletApproval)

        val dappBookUpdateJson: JsonElement =
            JsonParser.parseString(dappBookUpdateJson.trim())
        val dappBookUpdateWalletApproval = deserializer.parseData(dappBookUpdateJson)
        allApprovalRequests.add(dappBookUpdateWalletApproval)

        return allApprovalRequests
    }
}