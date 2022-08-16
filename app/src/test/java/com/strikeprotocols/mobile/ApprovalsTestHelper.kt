package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.data.models.Organization
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.approval.WalletApproval

fun getWalletApprovals() : List<WalletApproval> {
    val approvals = mutableListOf<WalletApproval>()

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

    val addAddressBookEntryRequestType =
        getAddAddressBookEntry(nonceAccountAddresses)
    val addAddressBookEntryApproval =
        getWalletApprovalRequest(addAddressBookEntryRequestType)

    approvals.add(addAddressBookEntryApproval)

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

fun getUserEmail() = "jdoe@crypto.org"

fun getVerifyUser() = VerifyUser(
    fullName = "John Doe",
    hasApprovalPermission = false,
    id = "jondoe",
    loginName = "Johnny Doey",
    organization = Organization(
        id = "crypto",
        name = "cryptology"
    ),
    publicKeys = null,
    useStaticKey = false
)

fun getNonce() = "7Zpss7rbtz6qU71ywcjcuANnVyQWJrqsZ3oekkR9Hknn"

enum class ResourceState {
    SUCCESS, ERROR
}