package com.strikeprotocols.mobile.common

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.data.models.*
import com.strikeprotocols.mobile.data.models.approval.*
import java.util.*

class GeneralDummyData {

    init {
        if(!BuildConfig.DEBUG) {
            throw Exception("Do not use dummy data outside of debug")
        }
    }

    fun emptyWalletSigner() = WalletSigner(
        publicKey = "",
        walletType = ""
    )

    fun generateInitialUserDummyData() =
        VerifyUser(
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
        vaultName = "Test Vault",
        details = SolanaApprovalRequestDetails.ApprovalRequestDetails(SolanaApprovalRequestType.UnknownApprovalType)
    )

    fun generateRecentBlockhashDummyData() =
        "JtcoutZwsH8Xd6mtQEi3MXEDhDVMckeSfWYoPn7VnzNnDnBYACwRmZU2caCY1BGYQoZ"

    class PhraseDummyData {

        companion object {
            //Please note this is to be used for mocked data purposes ONLY
            const val PHRASE =
                "wedding avoid come master casual have trend maid clump fly gain alter wagon bid rely lava foot buddy orchard already force tent just ladder"

            const val PHRASE_PRIVATE_KEY = "8U8sunPpaWPefPTy4wTwuRkgE9aCFa7iCjvbXRpuw1JY"
            const val PHRASE_PUBLIC_KEY = "GHNH45yPvJJKvzsJNjxqQWpmeWDF4yU5eWoS21Kqsaia"
        }

        fun generateVerifyWalletSignersDummyDataWithValidPublicKey() =
            listOf(
                WalletSigner(
                    publicKey = PHRASE_PUBLIC_KEY,
                    walletType = WalletSigner.WALLET_TYPE_SOLANA
                )
            )

        fun generateVerifyUserDummyDataWithValidKeyPair() : VerifyUser {
            return VerifyUser(
                fullName = null,
                hasApprovalPermission = null,
                id = null,
                loginName = null,
                organization = null,
                publicKeys = listOf(
                    WalletPublicKey(
                        key = PHRASE_PUBLIC_KEY,
                        walletType = null
                    )
                ),
                useStaticKey = null
            )
        }

    }
}

class MockedApprovals {

    init {
        if(!BuildConfig.DEBUG) {
            throw Exception("Do not use mocked data outside of debug")
        }
    }

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
    val EXAMPLE_JWT_TOKEN =
        "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZCI6IlNvbHIifQ.SWCJDd6B_m7xr_puQH-wgbxvXyJYXH9lTpldOU0eQKc"
    val loginApprovalJson = """
    {"id": "13cd643e-393e-4b38-91cb-5f1ab2655223", "walletType": "Solana", "submitDate": "2022-04-05T14:42:53.015+00:00", "submitterName": "User 2", "submitterEmail": "user2@org1", "approvalTimeoutInSeconds": 18000, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": { "type": "LoginApproval", "jwtToken": "$EXAMPLE_JWT_TOKEN"}}
""".trim()

    val dAppJson = """
    {"id":"8ab353ee-7c91-4d39-b041-042352455c63","walletType":"Solana","submitDate":"2022-04-05T19:39:50.166+00:00","submitterName":"Ben Holzman","submitterEmail":"bholzman2@blue.rock","approvalTimeoutInSeconds":1800,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"DAppTransactionRequest","account":{"identifier":"5096c2f1-74de-4c2e-8a61-0024a83f14b3","name":"Trading","accountType":"BalanceAccount","address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD"},"balanceChanges":[{"symbolInfo":{"symbol":"SOL","symbolDescription":"Solana"},"amount":"0.023981600","nativeAmount":"0.023981600","usdEquivalent":"3.13"},{"symbolInfo":{"symbol":"SRM","symbolDescription":"Serum","tokenMintAddress":"SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt"},"amount":"-2.000000","nativeAmount":"2.000000","usdEquivalent":"5.94"}],"instructions":[{"from":0,"instructions":[{"programId":"11111111111111111111111111111111","accountMetas":[{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true}],"data":"AAAAAPAdHwAAAAAApQAAAAAAAAAG3fbh12Whk9nL4UbO63msHLSF7V9bN5E6jPWFfv8AqQ=="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"So11111111111111111111111111111111111111112","signer":false,"writable":false},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"SysvarRent111111111111111111111111111111111","signer":false,"writable":false}],"data":"AQ=="},{"programId":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","accountMetas":[{"address":"jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai","signer":false,"writable":true},{"address":"5KrN1vytDRuRxRDZc5EoTKGhXFqZiR7evy1zTR8irhwZ","signer":false,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"EhAJTsW745jiWjViB7Q4xXcgKf6tMF7RcMX9cbTuXVBk","signer":false,"writable":true},{"address":"HFSNnAxfhDt4DnmY9yVs2HNFnEMaDJ7RxMVNB9Y5Hgjr","signer":false,"writable":true},{"address":"FUH3FvpU6M7zNpaJ7fSyVD8UiaTWGxmbciwHxJACcEbA","signer":false,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"6vBhv2L33KVJvAQeiaW3JEZLrJU7TtGaqcwPdrhytYWG","signer":false,"writable":false},{"address":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","signer":false,"writable":false}],"data":"AAUAAAA="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true}],"data":"CQ=="}]}],"signingData":{"feePayer":"59CH4KuZWQpyGbkEtUhzM9KbYEsssJ7NgM89db5GehLj","walletProgramId":"6m1icfABEiCG3vm4w9YL9QBTc7AN4ApU9VY38XmQH9VC","multisigOpAccountAddress":"11111111111111111111111111111111","walletAddress":"HNNg5RDk1o35APqnrDUJniT6ngyZbisEcsAPTjzcPuPK"},"dappInfo":{"address":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","name":"Serum dApp","logo":"https://raw.githubusercontent.com/project-serum/awesome-serum/master/logo-serum.png","url":"https://serum-demo2.strikeprotocols.com"}}}
    """.trim()

