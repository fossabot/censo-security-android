//package com.censocustody.android
//
//import com.censocustody.android.data.models.Chain
//import com.censocustody.android.data.models.approval.*
//import com.censocustody.android.data.models.approval.ApprovalRequestDetails.*
//import java.util.*
//
//
//fun getWalletApprovalRequest(approvalRequestType: ApprovalRequestDetails) : ApprovalRequest {
//    return ApprovalRequest(
//        id = "1",
//        submitterName = "",
//        submitterEmail = "",
//        submitDate = Date().toString(),
//        approvalTimeoutInSeconds = 1000,
//        numberOfDispositionsRequired = 1,
//        numberOfApprovalsReceived = 1,
//        numberOfDeniesReceived = 1,
//        vaultName = "Test Vault",
//        initiationOnly = false,
//        details = SolanaApprovalRequestDetails.ApprovalRequestDetails(approvalRequestType)
//    )
//}
//
//fun getSignersUpdateRequestForApproval(nonceAccountAddresses: List<String>): ApprovalRequestDetails {
//    return SignersUpdate(
//        type = ApprovalType.SIGNERS_UPDATE_TYPE.value,
//        slotUpdateType = SlotUpdateType.SetIfEmpty,
//        signer = SlotSignerInfo(
//            slotId = 2,
//            value = SignerInfo(
//                publicKey = "2XZHYvnhZGmgFB6TQyi3C1FYgeL9N2mK5c6rKfAFVJgg",
//                name = "User 3",
//                email = "user3@org1",
//                nameHashIsEmpty = false,
//                jpegThumbnail = null
//            )
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId = "VbvBzjdJfGC5apNttzS4oi1wGsPFU4EdFw5ijZAHcun",
//            multisigOpAccountAddress = "2nf2bKZtao2hK6f1ke69vq2Qqyj8swg7Ny4MZfR4jxHW",
//            walletAddress = "FvFqGJZEd1BeR2WggMUZPR8zHCHVfqTaEgRUAzcbuB4x",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "HMCoXMD8MozBU8ZFDfGDwTkVpWK2txXwesHdmvRvY95e",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = "4NwP6vHH4z2gopFoMOWR2T+dilDDJuXWKmQt5ckS7Ko=",
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getSignersUpdateRequest(nonceAccountAddresses: List<String>) = SignersUpdate(
//        type = ApprovalType.SIGNERS_UPDATE_TYPE.value,
//        slotUpdateType = SlotUpdateType.SetIfEmpty,
//        signer = SlotSignerInfo(
//            slotId = 1,
//            value = SignerInfo(
//                publicKey = "8Mj26LQKUUVMUX7z8Qsvjpz2x6hMLFaER9axqHK4PvP7",
//                name = "User 2",
//                email = "user2@org1",
//                nameHashIsEmpty = false,
//                jpegThumbnail = null
//            )
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId = "5JHcZTW5F1e5zWHb5zVWgi6pwbKuynRS3fyT7v48YEJZ",
//            multisigOpAccountAddress = "GjfqFH8T4BiL6pwtZxdq7BPZK7uaWJBKbooG4iQxSsaM",
//            walletAddress = "4B8ogHEgwh5CtnuCwyTXgxQjEfRgNcUpSTyhhtjKfL5Q",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "kwwyzySTUJHWBF64u15iwv8bMvcHTNmZzfEh9iDeC7j",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = "LZN57KM3swDiAbOZ1W0iwgGrJ2trzjaUGWX5Qc2zSK4=",
//            nonceAccountAddressesSlot = 2256
//        )
//    )
//
//fun getSignersUpdateWalletRequest(nonceAccountAddresses: List<String>) : ApprovalRequest {
//    return getWalletApprovalRequest(
//        getSignersUpdateRequest(nonceAccountAddresses)
//    )
//}
//
//fun getSolanaWalletCreationRequest(nonceAccountAddresses: List<String>) : WalletCreation {
//    return WalletCreation(
//        type = ApprovalType.WALLET_CREATION_TYPE.value,
//        accountSlot = 0,
//        accountInfo = AccountInfo(
//            name = "Account 1",
//            identifier = "c6055be1-a895-45a6-b0f3-fce261760b89",
//            accountType = AccountType.BalanceAccount,
//            chain = Chain.censo,
//            address = null
//        ),
//        approvalPolicy = ApprovalPolicy(
//            approvalsRequired = 1,
//            approvalTimeout = 3600000,
//            approvers = listOf(
//                SlotSignerInfo(
//                    slotId = 0,
//                    value = SignerInfo(
//                        name = "User 1",
//                        email = "authorized1@org1",
//                        publicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//                        nameHashIsEmpty = false,
//                        jpegThumbnail = null
//                    )
//                )
//            )
//        ),
//        whitelistEnabled = BooleanSetting.Off,
//        dappsEnabled = BooleanSetting.Off,
//        addressBookSlot = 1,
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
//            multisigOpAccountAddress = "2DBQ368KgyPkmqd6fKsQpmpMhBTDTuW6wWESbxDs5otz",
//            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getBitcoinWalletCreationRequest() : WalletCreation {
//    return WalletCreation(
//        type = ApprovalType.WALLET_CREATION_TYPE.value,
//        accountSlot = 0,
//        accountInfo = AccountInfo(
//            name = "Account 1",
//            identifier = "c6055be1-a895-45a6-b0f3-fce261760b89",
//            accountType = AccountType.BalanceAccount,
//            chain = Chain.bitcoin,
//            address = null
//        ),
//        approvalPolicy = ApprovalPolicy(
//            approvalsRequired = 1,
//            approvalTimeout = 3600000,
//            approvers = listOf(
//                SlotSignerInfo(
//                    slotId = 0,
//                    value = SignerInfo(
//                        name = "User 1",
//                        email = "authorized1@org1",
//                        publicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//                        nameHashIsEmpty = false,
//                        jpegThumbnail = null
//                    )
//                )
//            )
//        ),
//        whitelistEnabled = BooleanSetting.Off,
//        dappsEnabled = BooleanSetting.Off,
//        addressBookSlot = 0,
//        signingData = null
//    )
//}
//
//fun getEthereumWalletCreationRequest() : WalletCreation {
//    return WalletCreation(
//        type = ApprovalType.WALLET_CREATION_TYPE.value,
//        accountSlot = 0,
//        accountInfo = AccountInfo(
//            name = "Account 1",
//            identifier = "c6055be1-a895-45a6-b0f3-fce261760b89",
//            accountType = AccountType.BalanceAccount,
//            chain = Chain.ethereum,
//            address = null
//        ),
//        approvalPolicy = ApprovalPolicy(
//            approvalsRequired = 1,
//            approvalTimeout = 3600000,
//            approvers = listOf(
//                SlotSignerInfo(
//                    slotId = 0,
//                    value = SignerInfo(
//                        name = "User 1",
//                        email = "authorized1@org1",
//                        publicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//                        nameHashIsEmpty = false,
//                        jpegThumbnail = null
//                    )
//                )
//            )
//        ),
//        whitelistEnabled = BooleanSetting.Off,
//        dappsEnabled = BooleanSetting.Off,
//        addressBookSlot = 0,
//        signingData = null
//    )
//}
//
//fun getSolWithdrawalRequest(nonceAccountAddresses: List<String>) : WithdrawalRequest {
//    return WithdrawalRequest(
//        type = ApprovalType.WITHDRAWAL_TYPE.value,
//        account = AccountInfo(
//            name = "Account 1",
//            identifier = "c6055be1-a895-45a6-b0f3-fce261760b89",
//            accountType = AccountType.BalanceAccount,
//            address = "oRYGxVHXEqpLaH9QWxX8yRMzLsmPRXyfNmop2QrPQKY"
//        ),
//        symbolAndAmountInfo = SymbolAndAmountInfo(
//            symbolInfo = SymbolInfo(
//                symbol = "SOL",
//                symbolDescription = "Solana",
//                tokenMintAddress = "11111111111111111111111111111111"
//            ),
//            amount = "0.500000000",
//            nativeAmount = "0.500000000",
//            usdEquivalent = "17.75"
//        ),
//        destination = DestinationAddress(
//            name = "My External Sol address",
//            subName = null,
//            address = "2DQz5vWgs1PKxPDd9YaYKoemgFriRJqoFRniAQ7Wtuva",
//            tag = null
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
//            multisigOpAccountAddress = "9NDFtaczqouZ9SGTfd489EfN3KvMQgrAjpuu4QEr9Kys",
//            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getSplWithdrawalRequest(nonceAccountAddresses: List<String>) : WithdrawalRequest {
//    return WithdrawalRequest(
//        type = ApprovalType.WITHDRAWAL_TYPE.value,
//        account = AccountInfo(
//            name = "Account 1",
//            identifier = "5fb4556a-6de5-4a80-ac0e-6def9826384f",
//            accountType = AccountType.BalanceAccount,
//            address = "HT8kqgLxH5BsyA6Ah3oaAKG8SNAgzgRNH4uMfcAnUXTZ"
//        ),
//        symbolAndAmountInfo = SymbolAndAmountInfo(
//            symbolInfo = SymbolInfo(
//                symbol = "soTEST",
//                symbolDescription = "Test SPL token",
//                tokenMintAddress = "AZ6C941cFEv7EWUsPeeYYEK278Lw5wK4AVR6Mngdt9fr"
//            ),
//            amount = "0.000500",
//            nativeAmount = null,
//            usdEquivalent = null
//        ),
//        destination = DestinationAddress(
//            name = "Org1 Sol Wallet",
//            subName = null,
//            address = "7DhLZAT5buGyXpjpfRNKaHc1imjJaDzCXXTdM59JHrpQ",
//            tag = null
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
//            walletProgramId = "zeZ7E8F6UaNYy3ry3Mt6MGUSr679oTKV8tzXVe5B4bP",
//            multisigOpAccountAddress = "6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX",
//            walletAddress = "7fvoSJ6iNAyTFvBDuAWuciXWYiyUBtJfCUswZF3YGbUN",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getConversionRequest(nonceAccountAddresses: List<String>): ConversionRequest {
//    return ConversionRequest(
//        type = ApprovalType.CONVERSION_REQUEST_TYPE.value,
//        account = AccountInfo(
//            name = "Account 1",
//            identifier = "9826889c-df77-4c5b-b4ad-9bde935e6c52",
//            accountType = AccountType.BalanceAccount,
//            address = "F8MQFSzgGtddamGjNNoFuUfrZNZkV84icnXwyMVo7Aa3"
//        ),
//        symbolAndAmountInfo = SymbolAndAmountInfo(
//            symbolInfo = SymbolInfo(
//                symbol = "USDC",
//                symbolDescription = "USD Coin",
//                tokenMintAddress = "ALmJ9wWY2o1FiLcSDuvHN3xH5UHLkYsVbz2JWD37MuUY"
//            ),
//            amount = "500.000000",
//            nativeAmount = "500.000000",
//            usdEquivalent = "500.00"
//        ),
//        destination = DestinationAddress(
//            name = "USDC Redemption Address",
//            subName = null,
//            address = "Bt4cfS3fhtbCiB3uDXDRvft6SCVbHCH7Pz7kh66tzzKA",
//            tag = null
//        ),
//        destinationSymbolInfo = SymbolInfo(
//            symbol = "USD",
//            symbolDescription = "US Dollar",
//            tokenMintAddress = "11111111111111111111111111111111"
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "FBiyhqgyrv6iRejRgL9tDYxB2jtEB4RH9pnPK2CN5J4m",
//            walletProgramId = "CH2nLW24j2Wd1geFGSKkJmbAz1KLhACR9RRD1wHgCH74",
//            multisigOpAccountAddress = "11111111111111111111111111111111",
//            walletAddress = "2sGiNkpwYod6c1Wcd6H1ycd85KwykMfb8ZCt7t3XEp4h",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getWrapConversionRequest(nonceAccountAddresses: List<String>): WrapConversionRequest {
//    return WrapConversionRequest(
//        type = ApprovalType.WRAP_CONVERSION_REQUEST_TYPE.value,
//        account = AccountInfo(
//            name = "Account 1",
//            identifier = "82666cf4-3f31-4504-a1a2-5df9b35ba5b3",
//            accountType = AccountType.BalanceAccount,
//            address = "BSHKeDQL8NKBSmbX2M4svSqGL57qFhe7qvw72hpvgnZY"
//        ),
//        symbolAndAmountInfo = SymbolAndAmountInfo(
//            symbolInfo = SymbolInfo(
//                symbol = "SOL",
//                symbolDescription = "Solana",
//                tokenMintAddress = "11111111111111111111111111111111"
//            ),
//            amount = "0.500000000",
//            nativeAmount = "0.500000000",
//            usdEquivalent = "44.39"
//        ),
//        destinationSymbolInfo = SymbolInfo(
//            symbol = "wSOL",
//            symbolDescription = "Wrapped SOL",
//            tokenMintAddress = "11111111111111111111111111111111"
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
//            walletProgramId = "DaGSQwGd1GZnscN2Mu5d1CPYqYXAQMV29Q4Zk9yDhZLp",
//            multisigOpAccountAddress = "11111111111111111111111111111111",
//            walletAddress = "Ebse7xEiKuhe3bWY6dXiWB8QS4QDhr8fRBgH4tUKR2Ys",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getUnwrapConversionRequest(nonceAccountAddresses: List<String>) : ApprovalRequestDetails {
//    return  WrapConversionRequest(
//        type = ApprovalType.WRAP_CONVERSION_REQUEST_TYPE.value,
//        account= AccountInfo(
//            name= "Account 1",
//            identifier= "c2a6711d-8430-429f-816a-876eb62dd19e",
//            accountType= AccountType.BalanceAccount,
//            address= "7dMB51drmhKy9qQ8GjFPsaRDnadGCvn4iLWedqajbmUg"
//        ),
//        symbolAndAmountInfo= SymbolAndAmountInfo(
//            symbolInfo= SymbolInfo(
//                symbol= "wSOL",
//                symbolDescription= "Wrapped SOL",
//                tokenMintAddress= "11111111111111111111111111111111"
//            ),
//            amount= "0.300000000",
//            nativeAmount = "0.300000000",
//            usdEquivalent= "26.63"
//        ),
//        destinationSymbolInfo= SymbolInfo(
//            symbol= "SOL",
//            symbolDescription= "Solana",
//            tokenMintAddress= "11111111111111111111111111111111"
//        ),
//        signingData= SigningData.SolanaSigningData(
//            feePayer= "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId= "8S1mgAomg5mcJ6rC38xHMMJyFKTHxQc2dHgNrmQKzAz",
//            multisigOpAccountAddress= "11111111111111111111111111111111",
//            walletAddress= "HZmqaRJWQxB6B4DXCBmY5W8xjL2Wn5Q6rGHtajxUDbra",
//            nonceAccountAddresses= nonceAccountAddresses,
//            initiator = "3S3WAHv5h7gyEVTPQRuz6sf8poKM439zr14pHF43MtLK",
//            strikeFeeAmount = 2039280,
//            feeAccountGuidHash = "Oe1VO8ObkbQ2jHnzOD6tIGQNkX/sExJpdGOksGK47VU=",
//            walletGuidHash = "/Fz5hXppVfCrsvkgU8zXy5e3IO99xOmbQJuF7DUkHfw=",
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getCreateSolanaAddressBookEntry(nonceAccountAddresses: List<String>): ApprovalRequestDetails {
//    return CreateAddressBookEntry(
//        type = ApprovalType.CREATE_ADDRESS_BOOK_ENTRY_TYPE.value,
//        chain = Chain.censo,
//        slotId = 0,
//        name = "My External Sol address",
//        address = "2DQz5vWgs1PKxPDd9YaYKoemgFriRJqoFRniAQ7Wtuva",
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
//            multisigOpAccountAddress = "Dpt714om7J3B3f1ygptgoEnFvHo3aiXjeLPP7TqjHJhq",
//            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getCreateBitcoinAddressBookEntry(): ApprovalRequestDetails {
//    return CreateAddressBookEntry(
//        type = ApprovalType.CREATE_ADDRESS_BOOK_ENTRY_TYPE.value,
//        chain = Chain.bitcoin,
//        slotId = 0,
//        name = "My External Bitcoin address",
//        address = "2NG4bukdJgvzptpj3RXzFn7sGZDjBJwygB9",
//        signingData = null
//    )
//}
//
//fun getCreateEthereumAddressBookEntry(): ApprovalRequestDetails {
//    return CreateAddressBookEntry(
//        type = ApprovalType.CREATE_ADDRESS_BOOK_ENTRY_TYPE.value,
//        chain = Chain.ethereum,
//        slotId = 0,
//        name = "My External Ethereum address",
//        address = "0x23118ef009e46887fc5a868e879dc01194baa59e",
//        signingData = null
//    )
//}
//
//fun getDeleteSolanaAddressBookEntry(nonceAccountAddresses: List<String>): ApprovalRequestDetails {
//    return DeleteAddressBookEntry(
//        type = ApprovalType.DELETE_ADDRESS_BOOK_ENTRY_TYPE.value,
//        chain = Chain.censo,
//        slotId = 0,
//        name = "My External Sol address",
//        address = "2DQz5vWgs1PKxPDd9YaYKoemgFriRJqoFRniAQ7Wtuva",
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
//            multisigOpAccountAddress = "Dpt714om7J3B3f1ygptgoEnFvHo3aiXjeLPP7TqjHJhq",
//            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getDeleteBitcoinAddressBookEntry(): ApprovalRequestDetails {
//    return DeleteAddressBookEntry(
//        type = ApprovalType.DELETE_ADDRESS_BOOK_ENTRY_TYPE.value,
//        chain = Chain.bitcoin,
//        slotId = 0,
//        name = "My External Bitcoin address",
//        address = "2NG4bukdJgvzptpj3RXzFn7sGZDjBJwygB9",
//        signingData = null
//    )
//}
//
//fun getDeleteEthereumAddressBookEntry(): ApprovalRequestDetails {
//    return DeleteAddressBookEntry(
//        type = ApprovalType.DELETE_ADDRESS_BOOK_ENTRY_TYPE.value,
//        chain = Chain.ethereum,
//        slotId = 0,
//        name = "My External Ethereum address",
//        address = "0x23118ef009e46887fc5a868e879dc01194baa59e",
//        signingData = null
//    )
//}
//
//fun getAddDAppBookEntry(nonceAccountAddresses: List<String>) : ApprovalRequestDetails {
//    return  DAppBookUpdate(
//        type = ApprovalType.DAPP_BOOK_UPDATE_TYPE.value,
//        entriesToAdd= listOf(
//            SlotDAppInfo(
//                slotId= 0,
//                value= SolanaDApp(address= "GNGhSWWVNhXAy4fQgfAoQejJpGAxVaE4bdJjdb6iXRjK", name= "DApp", logo= "icon")
//            )
//        ),
//        entriesToRemove= emptyList(),
//        signingData= SigningData.SolanaSigningData(
//            feePayer= "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
//            walletProgramId= "3Nh3QsaXKbTbLM1BLsD4dhT4zeHTPaVbZX3eN3Yg1G2w",
//            multisigOpAccountAddress= "Hn2CJuYyyB2H3wwmdHPy1Aun2Jkye3MCSVajzUvw55A9",
//            walletAddress= "Re4dLGch8a1G98PeRtpHa5ApS6Gnik444CqB5BQ8rY1",
//            nonceAccountAddresses= nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getWalletConfigPolicyUpdate(nonceAccountAddresses: List<String>): ApprovalRequestDetails {
//    return WalletConfigPolicyUpdate(
//        type = ApprovalType.WALLET_CONFIG_POLICY_UPDATE_TYPE.value,
//        approvalPolicy = ApprovalPolicy(
//            approvalsRequired = 2,
//            approvalTimeout = 18000000,
//            approvers = listOf(
//                SlotSignerInfo(
//                    slotId = 0,
//                    value = SignerInfo(
//                        publicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//                        name = "User 1",
//                        email = "authorized1@org1",
//                        nameHashIsEmpty = false,
//                        jpegThumbnail = null
//                    )
//                ),
//                SlotSignerInfo(
//                    slotId = 1,
//                    value = SignerInfo(
//                        publicKey = "7AH35qStXtrUgRkmqDmhjufNHjF74R1A9cCKT3C3HaAR",
//                        name = "User 2",
//                        email = "user2@org1",
//                        nameHashIsEmpty = false,
//                        jpegThumbnail = null
//                    )
//                ),
//            )
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
//            multisigOpAccountAddress = "F6iUTdJDE4vnTgBanCtBgtoNHag57Uaut82xATGVVps3",
//            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getBalanceAccountPolicyUpdate(nonceAccountAddresses: List<String>) : ApprovalRequestDetails {
//    return BalanceAccountPolicyUpdate(
//        type = ApprovalType.BALANCE_ACCOUNT_POLICY_UPDATE_TYPE.value,
//        accountInfo= AccountInfo(
//            name= "Account 1",
//            identifier= "1ac4a7fc-d2f8-4c32-8707-7496ee958933",
//            accountType= AccountType.BalanceAccount,
//            address= "5743aqK2n9xnTSmFcbzTmfpdtcNeWdJsCxTxrCcNXUFH"
//        ),
//        approvalPolicy= ApprovalPolicy(
//            approvalsRequired= 2,
//            approvalTimeout= 3600000,
//            approvers= listOf(
//                SlotSignerInfo(
//                    slotId= 0,
//                    value= SignerInfo(publicKey= "4q8ApWsB3rSW2HPFwc1aWmGgcBMfj7tSKBbb5sBGAB6h", name= "User 1", email= "authorized1@org1", nameHashIsEmpty = false, jpegThumbnail = null)
//                ),
//                SlotSignerInfo(
//                    slotId= 1,
//                    value= SignerInfo(publicKey= "CDrdR8xX8t83eXxB2ESuHp9AxkiJkUuKnD98zyDfMtrG", name= "User 2", email= "user2@org1", nameHashIsEmpty = false, jpegThumbnail = null)
//                ),
//            )
//        ),
//        signingData= SigningData.SolanaSigningData(
//            feePayer= "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
//            walletProgramId= "8pPAcjFSByreFRnRm5YyAdBP2LfiNnWBtBzHtRDcJpUA",
//            multisigOpAccountAddress= "DbdTEwihgEYJYAgXBKEqQGknGyHsRnxE5coeZaVS4T9y",
//            walletAddress= "ECzeaMTMBXYXXfVM53n5iPepf8749QUqEzjW8jxefGhh",
//            nonceAccountAddresses= nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getBalanceAccountNameUpdate(nonceAccountAddresses: List<String>): ApprovalRequestDetails {
//    return BalanceAccountNameUpdate(
//        type = ApprovalType.BALANCE_ACCOUNT_NAME_UPDATE_TYPE.value,
//        accountInfo = AccountInfo(
//            name = "Account 1",
//            identifier = "b645a5d9-227f-4a9f-9331-52af64bf1989",
//            accountType = AccountType.BalanceAccount,
//            address = "DcvZ2k6ygvvu2Z5ihrSxRZL7bHJ38gPRgpCie8GzztTP"
//        ),
//        newAccountName = "New Name",
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
//            walletProgramId = "7kNPVcK2cpyaZsLsqmhZbjcbt433vYUckH1PM5gZeJ1L",
//            multisigOpAccountAddress = "7DY87mHHiSSyxFBbhCYbTpQE5M4Jk9Z9hymJ7UzL3sPm",
//            walletAddress = "4XaqL4MtTUDrncTGBqvTC9ketf8WVqrUocDkYhKAnDju",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getDAppTransactionRequest(nonceAccountAddresses: List<String>): ApprovalRequestDetails {
//    return DAppTransactionRequest(
//        type = ApprovalType.DAPP_TRANSACTION_REQUEST_TYPE.value,
//        account = AccountInfo(
//            name = "Account 1",
//            identifier = "f8d0a964-ea88-4843-973a-70e3a6ff8ab8",
//            accountType = AccountType.BalanceAccount,
//            address = "BRZxhTAUTwMokJyCMpuJcGtNdi8j8hApkwDWserqRFKr"
//        ),
//        dappInfo = SolanaDApp(
//            address = "H2ZSeYCg4MwnCYh73biZJfwMSJ6KuugoH8JLf775cUns",
//            name = "DApp Name",
//            logo = "dapp-icon"
//        ),
//        balanceChanges = emptyList(),
//        instructions = listOf(
//            SolanaInstructionChunk(
//                offset = 0,
//                instructionData = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAMfbXr9Pd04d9oxuffFREfSADbnRIPEpVn6V8fj2rrdmAGa4BO3DrDz6LyXzXydHrFWh2+J/9ZlPFdeiuHbyV8P3QwAAgAAAADC6wsAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAO9DFnzyLMsRmbgWH6JSH00UGHDt6bU14c2amFy43OtpgGa4BO3DrDz6LyXzXydHrFWh2+J/9ZlPFdeiuHbyV8P3QwAAgAAAADC6wsAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAMkSqNHZ0rdcs4p7lXiRTF5mdcdy2a5YKT6j6+BzOGthQGa4BO3DrDz6LyXzXydHrFWh2+J/9ZlPFdeiuHbyV8P3QwAAgAAAADC6wsAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAMv6SewjB9Bp/SAwlD3Dp3rVnnZqhlAFfBk8YNOpmSmbgGa4BO3DrDz6LyXzXydHrFWh2+J/9ZlPFdeiuHbyV8P3QwAAgAAAADC6wsAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAANsBX+mLaLauW1bAZ+zXt5Z8/XY7ehn5ra8f6XKuuomsAGa4BO3DrDz6LyXzXydHrFWh2+J/9ZlPFdeiuHbyV8P3QwAAgAAAADC6wsAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAM8Sd9NN2/+E90TCtcvBH4m11ETLdINd++tdjWUYZWUIwGa4BO3DrDz6LyXzXydHrFWh2+J/9ZlPFdeiuHbyV8P3QwAAgAAAADC6wsAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAM6Da5ybuajCBKiBb85gQGHPCHZktkOJYZwT+BQHm5N5wGa4BO3DrDz6LyXzXydHrFWh2+J/9ZlPFdeiuHbyV8P3QwAAgA="
//            ),
//            SolanaInstructionChunk(
//                offset = 788,
//                instructionData = "AAAAwusLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgADPa0Joq3Q2jHq6IeVnzS6oclt1ixZHlI7BKopyvdi8WwBmuATtw6w8+i8l818nR6xVodvif/WZTxXXorh28lfD90MAAIAAAAAwusLAAAAAA=="
//            )
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId = "5VouXXXz6WnRT2hGAeKqSoiqgdYMs5cAhtobuUh54AkS",
//            multisigOpAccountAddress = "Az1ZYXQYk1CraDLgLF72ge7bLd8k6kn4GWjPqiXco88t",
//            walletAddress = "GrdFiFNc6xC7fzC8ejcotE3uxPm8UTpndNWiEg4kALKW",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "6f51NkToPKgK2AY6GGf7scdbwXpZGNmkx6wvCdp9rQPW",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = "6ETtMeamm1fdYpXyezvfQqisy8nFFEDocw/HGyFFCUM=",
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getLoginApproval( jwtToken : String) : ApprovalRequestDetails {
//    return LoginApprovalRequest(
//        type = ApprovalType.LOGIN_TYPE.value,
//        jwtToken= jwtToken,
//        email = "tony@google.com",
//        name = "Sam Ortiz"
//    )
//}
//
//fun getWalletInitiationRequest(requestType: ApprovalRequestDetails, initiation: MultiSigOpInitiation) : ApprovalRequest {
//    return ApprovalRequest(
//        id= "1",
//        submitterName= "",
//        submitterEmail= "",
//        submitDate= Date().toString(),
//        approvalTimeoutInSeconds= 1000,
//        numberOfDispositionsRequired= 1,
//        numberOfApprovalsReceived= 1,
//        numberOfDeniesReceived= 1,
//        vaultName = "Test Vault",
//        initiationOnly = true,
//        details= SolanaApprovalRequestDetails.MultiSignOpInitiationDetails(initiation, requestType= requestType)
//    )
//}
//
//fun getOpAccountCreationInfo(accountSize: Long = 952, minBalanceForRentExemption: Long = 7516800) : MultiSigAccountCreationInfo {
//    return MultiSigAccountCreationInfo(
//        accountSize = accountSize,
//        minBalanceForRentExemption = minBalanceForRentExemption
//    )
//}
//
//fun getBalanceAccountAddressWhitelistUpdate(nonceAccountAddresses: List<String>): ApprovalRequestDetails {
//    return BalanceAccountAddressWhitelistUpdate(
//        type = ApprovalType.BALANCE_ACCOUNT_ADDRESS_WHITE_LIST_UPDATE_TYPE.value,
//        accountInfo = AccountInfo(
//            name = "Account 1",
//            identifier = "4d2eecc1-cbe1-4c36-a4ae-1f777a739eb3",
//            accountType = AccountType.BalanceAccount,
//            address = "HvZFxso1tq9FLD1Gh2ACGNsR5pQBgjVC8uo21Cc9ytzg"
//        ),
//        destinations = listOf(
//            SlotDestinationInfo(
//                slotId = 1,
//                value = DestinationAddress(
//                    name = "My External Sol address 1",
//                    subName = null,
//                    address = "AXX2TNxGhW2M3GpQPuWVuqmyAvQFVpyZD2dvR9gRiMRQ",
//                    tag = null
//                )
//            ),
//            SlotDestinationInfo(
//                slotId = 2,
//                value = DestinationAddress(
//                    name = "My External Sol address 2",
//                    subName = null,
//                    address = "2db8ovVF6iXTaPQAhJe3frG46iNLF5Ny7ZipGKDomiTh",
//                    tag = null
//                )
//            )
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
//            walletProgramId = "9LM4sYmMHk1VDcFpA8ezPeL8GtEVR5T51Qxcksrf4VX2",
//            multisigOpAccountAddress = "71S5qEAD3DMn7QY9fdb2uR1TV7kiAfcAqNHfQfyFUSME",
//            walletAddress = "AoEAvW2TvZYmy2WbmqN4nXdJT8o21RbJP6xNK2yR4of",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getBalanceAccountSettingsUpdate(nonceAccountAddresses: List<String>): ApprovalRequestDetails {
//    return BalanceAccountSettingsUpdate(
//        type = ApprovalType.BALANCE_ACCOUNT_SETTINGS_UPDATE_TYPE.value,
//        account = AccountInfo(
//            name = "Account 1",
//            identifier = "c6055be1-a895-45a6-b0f3-fce261760b89",
//            accountType = AccountType.BalanceAccount,
//            address = "oRYGxVHXEqpLaH9QWxX8yRMzLsmPRXyfNmop2QrPQKY"
//        ),
//        whitelistEnabled = BooleanSetting.On,
//        dappsEnabled = null,
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "87VXbkJsqdDvXYfDBtS4kW4TcFor7ogofZXbXjT7t7AU",
//            walletProgramId = "db4pdTHvA3XLBgKfwKzdx8DcNpHuWWn63t6u8kbYiuS",
//            multisigOpAccountAddress = "Dp4oaRWRtBxQdf5Lg2zti3TCjsUsxv4rUBgtf2HSQnVb",
//            walletAddress = "JCd6uutAtgsbxDfM54ss4TyeG6kakvSfdxJwjBTjkPLh",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//fun getRemoveDAppBookEntry(nonceAccountAddresses: List<String>): ApprovalRequestDetails {
//    return DAppBookUpdate(
//        type = ApprovalType.DAPP_BOOK_UPDATE_TYPE.value,
//        entriesToAdd = emptyList(),
//        entriesToRemove = listOf(
//            SlotDAppInfo(
//                slotId = 0,
//                value = SolanaDApp(
//                    address = "GNGhSWWVNhXAy4fQgfAoQejJpGAxVaE4bdJjdb6iXRjK",
//                    name = "DApp",
//                    logo = "icon"
//                )
//            )
//        ),
//        signingData = SigningData.SolanaSigningData(
//            feePayer = "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as",
//            walletProgramId = "3Nh3QsaXKbTbLM1BLsD4dhT4zeHTPaVbZX3eN3Yg1G2w",
//            multisigOpAccountAddress = "9CfoFci2agjCJ7bWqfgKEFSAc5zB6UR63MrK61nRaJzm",
//            walletAddress = "Re4dLGch8a1G98PeRtpHa5ApS6Gnik444CqB5BQ8rY1",
//            nonceAccountAddresses = nonceAccountAddresses,
//            initiator = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ",
//            strikeFeeAmount = 0,
//            feeAccountGuidHash = emptyHash,
//            walletGuidHash = emptyHash,
//            nonceAccountAddressesSlot = 2272
//        )
//    )
//}
//
//
//
