package com.strikeprotocols.mobile.common

import com.strikeprotocols.mobile.data.models.*

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
    details = Details(
        type = "SampleRequest",
        signingData = SigningData(
            feePayer = "6WF1VwByGfW31Nmk1xcfz9k5YyZLskX4659JXyAA3rpb",
            multisigOpAccountAddress = "8WF1VwByGfW31Nmk1xcfz9k5YyZLskX4659JXyAA3rpb",
            walletAddress = "9WF1VwByGfW31Nmk1xcfz9k5YyZLskX4659JXyAA3rpb",
            walletProgramId = "7WF1VwByGfW31Nmk1xcfz9k5YyZLskX4659JXyAA3rpb"
        )
    ),
    id = "03df25c845d460bcdad7802d2vf6fc1dfde97283bf75cc993eb6dca835ea2e2f",
    numberOfApprovalsReceived = 0,
    numberOfDeniesReceived = 0,
    numberOfDispositionsRequired = 1,
    submitDate = "2022-03-12T21:00:04.260+00:00",
    submitterEmail = "some_submitter@org.com",
    submitterName = "Some Submitter",
    walletType = "Solana"
)


object ValidDummyData {

    const val publicKey = "2HW9sVHwgirkdh5Exzoz7QAoQx3gC7LX2xJtF5ESHk87"
    const val encryptedPrivateKey = "Ym8Aqz1xuYS7s6ahcodFVrJJoUaMLq54nxgXVQy1EtAxeKYbUvoaAsWDJr7tUKrQUxgTsEn2JVVQn7sQAB"
    const val decryptionKey = "Md27x1XnpF166Te2PNrR9rGr2V3uTH9my4eRpzcUzvEfYZSE75ijbFq4TcdM"

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

    fun generateVerifyWalletSignersDummyDataWithValidPublicKey() = WalletSigners(
        items = listOf<WalletSigner>(
            WalletSigner(
                publicKey = publicKey,
                encryptedKey = encryptedPrivateKey,
                walletType = WalletSigner.WALLET_TYPE_SOLANA
            )
        )
    )

}
