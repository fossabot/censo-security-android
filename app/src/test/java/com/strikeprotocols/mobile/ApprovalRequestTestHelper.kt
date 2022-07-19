package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.models.approval.*
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.*
import java.util.*


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
    return SignersUpdate(
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
        )
    )
}

fun getSignersUpdateWalletRequest(nonceAccountAddresses: List<String>) : WalletApproval {
    return getWalletApprovalRequest(
        SignersUpdate(
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
            )
        )
    )
}

fun getBalanceAccountCreationRequest(nonceAccountAddresses: List<String>) : BalanceAccountCreation {
    return BalanceAccountCreation(
        type = ApprovalType.BALANCE_ACCOUNT_CREATION_TYPE.value,
        accountSlot = 0,
        accountInfo = AccountInfo(
            name = "Account 1",
            identifier = "c6055be1-a895-45a6-b0f3-fce261760b89",
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
                        publicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"
                    )
                )
            )
        ),
        whitelistEnabled = BooleanSetting.Off,
        dappsEnabled = BooleanSetting.Off,
        addressBookSlot = 1,
        signingData = SolanaSigningData(
            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
            multisigOpAccountAddress = "2DBQ368KgyPkmqd6fKsQpmpMhBTDTuW6wWESbxDs5otz",
            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
            nonceAccountAddresses = nonceAccountAddresses,
            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
            strikeFeeAmount = 0,
            feeAccountGuidHash = emptyHash,
            walletGuidHash = emptyHash,
        )
    )
}

fun getSolWithdrawalRequest(nonceAccountAddresses: List<String>) : WithdrawalRequest {
    return WithdrawalRequest(
        type = ApprovalType.WITHDRAWAL_TYPE.value,
        account = AccountInfo(
            name = "Account 1",
            identifier = "c6055be1-a895-45a6-b0f3-fce261760b89",
            accountType = AccountType.BalanceAccount,
            address = "oRYGxVHXEqpLaH9QWxX8yRMzLsmPRXyfNmop2QrPQKY"
        ),
        symbolAndAmountInfo = SymbolAndAmountInfo(
            symbolInfo = SymbolInfo(
                symbol = "SOL",
                symbolDescription = "Solana",
                tokenMintAddress = "11111111111111111111111111111111"
            ),
            amount = "0.500000000",
            usdEquivalent = "17.75"
        ),
        destination = DestinationAddress(
            name = "My External Sol address",
            subName = null,
            address = "2DQz5vWgs1PKxPDd9YaYKoemgFriRJqoFRniAQ7Wtuva",
            tag = null
        ),
        signingData = SolanaSigningData(
            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
            multisigOpAccountAddress = "9NDFtaczqouZ9SGTfd489EfN3KvMQgrAjpuu4QEr9Kys",
            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
            nonceAccountAddresses = nonceAccountAddresses,
            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
            strikeFeeAmount = 0,
            feeAccountGuidHash = emptyHash,
            walletGuidHash = emptyHash,
        )
    )
}

fun getSplWithdrawalRequest(nonceAccountAddresses: List<String>) : WithdrawalRequest {
    return WithdrawalRequest(
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
        )
    )
}

fun getConversionRequest(nonceAccountAddresses: List<String>): ConversionRequest {
    return ConversionRequest(
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
        )
    )
}

fun getWrapConversionRequest(nonceAccountAddresses: List<String>): WrapConversionRequest {
    return WrapConversionRequest(
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
        )
    )
}

fun getUnwrapConversionRequest(nonceAccountAddresses: List<String>) : SolanaApprovalRequestType {
    return  WrapConversionRequest(
        type = ApprovalType.WRAP_CONVERSION_REQUEST_TYPE.value,
        account= AccountInfo(
            name= "Account 1",
            identifier= "c2a6711d-8430-429f-816a-876eb62dd19e",
            accountType= AccountType.BalanceAccount,
            address= "7dMB51drmhKy9qQ8GjFPsaRDnadGCvn4iLWedqajbmUg"
        ),
        symbolAndAmountInfo= SymbolAndAmountInfo(
            symbolInfo= SymbolInfo(
                symbol= "wSOL",
                symbolDescription= "Wrapped SOL",
                tokenMintAddress= "11111111111111111111111111111111"
            ),
            amount= "0.300000000",
            usdEquivalent= "26.63"
        ),
        destinationSymbolInfo= SymbolInfo(
            symbol= "SOL",
            symbolDescription= "Solana",
            tokenMintAddress= "11111111111111111111111111111111"
        ),
        signingData= SolanaSigningData(
            feePayer= "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
            walletProgramId= "8S1mgAomg5mcJ6rC38xHMMJyFKTHxQc2dHgNrmQKzAz",
            multisigOpAccountAddress= "11111111111111111111111111111111",
            walletAddress= "HZmqaRJWQxB6B4DXCBmY5W8xjL2Wn5Q6rGHtajxUDbra",
            nonceAccountAddresses= nonceAccountAddresses,
            initiator = "3S3WAHv5h7gyEVTPQRuz6sf8poKM439zr14pHF43MtLK",
            strikeFeeAmount = 2039280,
            feeAccountGuidHash = "Oe1VO8ObkbQ2jHnzOD6tIGQNkX/sExJpdGOksGK47VU=",
            walletGuidHash = "/Fz5hXppVfCrsvkgU8zXy5e3IO99xOmbQJuF7DUkHfw=",
        )
    )
}