    val acceptVaultInvitationJson = """{"id": "422e3504-4eea-493a-a0dd-64a001115540", "walletType": "Solana", "submitDate": "2022-06-21T14:20:38.145+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 9223372036854775807, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "programVersion": null, "details": {"type": "AcceptVaultInvitation", "vaultGuid": "58e03f93-b9bc-4f22-b485-8e7a0abd8440", "vaultName": "Test Organization 1"}, "vaultName": "Test Organization 1"}"""

    val passwordResetJson = """{"id": "422e3504-4eea-493a-a0dd-64a001115540", "walletType": "Solana", "submitDate": "2022-06-21T14:20:38.145+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 9223372036854775807, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "programVersion": null, "details": {"type": "PasswordReset"}, "vaultName": null}"""

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

    fun get17StandardApprovals(): List<WalletApproval> {
        val withdrawalRequest =
            getWalletApprovalRequest(
                getSolWithdrawalRequest(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val conversionRequest =
            getWalletApprovalRequest(
                getConversionRequest(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val wrapConversionRequest =
            getWalletApprovalRequest(
                getWrapConversionRequest(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val balanceAccountNameUpdateRequest =
            getWalletApprovalRequest(
                getBalanceAccountNameUpdate(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val balanceAccountPolicyUpdateRequest =
            getWalletApprovalRequest(
                getBalanceAccountPolicyUpdate(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val balanceAccountSettingsUpdateRequest =
            getWalletApprovalRequest(
                getBalanceAccountSettingsUpdate(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val balanceAccountAddressWhitelistUpdateRequest =
            getWalletApprovalRequest(
                getBalanceAccountAddressWhitelistUpdate(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val dAppTransactionRequest =
            getWalletApprovalRequest(
                getDAppTransactionRequest(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val signersUpdateRequest =
            getWalletApprovalRequest(
                getSignersUpdateRequest(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val balanceAccountCreationRequest =
            getWalletApprovalRequest(
                getBalanceAccountCreationRequest(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val addressBookUpdateRequest =
            getWalletApprovalRequest(
                getAddAddressBookEntry(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val walletConfigPolicyUpdateRequest =
            getWalletApprovalRequest(
                getWalletConfigPolicyUpdate(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val dappBookUpdateRequest =
            getWalletApprovalRequest(
                getAddDAppBookEntry(
                    nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")
                )
            )

        val loginApproval =
            getWalletApprovalRequest(
                getLoginApproval(
                    jwtToken = EXAMPLE_JWT_TOKEN
                )
            )

        val acceptVaultRequest: JsonElement =
            JsonParser.parseString(acceptVaultInvitationJson.trim())
        val acceptVaultInvitationApproval =
            WalletApprovalDeserializer().parseData(acceptVaultRequest)

        val passwordResetRequest: JsonElement =
            JsonParser.parseString(passwordResetJson.trim())
        val passwordResetApproval =
            WalletApprovalDeserializer().parseData(passwordResetRequest)

        val unknownRequest =
            getWalletApprovalRequest(
                SolanaApprovalRequestType.UnknownApprovalType
            )

        return listOf(
            acceptVaultInvitationApproval,
            passwordResetApproval,
            withdrawalRequest,
            conversionRequest,
            loginApproval,
            balanceAccountSettingsUpdateRequest,
            addressBookUpdateRequest,
            signersUpdateRequest,
            dAppTransactionRequest,
            balanceAccountCreationRequest,
            balanceAccountAddressWhitelistUpdateRequest,
            balanceAccountNameUpdateRequest,
            balanceAccountPolicyUpdateRequest,
            walletConfigPolicyUpdateRequest,
            unknownRequest,
            wrapConversionRequest,
            dappBookUpdateRequest,
        )
    }

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

    fun getWalletApprovalRequest(solanaApprovalRequestType: SolanaApprovalRequestType) : WalletApproval {
        return WalletApproval(
            id = "1",
            walletType = WalletSigner.WALLET_TYPE_SOLANA,
            submitterName = "",
            submitterEmail = "",
            submitDate = Date().toString(),
            approvalTimeoutInSeconds = 1000,
            numberOfDispositionsRequired = 1,
            numberOfApprovalsReceived = 1,
            numberOfDeniesReceived = 1,
            vaultName = "Test Vault",
            details = SolanaApprovalRequestDetails.ApprovalRequestDetails(solanaApprovalRequestType)
        )
    }

    fun getSignersUpdateRequest(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
        return SolanaApprovalRequestType.SignersUpdate(
            type = ApprovalType.SIGNERS_UPDATE_TYPE.value,
            slotUpdateType = SlotUpdateType.Clear,
            signer = SlotSignerInfo(
                slotId = 2,
                value = SignerInfo(
                    publicKey = "8hyAhcNRc1WS1eZxNy4keGC9mbGoyXZkx75qxmwM3hUc",
                    name = "User 3",
                    email = "user3@org1"
                )
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "8pPAcjFSByreFRnRm5YyAdBP2LfiNnWBtBzHtRDcJpUA",
                multisigOpAccountAddress = "SLnWXM1QTraLWFhCm7JxDZk11PBE5Gu524ASzAC6YjW",
                walletAddress = "ECzeaMTMBXYXXfVM53n5iPepf8749QUqEzjW8jxefGhh",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getSignersUpdateWalletRequest(nonceAccountAddresses: List<String>) : WalletApproval {
        return getWalletApprovalRequest(
            SolanaApprovalRequestType.SignersUpdate(
                type = ApprovalType.SIGNERS_UPDATE_TYPE.value,
                slotUpdateType = SlotUpdateType.SetIfEmpty,
                signer = SlotSignerInfo(
                    slotId = 1,
                    value = SignerInfo(
                        publicKey = "6E5S1pMfe7DfBwYp2KmmYvTup2hduP385dhhoexX8i9",
                        name = "User 2",
                        email = "user2@org1"
                    )
                ),
                signingData = SolanaSigningData(
                    feePayer = "8UT5JS7vVcGLBHQe19Q5EK6aFA2CYnFG8a5C4dkrTL2B",
                    walletProgramId = "JAbzU4jwUMn92xhZcAX4M6JANEigzVMKKJqy6pA1cNBT",
                    multisigOpAccountAddress = "Hx9JnkPHioA9eu92y7jho1TxNaBCHYbw8zaSxvkGXSdD",
                    walletAddress = "FWhBukWcdXaMqZhJMvAAEH6PH81nV6JSpBEmwdvWgUjW",
                    nonceAccountAddresses = nonceAccountAddresses,
                    initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                    strikeFeeAmount = 0,
                    feeAccountGuidHash = emptyHash,
                    walletGuidHash = emptyHash,
                    nonceAccountAddressesSlot = 2272
                )
            )
        )
    }

    fun getBalanceAccountCreationRequest(nonceAccountAddresses: List<String>) : SolanaApprovalRequestType.BalanceAccountCreation {
        return SolanaApprovalRequestType.BalanceAccountCreation(
            type = ApprovalType.BALANCE_ACCOUNT_CREATION_TYPE.value,
            accountSlot = 0,
            accountInfo = AccountInfo(
                name = "Account 1",
                identifier = "1ac4a7fc-d2f8-4c32-8707-7496ee958933",
                accountType = AccountType.BalanceAccount,
                address = null
            ),
            approvalPolicy = ApprovalPolicy(
                approvalsRequired = 1,
                approvalTimeout = 3600000,
                approvers = listOf(
                    SlotSignerInfo(
                        slotId = 0,
                        value = SignerInfo(
                            name = "User 1",
                            email = "authorized1@org1",
                            publicKey = "4q8ApWsB3rSW2HPFwc1aWmGgcBMfj7tSKBbb5sBGAB6h"
                        )
                    )
                )
            ),
            whitelistEnabled = BooleanSetting.Off,
            dappsEnabled = BooleanSetting.Off,
            addressBookSlot = 0,
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "8pPAcjFSByreFRnRm5YyAdBP2LfiNnWBtBzHtRDcJpUA",
                multisigOpAccountAddress = "HypFjU4nfRYwdnNQTyJw8TFxYekptWTQNrTcW7ofMZxu",
                walletAddress = "ECzeaMTMBXYXXfVM53n5iPepf8749QUqEzjW8jxefGhh",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getSolWithdrawalRequest(nonceAccountAddresses: List<String>) : SolanaApprovalRequestType.WithdrawalRequest {
        return SolanaApprovalRequestType.WithdrawalRequest(
            type = ApprovalType.WITHDRAWAL_TYPE.value,
            account = AccountInfo(
                name = "Account 1",
                identifier = "9f3093c7-5b77-4ce4-b718-d47030bfdf3f",
                accountType = AccountType.BalanceAccount,
                address = "2AhhYePazh7dekyu9Ug8Vfp7weVCs3UgefLKq2pwpMzo"
            ),
            symbolAndAmountInfo = SymbolAndAmountInfo(
                symbolInfo = SymbolInfo(
                    symbol = "SOL",
                    symbolDescription = "Solana",
                    tokenMintAddress = "11111111111111111111111111111111"
                ),
                amount = "0.200000000",
                usdEquivalent = "17.75"
            ),
            destination = DestinationAddress(
                name = "My External Sol address",
                subName = null,
                address = "AzntcKp4TjdgRakBBorz6Tp2kC4PQg4gkDgrr9khCETU",
                tag = null
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "AG23jVQ5EftDonUwCMhgXtr4jQAPdhAtPFw4y84CMeuj",
                multisigOpAccountAddress = "9NDFtaczqouZ9SGTfd489EfN3KvMQgrAjpuu4QEr9Kys",
                walletAddress = "CV3Xhgcs48U5o6CnabtjngKbR7H5dxpzJxpfZqBeEMfV",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getSplWithdrawalRequest(nonceAccountAddresses: List<String>) : SolanaApprovalRequestType.WithdrawalRequest {
        return SolanaApprovalRequestType.WithdrawalRequest(
            type = ApprovalType.WITHDRAWAL_TYPE.value,
            account = AccountInfo(
                name = "Account 1",
                identifier = "5fb4556a-6de5-4a80-ac0e-6def9826384f",
                accountType = AccountType.BalanceAccount,
                address = "HT8kqgLxH5BsyA6Ah3oaAKG8SNAgzgRNH4uMfcAnUXTZ"
            ),
            symbolAndAmountInfo = SymbolAndAmountInfo(
                symbolInfo = SymbolInfo(
                    symbol = "soTEST",
                    symbolDescription = "Test SPL token",
                    tokenMintAddress = "AZ6C941cFEv7EWUsPeeYYEK278Lw5wK4AVR6Mngdt9fr"
                ),
                amount = "0.000500",
                usdEquivalent = null
            ),
            destination = DestinationAddress(
                name = "Org1 Sol Wallet",
                subName = null,
                address = "7DhLZAT5buGyXpjpfRNKaHc1imjJaDzCXXTdM59JHrpQ",
                tag = null
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "zeZ7E8F6UaNYy3ry3Mt6MGUSr679oTKV8tzXVe5B4bP",
                multisigOpAccountAddress = "6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX",
                walletAddress = "7fvoSJ6iNAyTFvBDuAWuciXWYiyUBtJfCUswZF3YGbUN",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getConversionRequest(nonceAccountAddresses: List<String>): SolanaApprovalRequestType.ConversionRequest {
        return SolanaApprovalRequestType.ConversionRequest(
            type = ApprovalType.CONVERSION_REQUEST_TYPE.value,
            account = AccountInfo(
                name = "Account 1",
                identifier = "9826889c-df77-4c5b-b4ad-9bde935e6c52",
                accountType = AccountType.BalanceAccount,
                address = "F8MQFSzgGtddamGjNNoFuUfrZNZkV84icnXwyMVo7Aa3"
            ),
            symbolAndAmountInfo = SymbolAndAmountInfo(
                symbolInfo = SymbolInfo(
                    symbol = "USDC",
                    symbolDescription = "USD Coin",
                    tokenMintAddress = "ALmJ9wWY2o1FiLcSDuvHN3xH5UHLkYsVbz2JWD37MuUY"
                ),
                amount = "500.000000",
                usdEquivalent = "500.00"
            ),
            destination = DestinationAddress(
                name = "USDC Redemption Address",
                subName = null,
                address = "Bt4cfS3fhtbCiB3uDXDRvft6SCVbHCH7Pz7kh66tzzKA",
                tag = null
            ),
            destinationSymbolInfo = SymbolInfo(
                symbol = "USD",
                symbolDescription = "US Dollar",
                tokenMintAddress = "11111111111111111111111111111111"
            ),
            signingData = SolanaSigningData(
                feePayer = "FBiyhqgyrv6iRejRgL9tDYxB2jtEB4RH9pnPK2CN5J4m",
                walletProgramId = "CH2nLW24j2Wd1geFGSKkJmbAz1KLhACR9RRD1wHgCH74",
                multisigOpAccountAddress = "11111111111111111111111111111111",
                walletAddress = "2sGiNkpwYod6c1Wcd6H1ycd85KwykMfb8ZCt7t3XEp4h",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getWrapConversionRequest(nonceAccountAddresses: List<String>): SolanaApprovalRequestType.WrapConversionRequest {
        return SolanaApprovalRequestType.WrapConversionRequest(
            type = ApprovalType.WRAP_CONVERSION_REQUEST_TYPE.value,
            account = AccountInfo(
                name = "Account 1",
                identifier = "82666cf4-3f31-4504-a1a2-5df9b35ba5b3",
                accountType = AccountType.BalanceAccount,
                address = "BSHKeDQL8NKBSmbX2M4svSqGL57qFhe7qvw72hpvgnZY"
            ),
            symbolAndAmountInfo = SymbolAndAmountInfo(
                symbolInfo = SymbolInfo(
                    symbol = "SOL",
                    symbolDescription = "Solana",
                    tokenMintAddress = "11111111111111111111111111111111"
                ),
                amount = "0.500000000",
                usdEquivalent = "44.39"
            ),
            destinationSymbolInfo = SymbolInfo(
                symbol = "wSOL",
                symbolDescription = "Wrapped SOL",
                tokenMintAddress = "11111111111111111111111111111111"
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "DaGSQwGd1GZnscN2Mu5d1CPYqYXAQMV29Q4Zk9yDhZLp",
                multisigOpAccountAddress = "11111111111111111111111111111111",
                walletAddress = "Ebse7xEiKuhe3bWY6dXiWB8QS4QDhr8fRBgH4tUKR2Ys",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getUnwrapConversionRequest(nonceAccountAddresses: List<String>) : SolanaApprovalRequestType {
        return SolanaApprovalRequestType.WrapConversionRequest(
            type = ApprovalType.WRAP_CONVERSION_REQUEST_TYPE.value,
            account = AccountInfo(
                name = "Account 1",
                identifier = "82666cf4-3f31-4504-a1a2-5df9b35ba5b3",
                accountType = AccountType.BalanceAccount,
                address = "BSHKeDQL8NKBSmbX2M4svSqGL57qFhe7qvw72hpvgnZY"
            ),
            symbolAndAmountInfo = SymbolAndAmountInfo(
                symbolInfo = SymbolInfo(
                    symbol = "wSOL",
                    symbolDescription = "Wrapped SOL",
                    tokenMintAddress = "11111111111111111111111111111111"
                ),
                amount = "0.300000000",
                usdEquivalent = "26.63"
            ),
            destinationSymbolInfo = SymbolInfo(
                symbol = "SOL",
                symbolDescription = "Solana",
                tokenMintAddress = "11111111111111111111111111111111"
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "DaGSQwGd1GZnscN2Mu5d1CPYqYXAQMV29Q4Zk9yDhZLp",
                multisigOpAccountAddress = "11111111111111111111111111111111",
                walletAddress = "Ebse7xEiKuhe3bWY6dXiWB8QS4QDhr8fRBgH4tUKR2Ys",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getAddDAppBookEntry(nonceAccountAddresses: List<String>) : SolanaApprovalRequestType {
        return SolanaApprovalRequestType.DAppBookUpdate(
            type = ApprovalType.DAPP_BOOK_UPDATE_TYPE.value,
            entriesToAdd = listOf(
                SlotDAppInfo(
                    slotId = 0,
                    value = SolanaDApp(
                        address = "GNGhSWWVNhXAy4fQgfAoQejJpGAxVaE4bdJjdb6iXRjK",
                        name = "DApp",
                        logo = "icon"
                    )
                )
            ),
            entriesToRemove = emptyList(),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "3Nh3QsaXKbTbLM1BLsD4dhT4zeHTPaVbZX3eN3Yg1G2w",
                multisigOpAccountAddress = "Hn2CJuYyyB2H3wwmdHPy1Aun2Jkye3MCSVajzUvw55A9",
                walletAddress = "Re4dLGch8a1G98PeRtpHa5ApS6Gnik444CqB5BQ8rY1",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getWalletConfigPolicyUpdate(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
        return SolanaApprovalRequestType.WalletConfigPolicyUpdate(
            type = ApprovalType.WALLET_CONFIG_POLICY_UPDATE_TYPE.value,
            approvalPolicy = ApprovalPolicy(
                approvalsRequired = 3,
                approvalTimeout = 18000000,
                approvers = listOf(
                    SlotSignerInfo(
                        slotId = 0,
                        value = SignerInfo(
                            publicKey = "5zpDzYujD8xnZ5B9m93qHCGMSeLDb7eAKCo4kWha7knV",
                            name = "User 1",
                            email = "authorized1@org1"
                        )
                    ),
                    SlotSignerInfo(
                        slotId = 1,
                        value = SignerInfo(
                            publicKey = "3tSshpPL1WyNR7qDfxPffinndQmgfvTGoZc3PgL65Z9o",
                            name = "User 2",
                            email = "user2@org1"
                        )
                    ),
                    SlotSignerInfo(
                        slotId = 2,
                        value = SignerInfo(
                            publicKey = "5rt9dFozMbpPf2mMgiMuu2f4CxQa3pWS1Exo9wHPbwJK",
                            name = "User 3",
                            email = "user3@org1"
                        )
                    )
                )
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "5kx1BNgMpBbEfrrzfqZrmW8xfqpdbC8b34TkwGbXg42r",
                multisigOpAccountAddress = "F6iUTdJDE4vnTgBanCtBgtoNHag57Uaut82xATGVVps3",
                walletAddress = "hBVqSAZ3Z7dSrWXoQdKJGttgHVrWa3qzdeHpiX6WKk3",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getBalanceAccountPolicyUpdate(nonceAccountAddresses: List<String>) : SolanaApprovalRequestType {
        return SolanaApprovalRequestType.BalanceAccountPolicyUpdate(
            type = ApprovalType.BALANCE_ACCOUNT_POLICY_UPDATE_TYPE.value,
            accountInfo = AccountInfo(
                name = "Account 1",
                identifier = "1ac4a7fc-d2f8-4c32-8707-7496ee958933",
                accountType = AccountType.BalanceAccount,
                address = "5743aqK2n9xnTSmFcbzTmfpdtcNeWdJsCxTxrCcNXUFH"
            ),
            approvalPolicy = ApprovalPolicy(
                approvalsRequired = 2,
                approvalTimeout = 3600000,
                approvers = listOf(
                    SlotSignerInfo(
                        slotId = 0,
                        value = SignerInfo(
                            publicKey = "4q8ApWsB3rSW2HPFwc1aWmGgcBMfj7tSKBbb5sBGAB6h",
                            name = "User 1",
                            email = "authorized1@org1"
                        )
                    ),
                    SlotSignerInfo(
                        slotId = 1,
                        value = SignerInfo(
                            publicKey = "CDrdR8xX8t83eXxB2ESuHp9AxkiJkUuKnD98zyDfMtrG",
                            name = "User 2",
                            email = "user2@org1"
                        )
                    ),
                )
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "8pPAcjFSByreFRnRm5YyAdBP2LfiNnWBtBzHtRDcJpUA",
                multisigOpAccountAddress = "DbdTEwihgEYJYAgXBKEqQGknGyHsRnxE5coeZaVS4T9y",
                walletAddress = "ECzeaMTMBXYXXfVM53n5iPepf8749QUqEzjW8jxefGhh",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getBalanceAccountNameUpdate(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
        return SolanaApprovalRequestType.BalanceAccountNameUpdate(
            type = ApprovalType.BALANCE_ACCOUNT_NAME_UPDATE_TYPE.value,
            accountInfo = AccountInfo(
                name = "Account 1",
                identifier = "b645a5d9-227f-4a9f-9331-52af64bf1989",
                accountType = AccountType.BalanceAccount,
                address = "DcvZ2k6ygvvu2Z5ihrSxRZL7bHJ38gPRgpCie8GzztTP"
            ),
            newAccountName = "New Name",
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "7kNPVcK2cpyaZsLsqmhZbjcbt433vYUckH1PM5gZeJ1L",
                multisigOpAccountAddress = "7DY87mHHiSSyxFBbhCYbTpQE5M4Jk9Z9hymJ7UzL3sPm",
                walletAddress = "4XaqL4MtTUDrncTGBqvTC9ketf8WVqrUocDkYhKAnDju",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getDAppTransactionRequest(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
        return SolanaApprovalRequestType.DAppTransactionRequest(
            type = ApprovalType.DAPP_TRANSACTION_REQUEST_TYPE.value,
            account = AccountInfo(
                name = "Account 1",
                identifier = "6689604c-452c-4c35-9ab9-f7add6c539a5",
                accountType = AccountType.BalanceAccount,
                address = "6zmSor8Y9CXjFExGPZsJA1wk1utAJeRw6NH9a1w2zymX"
            ),
            dappInfo = SolanaDApp(
                address = "5zR1qXBiPwDX4wnhusoFbzExL2URtD3cjjpJqyMXDZiz",
                name = "DApp Name",
                logo = "dapp-icon"
            ),
            balanceChanges = emptyList(),
            instructions = listOf(
                SolanaInstructionBatch(
                    from = 0,
                    instructions = listOf(
                        SolanaInstruction(
                            programId = "ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL",
                            accountMetas = listOf(
                                SolanaAccountMeta(
                                    address = "6zmSor8Y9CXjFExGPZsJA1wk1utAJeRw6NH9a1w2zymX",
                                    signer = true,
                                    writable = true
                                ),
                                SolanaAccountMeta(
                                    address = "74FrjocsXjzBzL92efmDBLKczVw7UFzQznCe76grKdeh",
                                    signer = false,
                                    writable = true
                                ),
                                SolanaAccountMeta(
                                    address = "6zmSor8Y9CXjFExGPZsJA1wk1utAJeRw6NH9a1w2zymX",
                                    signer = true,
                                    writable = true
                                ),
                                SolanaAccountMeta(
                                    address = "EQQKpcUbk6M5ikbK6SjezzMWUBbJB9qWtpEvw4BwuexT",
                                    signer = false,
                                    writable = false
                                ),
                                SolanaAccountMeta(
                                    address = "11111111111111111111111111111111",
                                    signer = false,
                                    writable = false
                                ),
                                SolanaAccountMeta(
                                    address = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
                                    signer = false,
                                    writable = false
                                ),
                                SolanaAccountMeta(
                                    address = "SysvarRent111111111111111111111111111111111",
                                    signer = false,
                                    writable = false
                                ),
                            ),
                            data = "AQIDrA=="
                        )
                    )
                )
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "DafeuJ6v1Mv1cvEVU6EnH2uomCP8nvj9EbKHgXGxGChy",
                multisigOpAccountAddress = "11111111111111111111111111111111",
                walletAddress = "6Vie7d1hd84JnQVAyZW5V5EkTfVTF1Gap2a5mtwMqobF",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getLoginApproval( jwtToken : String) : SolanaApprovalRequestType {
        return SolanaApprovalRequestType.LoginApprovalRequest(
            type = ApprovalType.LOGIN_TYPE.value,
            jwtToken = jwtToken,
            email = "sharris@blue.rock",
            name = "Sammy Miami Harris"
        )
    }

    fun getWalletInitiationRequest( requestType: SolanaApprovalRequestType, initiation: MultiSigOpInitiation) : WalletApproval {
        return WalletApproval(
            id= "1",
            walletType = WalletSigner.WALLET_TYPE_SOLANA,
            submitterName= "",
            submitterEmail= "",
            submitDate= Date().toString(),
            approvalTimeoutInSeconds= 1000,
            numberOfDispositionsRequired= 1,
            numberOfApprovalsReceived= 1,
            numberOfDeniesReceived= 1,
            vaultName = "Test Vault",
            details= SolanaApprovalRequestDetails.MultiSignOpInitiationDetails(initiation, requestType= requestType)
        )
    }

    fun getOpAccountCreationInfo() : MultiSigAccountCreationInfo {
        return MultiSigAccountCreationInfo(
            accountSize= 848,
            minBalanceForRentExemption= 6792960
        )
    }

    fun getBalanceAccountAddressWhitelistUpdate(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
        return SolanaApprovalRequestType.BalanceAccountAddressWhitelistUpdate(
            type = ApprovalType.BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE_TYPE.value,
            accountInfo = AccountInfo(
                name = "Account 1",
                identifier = "4d2eecc1-cbe1-4c36-a4ae-1f777a739eb3",
                accountType = AccountType.BalanceAccount,
                address = "HvZFxso1tq9FLD1Gh2ACGNsR5pQBgjVC8uo21Cc9ytzg"
            ),
            destinations = listOf(
                SlotDestinationInfo(
                    slotId = 1,
                    value = DestinationAddress(
                        name = "My External Sol address 1",
                        subName = null,
                        address = "AXX2TNxGhW2M3GpQPuWVuqmyAvQFVpyZD2dvR9gRiMRQ",
                        tag = null
                    )
                ),
                SlotDestinationInfo(
                    slotId = 2,
                    value = DestinationAddress(
                        name = "My External Sol address 2",
                        subName = null,
                        address = "2db8ovVF6iXTaPQAhJe3frG46iNLF5Ny7ZipGKDomiTh",
                        tag = null
                    )
                )
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "9LM4sYmMHk1VDcFpA8ezPeL8GtEVR5T51Qxcksrf4VX2",
                multisigOpAccountAddress = "71S5qEAD3DMn7QY9fdb2uR1TV7kiAfcAqNHfQfyFUSME",
                walletAddress = "AoEAvW2TvZYmy2WbmqN4nXdJT8o21RbJP6xNK2yR4of",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getBalanceAccountSettingsUpdate(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
        return SolanaApprovalRequestType.BalanceAccountSettingsUpdate(
            type = ApprovalType.BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE.value,
            account = AccountInfo(
                name = "Account 1",
                identifier = "b645a5d9-227f-4a9f-9331-52af64bf1989",
                accountType = AccountType.BalanceAccount,
                address = "DcvZ2k6ygvvu2Z5ihrSxRZL7bHJ38gPRgpCie8GzztTP"
            ),
            whitelistEnabled = BooleanSetting.On,
            dappsEnabled = null,
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "7kNPVcK2cpyaZsLsqmhZbjcbt433vYUckH1PM5gZeJ1L",
                multisigOpAccountAddress = "GM2yp6wzBijkziNSDAXoDsuJ2e76VTLgqTfikh5r9BfD",
                walletAddress = "4XaqL4MtTUDrncTGBqvTC9ketf8WVqrUocDkYhKAnDju",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }


    fun getAddAddressBookEntry(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
        return SolanaApprovalRequestType.AddressBookUpdate(
            type = ApprovalType.ADDRESS_BOOK_TYPE.value,
            entriesToRemove = emptyList(),
            entriesToAdd = listOf(
                SlotDestinationInfo(
                    slotId = 1,
                    value = DestinationAddress(
                        name = "My External Sol address 1",
                        subName = null,
                        address = "6RsFJRJb2RxZG7kKFbnnKdei4bUmC51wBbEpZtm9AuzV",
                        tag = null
                    )
                )
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "A2iHua5UQd6RWb6C3ZctQcfVZDoeG5pUvBtRQfWSxSqb",
                multisigOpAccountAddress = "2Qr2bq8KpyAho1rSnE7TUwXgHW3UpM7KwYEijF11JF2d",
                walletAddress = "FpyUo7gVxzB3mPVSkcHNdzdo1T6tNHvkzUvFajw2PwkG",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }

    fun getRemoveDAppBookEntry(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
        return SolanaApprovalRequestType.DAppBookUpdate(
            type = ApprovalType.DAPP_BOOK_UPDATE_TYPE.value,
            entriesToAdd = emptyList(),
            entriesToRemove = listOf(
                SlotDAppInfo(
                    slotId = 0,
                    value = SolanaDApp(
                        address = "GNGhSWWVNhXAy4fQgfAoQejJpGAxVaE4bdJjdb6iXRjK",
                        name = "DApp",
                        logo = "icon"
                    )
                )
            ),
            signingData = SolanaSigningData(
                feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
                walletProgramId = "3Nh3QsaXKbTbLM1BLsD4dhT4zeHTPaVbZX3eN3Yg1G2w",
                multisigOpAccountAddress = "9CfoFci2agjCJ7bWqfgKEFSAc5zB6UR63MrK61nRaJzm",
                walletAddress = "Re4dLGch8a1G98PeRtpHa5ApS6Gnik444CqB5BQ8rY1",
                nonceAccountAddresses = nonceAccountAddresses,
                initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                strikeFeeAmount = 0,
                feeAccountGuidHash = emptyHash,
                walletGuidHash = emptyHash,
                nonceAccountAddressesSlot = 2272
            )
        )
    }
}
