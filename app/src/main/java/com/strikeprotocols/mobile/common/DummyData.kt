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