fun getAddAddressBookEntry(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
    return AddressBookUpdate(
        type = ApprovalType.ADDRESS_BOOK_TYPE.value,
        entriesToRemove = emptyList(),
        entriesToAdd = listOf(
            SlotDestinationInfo(
                slotId = 0,
                value = DestinationAddress(
                    name = "My External Sol address",
                    subName = null,
                    address = "2DQz5vWgs1PKxPDd9YaYKoemgFriRJqoFRniAQ7Wtuva",
                    tag = null
                )
            )
        ),
        signingData = SolanaSigningData(
            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
            multisigOpAccountAddress = "Dpt714om7J3B3f1ygptgoEnFvHo3aiXjeLPP7TqjHJhq",
            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
            nonceAccountAddresses = nonceAccountAddresses,
            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
            strikeFeeAmount = 0,
            feeAccountGuidHash = emptyHash,
            walletGuidHash = emptyHash,
        )
    )
}


fun getAddDAppBookEntry(nonceAccountAddresses: List<String>) : SolanaApprovalRequestType {
    return  DAppBookUpdate(
        type = ApprovalType.DAPP_BOOK_UPDATE_TYPE.value,
        entriesToAdd= listOf(
            SlotDAppInfo(
                slotId= 0,
                value= SolanaDApp(address= "GNGhSWWVNhXAy4fQgfAoQejJpGAxVaE4bdJjdb6iXRjK", name= "DApp", logo= "icon")
            )
        ),
        entriesToRemove= emptyList(),
        signingData= SolanaSigningData(
            feePayer= "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
            walletProgramId= "3Nh3QsaXKbTbLM1BLsD4dhT4zeHTPaVbZX3eN3Yg1G2w",
            multisigOpAccountAddress= "Hn2CJuYyyB2H3wwmdHPy1Aun2Jkye3MCSVajzUvw55A9",
            walletAddress= "Re4dLGch8a1G98PeRtpHa5ApS6Gnik444CqB5BQ8rY1",
            nonceAccountAddresses= nonceAccountAddresses,
            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
            strikeFeeAmount = 0,
            feeAccountGuidHash = emptyHash,
            walletGuidHash = emptyHash,
        )
    )
}

fun getWalletConfigPolicyUpdate(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
    return WalletConfigPolicyUpdate(
        type = ApprovalType.WALLET_CONFIG_POLICY_UPDATE_TYPE.value,
        approvalPolicy = ApprovalPolicy(
            approvalsRequired = 2,
            approvalTimeout = 18000000,
            approvers = listOf(
                SlotSignerInfo(
                    slotId = 0,
                    value = SignerInfo(
                        publicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
                        name = "User 1",
                        email = "authorized1@org1"
                    )
                ),
                SlotSignerInfo(
                    slotId = 1,
                    value = SignerInfo(
                        publicKey = "7AH35qStXtrUgRkmqDmhjufNHjF74R1A9cCKT3C3HaAR",
                        name = "User 2",
                        email = "user2@org1"
                    )
                ),
            )
        ),
        signingData = SolanaSigningData(
            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
            multisigOpAccountAddress = "F6iUTdJDE4vnTgBanCtBgtoNHag57Uaut82xATGVVps3",
            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
            nonceAccountAddresses = nonceAccountAddresses,
            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
            strikeFeeAmount = 0,
            feeAccountGuidHash = emptyHash,
            walletGuidHash = emptyHash,
        )
    )
}

