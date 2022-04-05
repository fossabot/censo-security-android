package com.strikeprotocols.mobile.common

import com.strikeprotocols.mobile.data.models.*
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetails

fun generateInitialUserDummyData() = VerifyUser(
    fullName = "John Doe",
    hasApprovalPermission = false,
    id = "jondoe",
    loginName = "Johnny Doey",
    organization = Organization(
        id = "cryptoco",
        name = "cryptology"
    ),
    publicKeys = null,
    useStaticKey = false
)

fun generateWalletApprovalsDummyData() = WalletApproval(
    approvalTimeoutInSeconds = 18000,
    id = "03df25c845d460bcdad7802d2vf6fc1dfde97283bf75cc993eb6dca835ea2e2f",
    numberOfApprovalsReceived = 0,
    numberOfDeniesReceived = 0,
    numberOfDispositionsRequired = 1,
    submitDate = "2022-03-12T21:00:04.260+00:00",
    submitterEmail = "some_submitter@org.com",
    submitterName = "Some Submitter",
    walletType = "Solana",
    details = SolanaApprovalRequestDetails.ApprovalRequestDetails(SolanaApprovalRequestType.UnknownApprovalType)
)

fun generateRecentBlockhashDummyData() =
    "JtcoutZwsH8Xd6mtQEi3MXEDhDVMckeSfWYoPn7VnzNnDnBYACwRmZU2caCY1BGYQoZ"

object ValidDummyData {

    const val publicKey = "9uBGYQoZr6otJGU1FkBiRuabZPJsH8XdMckeSz9Bk8kn"
    const val encryptedPrivateKey = "4JtcoutZwfkKiu1vD5vNwNhCi6DgNZECSwCfESmY9VWYmoPn7VnzNnDnBYye2bEVk1TK8Pe2zaxDcFRdtF"
    const val decryptionKey = "R6mtQEi3MXEDhDVjduAhfdfCDWVhfWYACwRmZU2caCY1f6fHdszEsoMqvPx5"

    //Secret Key for this flow: Md27x1XnpF166Te2PNrR9rGr2V3uTH9my4eRpzcUzvEfYZSE75ijbFq4TcdM
    fun generateVerifyUserDummyDataWithValidPublicKey() = VerifyUser(
        fullName = "John Doe",
        hasApprovalPermission = false,
        id = "jondoe",
        loginName = "Johnny Doey",
        organization = Organization(
            id = "cryptoco",
            name = "cryptology"
        ),
        publicKeys = listOf(
            PublicKey(
                key = publicKey,
                walletType = WalletSigner.WALLET_TYPE_SOLANA
            )
        ),
        useStaticKey = false
    )

    fun generateVerifyWalletSignersDummyDataWithValidPublicKey() =
        listOf<WalletSigner>(
            WalletSigner(
                publicKey = publicKey,
                encryptedKey = encryptedPrivateKey,
                walletType = WalletSigner.WALLET_TYPE_SOLANA
            )
        )
}
