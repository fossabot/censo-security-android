package com.strikeprotocols.mobile.common

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.strikeprotocols.mobile.data.models.*
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.data.models.approval.WalletApprovalDeserializer

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
        publicKeys = emptyList(),
//        publicKeys = listOf(
//            WalletPublicKey(
//                key = publicKey,
//                walletType = WalletSigner.WALLET_TYPE_SOLANA
//            )
//        ),
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

object MockedApprovals {

    val signersUpdateJson = """
    {"id": "13cd643e-393e-4b38-91cb-5f1ab2655223", "walletType": "Solana", "submitDate": "2022-05-05T14:42:53.015+00:00", "submitterName": "User 2", "submitterEmail": "user2@org1", "approvalTimeoutInSeconds": 180000, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": {"type": "SignersUpdate", "slotUpdateType": "SetIfEmpty", "signer": {"slotId": 1, "value": {"publicKey": "HhNwcVMrJX8newbDVderrnsmvG6uGYuxUUvzm6BqdjzH", "name": "User 2", "email": "user2@org1"}}, "signingData": {"feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "GN694sm2Ex1GcnamYwqfjSs6XJ7xadTXiZqBwvGvQyT8", "multisigOpAccountAddress": "DHgNPbMHz66DQacFdo4rN8pks9Lw1zpfqqqAymHUgQkg", "walletAddress": "6JmmkmowSLQ3jFQacREDNwbrD3Hj7Eyj9MvK8eBTzV5q"}}}
""".trim()

    // Approval for balance account creation:
    val balanceAccountCreationJson = """
    {"id": "922b51bd-cd83-4a11-a7ce-156bf7573923", "walletType": "Solana", "submitDate": "2022-04-05T14:42:58.170+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 18000, "numberOfDispositionsRequired": 2, "numberOfApprovalsReceived": 1, "numberOfDeniesReceived": 0, "details": {"type": "BalanceAccountCreation", "accountSlot": 0, "accountInfo": {"identifier": "00a56503-e4cf-40a8-9eca-508093cf225e", "name": "Account 1", "accountType": "BalanceAccount"}, "approvalsRequired": 1, "approvalTimeout": 3600000, "approvers": [{"slotId": 0, "value": {"publicKey": "Ffs2XpnxtSBH5xTHQtece1jrmdVjT2syTceQHkjKXK1e", "name": "User 1", "email": "authorized1@org1"}}, {"slotId": 0, "value": {"publicKey": "Ffs2XpnxtSBH5xTHQtece1jrmdVjT2syTceQHkjKXK1e", "name": "User 2", "email": "authorized2@org1"}}, {"slotId": 0, "value": {"publicKey": "Ffs2XpnxtSBH5xTHQtece1jrmdVjT2syTceQHkjKXK1e", "name": "User 3", "email": "authorized3@org1"}}], "whitelistEnabled": "Off", "dappsEnabled": "Off", "addressBookSlot": 0, "stakingValidator": null, "signingData": {"feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "GN694sm2Ex1GcnamYwqfjSs6XJ7xadTXiZqBwvGvQyT8", "multisigOpAccountAddress": "Dv4oUQtQzz9ew11qb27XCQW95xocvnQgd5j2Xs451TAR", "walletAddress": "6JmmkmowSLQ3jFQacREDNwbrD3Hj7Eyj9MvK8eBTzV5q"}}}
""".trim()

    // Approval for removing a signer
    val signersUpdateRemovalJson = """
    {"id": "40a7fb51-a39c-4afa-aa3b-5c4f77939026", "walletType": "Solana", "submitDate": "2022-04-05T14:43:19.120+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 18000, "numberOfDispositionsRequired": 2, "numberOfApprovalsReceived": 1, "numberOfDeniesReceived": 0, "details": {"type": "SignersUpdate", "slotUpdateType": "Clear", "signer": {"slotId": 2, "value": {"publicKey": "HwW3iudK4dcqcMm76iy7iBziJU4htaYZGTjzCBnYA6rH", "name": "User 3", "email": "user3@org1"}}, "signingData": {"feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "GN694sm2Ex1GcnamYwqfjSs6XJ7xadTXiZqBwvGvQyT8", "multisigOpAccountAddress": "CngursCC8PNA7x3tP2jVXXrvgSWu1NtNCYMnzT5v4jnp", "walletAddress": "6JmmkmowSLQ3jFQacREDNwbrD3Hj7Eyj9MvK8eBTzV5q"}}}
""".trim()

