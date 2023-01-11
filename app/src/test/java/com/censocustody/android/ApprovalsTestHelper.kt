package com.censocustody.android

import com.google.gson.JsonParser
import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.Organization
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.models.WalletPublicKey
import com.censocustody.android.data.models.approval.ApprovalRequest
import com.censocustody.android.data.models.approval.ApprovalRequestDeserializer

private val deserializer = ApprovalRequestDeserializer()

fun getWalletApprovals() : List<ApprovalRequest> {
    val approvals = mutableListOf<ApprovalRequest>()

    val nonceAccountAddresses = listOf(getNonce())

    val signersUpdateRequestType =
        getSignersUpdateRequest(nonceAccountAddresses)
    val signersUpdateApproval =
        getWalletApprovalRequest(signersUpdateRequestType)

    approvals.add(signersUpdateApproval)

    val unwrapConversionRequestType =
        getUnwrapConversionRequest(nonceAccountAddresses)
    val unwrapConversionApproval =
        getWalletApprovalRequest(unwrapConversionRequestType)

    approvals.add(unwrapConversionApproval)

    val createAddressBookEntryRequestType =
        getCreateSolanaAddressBookEntry(nonceAccountAddresses)
    val addCreateBookEntryApproval =
        getWalletApprovalRequest(createAddressBookEntryRequestType)

    approvals.add(addCreateBookEntryApproval)

    val deleteAddressBookEntryRequestType =
        getDeleteSolanaAddressBookEntry(nonceAccountAddresses)
    val deleteAddressBookEntryApproval =
        getWalletApprovalRequest(deleteAddressBookEntryRequestType)

    approvals.add(deleteAddressBookEntryApproval)

    val addDAppBookEntryRequestType =
        getAddDAppBookEntry(nonceAccountAddresses)
    val addDAppBookEntryApproval =
        getWalletApprovalRequest(addDAppBookEntryRequestType)

    approvals.add(addDAppBookEntryApproval)

    val walletConfigPolicyUpdateRequestType =
        getWalletConfigPolicyUpdate(nonceAccountAddresses)
    val walletConfigPolicyUpdateApproval =
        getWalletApprovalRequest(walletConfigPolicyUpdateRequestType)

    approvals.add(walletConfigPolicyUpdateApproval)

    val balanceAccountPolicyUpdateRequestType =
        getBalanceAccountPolicyUpdate(nonceAccountAddresses)
    val balanceAccountPolicyUpdateApproval =
        getWalletApprovalRequest(balanceAccountPolicyUpdateRequestType)

    approvals.add(balanceAccountPolicyUpdateApproval)

    val balanceAccountNameUpdateRequestType =
        getBalanceAccountNameUpdate(nonceAccountAddresses)
    val balanceAccountNameUpdateApproval =
        getWalletApprovalRequest(balanceAccountNameUpdateRequestType)

    approvals.add(balanceAccountNameUpdateApproval)

    val dAppTransactionRequestType =
        getDAppTransactionRequest(nonceAccountAddresses)
    val dAppTransactionApproval =
        getWalletApprovalRequest(dAppTransactionRequestType)

    approvals.add(dAppTransactionApproval)

    val loginApprovalRequestType =
        getLoginApproval("jwttoken")
    val loginApproval =
        getWalletApprovalRequest(loginApprovalRequestType)

    approvals.add(loginApproval)

    val balanceAccountAddressWhitelistUpdateRequestType =
        getBalanceAccountAddressWhitelistUpdate(nonceAccountAddresses)
    val balanceAccountAddressWhitelistUpdateApproval =
        getWalletApprovalRequest(balanceAccountAddressWhitelistUpdateRequestType)

    approvals.add(balanceAccountAddressWhitelistUpdateApproval)

    val balanceAccountSettingsUpdateRequestType =
        getBalanceAccountSettingsUpdate(nonceAccountAddresses)
    val balanceAccountSettingsUpdateApproval =
        getWalletApprovalRequest(balanceAccountSettingsUpdateRequestType)

    approvals.add(balanceAccountSettingsUpdateApproval)

    val removeDAppBookEntryRequestType =
        getRemoveDAppBookEntry(nonceAccountAddresses)
    val removeDAppBookEntryApproval =
        getWalletApprovalRequest(removeDAppBookEntryRequestType)

    approvals.add(removeDAppBookEntryApproval)

    return approvals
}

fun getLoginApproval() : ApprovalRequest {
    val loginApprovalRequestType =
        getLoginApproval("jwttoken")

    return getWalletApprovalRequest(loginApprovalRequestType)
}

fun getRemoveDAppBookEntryApproval() : ApprovalRequest {
    val nonceAccountAddresses = listOf(getNonce())

    val removeDAppBookEntryRequestType =
        getRemoveDAppBookEntry(nonceAccountAddresses)

    return getWalletApprovalRequest(removeDAppBookEntryRequestType)
}

fun getMultiSigWalletCreationApprovalRequest(): ApprovalRequest =
    deserializer.parseData(JsonParser.parseString(multiSigWithWalletCreationJson.trim()))

fun getVerifyUser() = VerifyUser(
    fullName = "John Doe",
    hasApprovalPermission = false,
    id = "jondoe",
    loginName = "Johnny Doey",
    organization = Organization(
        id = "crypto",
        name = "cryptology"
    ),
    publicKeys = listOf(WalletPublicKey(key = ExampleMnemonicAndKeys.PUBLIC_KEY, chain = Chain.solana)),
    useStaticKey = false,
    deviceKey = ""
)

class ExampleMnemonicAndKeys {
    companion object {
        val MNEMONIC = "lizard size puppy joke venue census need net produce age all proof opinion promote setup flight tortoise multiply blanket problem defy arrest switch also"
        val BAD_MNEMONIC = "lizard trigger puppy joke venue census need net produce age all proof opinion promote setup flight tortoise multiply blanket problem defy arrest switch also"
        val PRIVATE_KEY = "xprvA2mmuoFKSeeWa7CaZwqLBL6Xuvz9q5XwGh3imXraW2XUeDhy4GA7SJkbgtWZV2vz3ctDzR4dMAs4um1Yj2Ad7iEaonyUzd5JicxR1gprJuY"
        val PUBLIC_KEY = "xpub6Fm8KJnDH2ConbH3fyNLYU3GTxpeEYFnduyKZvGC4N4TX237boUMz755YB3T18k1KdYrboxCRtiBppA3DmWXqmxebjyzoyyWrzXRTDeVoax"
    }
}

fun getNonce() = "7Zpss7rbtz6qU71ywcjcuANnVyQWJrqsZ3oekkR9Hknn"

enum class ResourceState {
    SUCCESS, ERROR
}