fun getBalanceAccountPolicyUpdate(nonceAccountAddresses: List<String>) : SolanaApprovalRequestType {
    return BalanceAccountPolicyUpdate(
        type = ApprovalType.BALANCE_ACCOUNT_POLICY_UPDATE_TYPE.value,
        accountInfo= AccountInfo(
            name= "Account 1",
            identifier= "1ac4a7fc-d2f8-4c32-8707-7496ee958933",
            accountType= AccountType.BalanceAccount,
            address= "5743aqK2n9xnTSmFcbzTmfpdtcNeWdJsCxTxrCcNXUFH"
        ),
        approvalPolicy= ApprovalPolicy(
            approvalsRequired= 2,
            approvalTimeout= 3600000,
            approvers= listOf(
                SlotSignerInfo(
                    slotId= 0,
                    value= SignerInfo(publicKey= "4q8ApWsB3rSW2HPFwc1aWmGgcBMfj7tSKBbb5sBGAB6h", name= "User 1", email= "authorized1@org1")
                ),
                SlotSignerInfo(
                    slotId= 1,
                    value= SignerInfo(publicKey= "CDrdR8xX8t83eXxB2ESuHp9AxkiJkUuKnD98zyDfMtrG", name= "User 2", email= "user2@org1")
                ),
            )
        ),
        signingData= SolanaSigningData(
            feePayer= "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
            walletProgramId= "8pPAcjFSByreFRnRm5YyAdBP2LfiNnWBtBzHtRDcJpUA",
            multisigOpAccountAddress= "DbdTEwihgEYJYAgXBKEqQGknGyHsRnxE5coeZaVS4T9y",
            walletAddress= "ECzeaMTMBXYXXfVM53n5iPepf8749QUqEzjW8jxefGhh",
            nonceAccountAddresses= nonceAccountAddresses,
            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
            strikeFeeAmount = 0,
            feeAccountGuidHash = emptyHash,
            walletGuidHash = emptyHash,
        )
    )
}

fun getBalanceAccountNameUpdate(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
    return BalanceAccountNameUpdate(
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
        )
    )
}

fun getDAppTransactionRequest(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
    return DAppTransactionRequest(
        type = ApprovalType.DAPP_TRANSACTION_REQUEST_TYPE.value,
        account = AccountInfo(
            name = "Account 1",
            identifier = "3392e225-1f6a-442c-bb56-c35466013844",
            accountType = AccountType.BalanceAccount,
            address = "7etR3atK42QU7RwV6GSxqgYss4dpCcF9mvEaSqnXqbP4"
        ),
        dappInfo = SolanaDApp(
            address = "6txbcRGTsQ6tymXjgx11fVjqAGRtj37mhxCMePH4B5Gr",
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
                                address = "32bJsGEdPMxHNgUhXWRTjqMJHZmbgf1ukBFmG56tQuts",
                                signer = true,
                                writable = true
                            ),
                            SolanaAccountMeta(
                                address = "2DSmCoxPU1moZ2VYAtt1k6fLNYtgTtYQznUaXe8zz2kq",
                                signer = false,
                                writable = true
                            ),
                            SolanaAccountMeta(
                                address = "32bJsGEdPMxHNgUhXWRTjqMJHZmbgf1ukBFmG56tQuts",
                                signer = true,
                                writable = true
                            ),
                            SolanaAccountMeta(
                                address = "3CYKpq8zCRqzuFMzYqFDVRiiSTiZh7GP44sMrueaMK2C",
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
            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
            walletProgramId = "CPjitkp6iNimp1KkDeosrqcBCaxQdNjP6Wmn1xHGkVM7",
            multisigOpAccountAddress = "22btwB54QFYCLFVf53k64Jhj8KnpYCx2HwJmsqTq8Ly2",
            walletAddress = "3CwacbyswAFN914pyjBRGrZm5m7QdSYuNHdnJWeTrJ85",
            nonceAccountAddresses = nonceAccountAddresses,
            initiator = "9CZ2ZaGe961oLCWJUpr16ofZxBM4yK9tGfbdwj57JNJP",
            strikeFeeAmount = 0,
            feeAccountGuidHash = emptyHash,
            walletGuidHash = emptyHash,
        )
    )
}

fun getLoginApproval( jwtToken : String) : SolanaApprovalRequestType {
    return LoginApprovalRequest(
        type = ApprovalType.LOGIN_TYPE.value,
        jwtToken= jwtToken,
        email = "sharris@blue.rock",
        name = "Sam Ortiz"
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
        accountSize= 952,
        minBalanceForRentExemption= 7516800
    )
}

fun getBalanceAccountAddressWhitelistUpdate(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
    return BalanceAccountAddressWhitelistUpdate(
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
        )
    )
}

fun getBalanceAccountSettingsUpdate(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
    return BalanceAccountSettingsUpdate(
        type = ApprovalType.BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE.value,
        account = AccountInfo(
            name = "Account 1",
            identifier = "c6055be1-a895-45a6-b0f3-fce261760b89",
            accountType = AccountType.BalanceAccount,
            address = "oRYGxVHXEqpLaH9QWxX8yRMzLsmPRXyfNmop2QrPQKY"
        ),
        whitelistEnabled = BooleanSetting.On,
        dappsEnabled = null,
        signingData = SolanaSigningData(
            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
            multisigOpAccountAddress = "Dp4oaRWRtBxQdf5Lg2zti3TCjsUsxv4rUBgtf2HSQnVb",
            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
            nonceAccountAddresses = nonceAccountAddresses,
            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
            strikeFeeAmount = 0,
            feeAccountGuidHash = emptyHash,
            walletGuidHash = emptyHash,
        )
    )
}

fun getRemoveDAppBookEntry(nonceAccountAddresses: List<String>): SolanaApprovalRequestType {
    return DAppBookUpdate(
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
        )
    )
}