    // Transfer approval
    val withdrawalRequestJson = """
    {"id": "0c949b8b-b190-43b3-8e35-8236206ff864", "walletType": "Solana", "submitDate": "2022-04-05T15:24:14.065+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 3600, "numberOfDispositionsRequired": 2, "numberOfApprovalsReceived": 1, "numberOfDeniesReceived": 0, "details": {"type": "WithdrawalRequest", "account": {"identifier": "caa3a0ee-dd90-43b7-9204-b5c87105ebb6", "name": "Account 1", "accountType": "BalanceAccount", "address": "3uCYEp6SRZ9YBMCSZ56492yiXE52nE5D4gPPap75FAha"}, "symbolAndAmountInfo": {"symbolInfo": {"symbol": "SOL", "symbolDescription": "Solana", "tokenMintAddress": "11111111111111111111111111111111"}, "amount": "0.500000000", "nativeAmount": "0.500000000", "usdEquivalent": "44.39"}, "destination": {"name": "My External Sol address", "address": "ApDG986BAFH3DDQmcHKHg3cDvYNhqxpbrJqCHH2cVmkE", "subName": "Sub"}, "signingData": {"feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "CpikHpeN5zVCSvGj8riDCz6iqz4CZ3FNqNq5qwjBHm3x", "multisigOpAccountAddress": "FbvmXsLJWwC9ww6X9aENTwnyFNBMQptZyZmvry3GNqDq", "walletAddress": "BcHw51RmaSLCAzd3Jy1UfQZHKdv4E993uGoqtrsreu1t"}}}
""".trim()

    // Conversion approval:
    val conversionRequestJson = """
    {"id": "ffc2b7de-ad6a-4a89-b57c-513d2cfb42a3", "walletType": "Solana", "submitDate": "2022-04-06T09:45:38.678+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 3600, "numberOfDispositionsRequired": 2, "numberOfApprovalsReceived": 1, "numberOfDeniesReceived": 0, "details": {"type": "ConversionRequest", "account": {"identifier": "22b5c380-9049-4905-af4d-cce44284cce6", "name": "Account 1", "accountType": "BalanceAccount", "address": "s4HnJVY6B4yBPY57csiYinGEeJsrHFZZScLxMNJr6Kk"}, "symbolAndAmountInfo": {"symbolInfo": {"symbol": "USDC", "symbolDescription": "USD Coin", "tokenMintAddress": "ALmJ9wWY2o1FiLcSDuvHN3xH5UHLkYsVbz2JWD37MuUY"}, "amount": "50000.000000", "nativeAmount": "500.000000", "usdEquivalent": "50000.00"}, "destination": {"name": "USDC Redemption Address", "address": "Emfuy1FWbVtNLgTum38rrK3EbkXonwxtNyPztwFi3r8a"}, "destinationSymbolInfo": {"symbol": "USD", "symbolDescription": "US Dollar", "tokenMintAddress": "11111111111111111111111111111111"}, "signingData": {"feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "ALKWER79Nt7AzhHwS99wWjXNtLvBCAHJYyAhEDYDVEpF", "multisigOpAccountAddress": "DpBrGAFpqspN2Fe46NJUPBxV6AnWKenuTpj4doK6Gt4p", "walletAddress": "AYsBiTxSFnqRooYiH2B6rrMRtVXRCrUTTXa3L121fUMB"}}}
""".trim()

    // Login Approval
    const val EXAMPLE_JWT_TOKEN =
        "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZCI6IlNvbHIifQ.SWCJDd6B_m7xr_puQH-wgbxvXyJYXH9lTpldOU0eQKc"
    val loginApprovalJson = """
    {"id": "13cd643e-393e-4b38-91cb-5f1ab2655223", "walletType": "Solana", "submitDate": "2022-04-05T14:42:53.015+00:00", "submitterName": "User 2", "submitterEmail": "user2@org1", "approvalTimeoutInSeconds": 18000, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": { "type": "LoginApproval", "jwtToken": "$EXAMPLE_JWT_TOKEN"}}
""".trim()

    val dAppJson = """
    {"id":"8ab353ee-7c91-4d39-b041-042352455c63","walletType":"Solana","submitDate":"2022-04-05T19:39:50.166+00:00","submitterName":"Ben Holzman","submitterEmail":"bholzman2@blue.rock","approvalTimeoutInSeconds":1800,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"DAppTransactionRequest","account":{"identifier":"5096c2f1-74de-4c2e-8a61-0024a83f14b3","name":"Trading","accountType":"BalanceAccount","address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD"},"balanceChanges":[{"symbolInfo":{"symbol":"SOL","symbolDescription":"Solana"},"amount":"0.023981600","nativeAmount":"0.023981600","usdEquivalent":"3.13"},{"symbolInfo":{"symbol":"SRM","symbolDescription":"Serum","tokenMintAddress":"SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt"},"amount":"-2.000000","nativeAmount":"2.000000","usdEquivalent":"5.94"}],"instructions":[{"from":0,"instructions":[{"programId":"11111111111111111111111111111111","accountMetas":[{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true}],"data":"AAAAAPAdHwAAAAAApQAAAAAAAAAG3fbh12Whk9nL4UbO63msHLSF7V9bN5E6jPWFfv8AqQ=="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"So11111111111111111111111111111111111111112","signer":false,"writable":false},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"SysvarRent111111111111111111111111111111111","signer":false,"writable":false}],"data":"AQ=="},{"programId":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","accountMetas":[{"address":"jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai","signer":false,"writable":true},{"address":"5KrN1vytDRuRxRDZc5EoTKGhXFqZiR7evy1zTR8irhwZ","signer":false,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"EhAJTsW745jiWjViB7Q4xXcgKf6tMF7RcMX9cbTuXVBk","signer":false,"writable":true},{"address":"HFSNnAxfhDt4DnmY9yVs2HNFnEMaDJ7RxMVNB9Y5Hgjr","signer":false,"writable":true},{"address":"FUH3FvpU6M7zNpaJ7fSyVD8UiaTWGxmbciwHxJACcEbA","signer":false,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"6vBhv2L33KVJvAQeiaW3JEZLrJU7TtGaqcwPdrhytYWG","signer":false,"writable":false},{"address":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","signer":false,"writable":false}],"data":"AAUAAAA="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true}],"data":"CQ=="}]}],"signingData":{"feePayer":"59CH4KuZWQpyGbkEtUhzM9KbYEsssJ7NgM89db5GehLj","walletProgramId":"6m1icfABEiCG3vm4w9YL9QBTc7AN4ApU9VY38XmQH9VC","multisigOpAccountAddress":"11111111111111111111111111111111","walletAddress":"HNNg5RDk1o35APqnrDUJniT6ngyZbisEcsAPTjzcPuPK"},"dappInfo":{"address":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","name":"Serum dApp","logo":"https://raw.githubusercontent.com/project-serum/awesome-serum/master/logo-serum.png","url":"https://serum-demo2.strikeprotocols.com"}}}
    """.trim()

    // Initiation for a balance account creation:
    val multiSigWithBalanceAccountCreationJson = """
    {"id": "922b51bd-cd83-4a11-a7ce-156bf7573923", "walletType": "Solana", "submitDate": "2022-04-05T14:42:58.170+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 1800, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": {"type": "MultisigOpInitiation", "details": {"type": "BalanceAccountCreation", "accountSlot": 0, "accountInfo": {"identifier": "00a56503-e4cf-40a8-9eca-508093cf225e", "name": "Account 1", "accountType": "BalanceAccount"}, "approvalsRequired": 1, "approvalTimeout": 3600000, "approvers": [{"slotId": 0, "value": {"publicKey": "Ffs2XpnxtSBH5xTHQtece1jrmdVjT2syTceQHkjKXK1e", "name": "User 1", "email": "authorized1@org1"}}], "whitelistEnabled": "Off", "dappsEnabled": "Off", "addressBookSlot": 0, "stakingValidator": null, "signingData": {"feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "GN694sm2Ex1GcnamYwqfjSs6XJ7xadTXiZqBwvGvQyT8", "multisigOpAccountAddress": "11111111111111111111111111111111", "walletAddress": "6JmmkmowSLQ3jFQacREDNwbrD3Hj7Eyj9MvK8eBTzV5q"}}, "opAccountCreationInfo": {"accountSize": 848, "minBalanceForRentExemption": 6792960}, "dataAccountCreationInfo": null}}
""".trim()

    // Initiation for removing an signer
    val multiSigWithSignersUpdateJson = """
{"id": "40a7fb51-a39c-4afa-aa3b-5c4f77939026", "walletType": "Solana", "submitDate": "2022-04-05T14:43:19.120+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 1800, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": {"type": "MultisigOpInitiation", "details": {"type": "SignersUpdate", "slotUpdateType": "Clear", "signer": {"slotId": 2, "value": {"publicKey": "HwW3iudK4dcqcMm76iy7iBziJU4htaYZGTjzCBnYA6rH", "name": "User 3", "email": "user3@org1"}}, "signingData": {"feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "GN694sm2Ex1GcnamYwqfjSs6XJ7xadTXiZqBwvGvQyT8", "multisigOpAccountAddress": "11111111111111111111111111111111", "walletAddress": "6JmmkmowSLQ3jFQacREDNwbrD3Hj7Eyj9MvK8eBTzV5q"}}, "opAccountCreationInfo": {"accountSize": 848, "minBalanceForRentExemption": 6792960}, "dataAccountCreationInfo": null}}
""".trim()

    // Transfer initiation:
    val multiSigWithWithdrawalRequestJson = """
{"id": "0c949b8b-b190-43b3-8e35-8236206ff864", "walletType": "Solana", "submitDate": "2022-04-05T15:24:14.065+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 1800, "numberOfDispositionsRequired": 2, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": {"type": "MultisigOpInitiation", "details": {"type": "WithdrawalRequest", "account": {"identifier": "caa3a0ee-dd90-43b7-9204-b5c87105ebb6", "name": "Account 1", "accountType": "BalanceAccount", "address": "3uCYEp6SRZ9YBMCSZ56492yiXE52nE5D4gPPap75FAha"}, "symbolAndAmountInfo": {"symbolInfo": {"symbol": "SOL", "symbolDescription": "Solana", "tokenMintAddress": "11111111111111111111111111111111"}, "amount": "0.500000000", "nativeAmount": "0.500000000", "usdEquivalent": "44.39"}, "destination": {"name": "My External Sol address", "address": "ApDG986BAFH3DDQmcHKHg3cDvYNhqxpbrJqCHH2cVmkE"}, "signingData": {"feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "CpikHpeN5zVCSvGj8riDCz6iqz4CZ3FNqNq5qwjBHm3x", "multisigOpAccountAddress": "11111111111111111111111111111111", "walletAddress": "BcHw51RmaSLCAzd3Jy1UfQZHKdv4E993uGoqtrsreu1t"}}, "opAccountCreationInfo": {"accountSize": 848, "minBalanceForRentExemption": 6792960}, "dataAccountCreationInfo": null}}
""".trim()


    // DApp initiation
    val multiSignWithDAppRequestJson = """
{"id":"8ab353ee-7c91-4d39-b041-042352455c63","walletType":"Solana","submitDate":"2022-04-05T19:39:50.166+00:00","submitterName":"Ben Holzman","submitterEmail":"bholzman2@blue.rock","approvalTimeoutInSeconds":1800,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"MultisigOpInitiation","details":{"type":"DAppTransactionRequest","account":{"identifier":"5096c2f1-74de-4c2e-8a61-0024a83f14b3","name":"Trading","accountType":"BalanceAccount","address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD"},"balanceChanges":[{"symbolInfo":{"symbol":"SOL","symbolDescription":"Solana"},"amount":"0.023981600","nativeAmount":"0.023981600","usdEquivalent":"3.13"},{"symbolInfo":{"symbol":"SRM","symbolDescription":"Serum","tokenMintAddress":"SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt"},"amount":"2.000000","nativeAmount":"2.000000","usdEquivalent":"5.94"}],"instructions":[{"from":0,"instructions":[{"programId":"11111111111111111111111111111111","accountMetas":[{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true}],"data":"AAAAAPAdHwAAAAAApQAAAAAAAAAG3fbh12Whk9nL4UbO63msHLSF7V9bN5E6jPWFfv8AqQ=="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"So11111111111111111111111111111111111111112","signer":false,"writable":false},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"SysvarRent111111111111111111111111111111111","signer":false,"writable":false}],"data":"AQ=="},{"programId":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","accountMetas":[{"address":"jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai","signer":false,"writable":true},{"address":"5KrN1vytDRuRxRDZc5EoTKGhXFqZiR7evy1zTR8irhwZ","signer":false,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"EhAJTsW745jiWjViB7Q4xXcgKf6tMF7RcMX9cbTuXVBk","signer":false,"writable":true},{"address":"HFSNnAxfhDt4DnmY9yVs2HNFnEMaDJ7RxMVNB9Y5Hgjr","signer":false,"writable":true},{"address":"FUH3FvpU6M7zNpaJ7fSyVD8UiaTWGxmbciwHxJACcEbA","signer":false,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"6vBhv2L33KVJvAQeiaW3JEZLrJU7TtGaqcwPdrhytYWG","signer":false,"writable":false},{"address":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","signer":false,"writable":false}],"data":"AAUAAAA="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true}],"data":"CQ=="}]}],"signingData":{"feePayer":"59CH4KuZWQpyGbkEtUhzM9KbYEsssJ7NgM89db5GehLj","walletProgramId":"6m1icfABEiCG3vm4w9YL9QBTc7AN4ApU9VY38XmQH9VC","multisigOpAccountAddress":"11111111111111111111111111111111","walletAddress":"HNNg5RDk1o35APqnrDUJniT6ngyZbisEcsAPTjzcPuPK"},"dappInfo":{"address":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","name":"Serum dApp","logo":"https://raw.githubusercontent.com/project-serum/awesome-serum/master/logo-serum.png","url":"https://serum-demo2.strikeprotocols.com"}},"opAccountCreationInfo":{"accountSize":848,"minBalanceForRentExemption":6792960},"dataAccountCreationInfo":{"accountSize":2696,"minBalanceForRentExemption":19655040}}}
""".trim()

    // Conversion initiation
    val multiSigWithConversionRequestJson = """
    {"id": "ffc2b7de-ad6a-4a89-b57c-513d2cfb42a3", "walletType": "Solana", "submitDate": "2022-04-06T09:45:38.678+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 1800, "numberOfDispositionsRequired": 2, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": {"type": "MultisigOpInitiation", "details": {"type": "ConversionRequest", "account": {"identifier": "22b5c380-9049-4905-af4d-cce44284cce6", "name": "Account 1", "accountType": "BalanceAccount", "address": "s4HnJVY6B4yBPY57csiYinGEeJsrHFZZScLxMNJr6Kk"}, "symbolAndAmountInfo": {"symbolInfo": {"symbol": "USDC", "symbolDescription": "USD Coin", "tokenMintAddress": "ALmJ9wWY2o1FiLcSDuvHN3xH5UHLkYsVbz2JWD37MuUY"}, "amount": "500.000000", "nativeAmount": "500.000000", "usdEquivalent": "500.00"}, "destination": {"name": "USDC Redemption Address", "address": "Emfuy1FWbVtNLgTum38rrK3EbkXonwxtNyPztwFi3r8a"}, "destinationSymbolInfo": {"symbol": "USD", "symbolDescription": "US Dollar", "tokenMintAddress": "11111111111111111111111111111111"}, "signingData": {"feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "ALKWER79Nt7AzhHwS99wWjXNtLvBCAHJYyAhEDYDVEpF", "multisigOpAccountAddress": "11111111111111111111111111111111", "walletAddress": "AYsBiTxSFnqRooYiH2B6rrMRtVXRCrUTTXa3L121fUMB"}}, "opAccountCreationInfo": {"accountSize": 848, "minBalanceForRentExemption": 6792960}, "dataAccountCreationInfo": null}}
""".trim()
//endregion

    fun getFullListOfApprovalItems(): List<WalletApproval> {
        val deserializer = WalletApprovalDeserializer()

        val allApprovalRequests = mutableListOf<WalletApproval>()

        val signersAsJsonElement: JsonElement = JsonParser.parseString(signersUpdateJson.trim())
        val signersUpdateWalletApproval = deserializer.parseData(signersAsJsonElement)
        allApprovalRequests.add(signersUpdateWalletApproval)

        val balanceAccountCreationJson: JsonElement =
            JsonParser.parseString(balanceAccountCreationJson.trim())
        val balanceAccountCreationWalletApproval =
            deserializer.parseData(balanceAccountCreationJson)
        allApprovalRequests.add(balanceAccountCreationWalletApproval)

        val withdrawalRequestJson: JsonElement =
            JsonParser.parseString(withdrawalRequestJson.trim())
        val withdrawalRequestWalletApproval = deserializer.parseData(withdrawalRequestJson)
        allApprovalRequests.add(withdrawalRequestWalletApproval)

        val conversionRequestJson: JsonElement =
            JsonParser.parseString(conversionRequestJson.trim())
        val conversionRequestWalletApproval = deserializer.parseData(conversionRequestJson)
        allApprovalRequests.add(conversionRequestWalletApproval)

        val signersRemovalAsJsonElement: JsonElement = JsonParser.parseString(signersUpdateRemovalJson.trim())
        val signersUpdateRemovalWalletApproval = deserializer.parseData(signersRemovalAsJsonElement)
        allApprovalRequests.add(signersUpdateRemovalWalletApproval)

        val dAppJson: JsonElement =
            JsonParser.parseString(dAppJson.trim())
        val dAppWalletApproval = deserializer.parseData(dAppJson)
        allApprovalRequests.add(dAppWalletApproval)

        val loginJson: JsonElement =
            JsonParser.parseString(loginApprovalJson.trim())
        val loginApproval = deserializer.parseData(loginJson)
        allApprovalRequests.add(loginApproval)

        val unknownApproval = conversionRequestWalletApproval.copy(
            details = SolanaApprovalRequestDetails.ApprovalRequestDetails(SolanaApprovalRequestType.UnknownApprovalType))
        allApprovalRequests.add(unknownApproval)

        val multiSigWithBalanceAccountCreationJson: JsonElement =
            JsonParser.parseString(multiSigWithBalanceAccountCreationJson.trim())
        val multiSigWithBalanceAccountCreationWalletApproval =
            deserializer.parseData(multiSigWithBalanceAccountCreationJson)
        allApprovalRequests.add(multiSigWithBalanceAccountCreationWalletApproval)

        val multiSigWithSignersUpdateJson: JsonElement =
            JsonParser.parseString(multiSigWithSignersUpdateJson.trim())
        val multiSigWithSignersUpdateWalletApproval =
            deserializer.parseData(multiSigWithSignersUpdateJson)
        allApprovalRequests.add(multiSigWithSignersUpdateWalletApproval)

        val multiSigWithWithdrawalRequestJson: JsonElement =
            JsonParser.parseString(multiSigWithWithdrawalRequestJson.trim())
        val multiSigWithWithdrawalRequestWalletApproval =
            deserializer.parseData(multiSigWithWithdrawalRequestJson)
        allApprovalRequests.add(multiSigWithWithdrawalRequestWalletApproval)

        val multiSigWithConversionRequestJson: JsonElement =
            JsonParser.parseString(multiSigWithConversionRequestJson.trim())
        val multiSigWithConversionRequestWalletApproval =
            deserializer.parseData(multiSigWithConversionRequestJson)
        allApprovalRequests.add(multiSigWithConversionRequestWalletApproval)

        val multiSignWithDAppRequestJson: JsonElement =
            JsonParser.parseString(multiSignWithDAppRequestJson)
        val multiSignWithDAppRequestWalletApproval =
            deserializer.parseData(multiSignWithDAppRequestJson)
        allApprovalRequests.add(multiSignWithDAppRequestWalletApproval)


        return allApprovalRequests
    }
}
