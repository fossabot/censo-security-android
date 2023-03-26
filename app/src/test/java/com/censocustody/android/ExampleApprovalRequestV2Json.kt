package com.censocustody.android


//region Multi Sign Op Approvals

val vaultPolicyJson =
    """
        {"id":"b842a31b-aadc-4f35-adda-90670e564a18","submitDate":"2023-01-30T19:34:01.007+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"VaultPolicyUpdate","approvalPolicy":{"approvalsRequired":2,"approvalTimeout":3600000,"approvers":[{"slotId":0,"value":{"name":"User 1","email":"authorized1@org1","publicKeys":[{"chain":"bitcoin","key":"tpubDF4Sv98dWcmFsSGKyXsKEQu6TxxeeCDPtv8zm6Mgm1Tno2TvtGbDRp86x5iXhnjm9BaKonr4DwdZ7iJ78TLAkNpk9ckHREeBDaSVPnG9Hwp"},{"chain":"censo","key":"PQz3kTmS2CuR8D4LRoZWrjWQmPHvwzvN5b9VRshf6o5LVKFGn8VveKw8gbdmoYuwz9A7yfijV2QMBJDhwPGFXaBH"},{"chain":"ethereum","key":"R4ToeRnb7TbJzd7YjB554gC9ssMpTMZxHt7Gm1SxDnDKXym7vUnP95jn47feogCSbgiSm1V3zsC7PDc5a2q8FL5x"},{"chain":"polygon","key":"PKs5xn6af9DfchvvZcCjkbcson3V8e2mbNPGmnrb4TMRgoMVojTqWeagi84SUGr3tDGDsx4z2giQCEZY2VnLbJSX"}],"nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}},{"slotId":1,"value":{"name":"User 2","email":"user2@org1","publicKeys":[{"chain":"bitcoin","key":"tpubDErqTuDP6DkzWqok2Q9ds8R1LETFfb54zp7nYAX1g52poa1ChV7cPbGxMeDjRJG3nDnVYGHudhuVzdBvNerSDU2ztwaNBbCjNyvW9ZajNfd"},{"chain":"censo","key":"Mdou45FSikR7CUGcsRBmWrubpWwgcDHao5NZQ9xEyoCw5VKoGHNWZySV7dKU3cZ9qG2LEUZDE8GgHj1AQAhezcbk"},{"chain":"ethereum","key":"PwXsHSb4Ke5rxxDu7iTQ99dhQ6375ag4LQ2VJxpqmtSkZhWnBsLPPvKGBeLHU2gBjtcHk5oPcAywEc4YMtUpYedG"},{"chain":"polygon","key":"RoRXHvy73y27139ZAiAzkrrbhgAJ6SRdf9wHgTJkmaLTPpxFgBzUHRFtq6tDRBhxXoRRBnvdaNKi1868Qsq21YVT"}],"nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}}]},"currentOnChainPolicies":[{"chain":"ethereum","owners":["0xc3c1c38e46ee66c12ce5bb8b19f104b852e20edf"],"threshold":1},{"chain":"polygon","owners":["0x86f922a45f704811df20115eb91ac6848653dff6"],"threshold":1}],"signingData":[{"type":"ethereum","transaction":{"safeNonce":0,"chainId":31337,"priorityFee":null,"vaultAddress":"0x983e5b1a007bc24bf9c16139f64ebe5ad10d3154","contractAddresses":[]}},{"type":"polygon","transaction":{"safeNonce":0,"chainId":31337,"priorityFee":null,"vaultAddress":"0xc23b2e57a1b1665a6f2f1358e5a25e9d6477e079","contractAddresses":[]}}]},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent()

// Initiation for a wallet creation:
val exampleRequests = listOf(
    """
        {"id":"edeb9c6e-26cd-41aa-81db-850f0a170295","submitDate":"2023-01-30T17:13:51.163+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":300,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"Login","jwtToken":"*****","email":"authorized1@org1","name":"User 1"},"vaultName":null,"initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"b842a31b-aadc-4f35-adda-90670e564a18","submitDate":"2023-01-30T19:34:01.007+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"VaultPolicyUpdate","approvalPolicy":{"approvalsRequired":2,"approvalTimeout":3600000,"approvers":[{"slotId":0,"value":{"name":"User 1","email":"authorized1@org1","publicKeys":[{"chain":"bitcoin","key":"tpubDF4Sv98dWcmFsSGKyXsKEQu6TxxeeCDPtv8zm6Mgm1Tno2TvtGbDRp86x5iXhnjm9BaKonr4DwdZ7iJ78TLAkNpk9ckHREeBDaSVPnG9Hwp"},{"chain":"censo","key":"PQz3kTmS2CuR8D4LRoZWrjWQmPHvwzvN5b9VRshf6o5LVKFGn8VveKw8gbdmoYuwz9A7yfijV2QMBJDhwPGFXaBH"},{"chain":"ethereum","key":"R4ToeRnb7TbJzd7YjB554gC9ssMpTMZxHt7Gm1SxDnDKXym7vUnP95jn47feogCSbgiSm1V3zsC7PDc5a2q8FL5x"},{"chain":"polygon","key":"PKs5xn6af9DfchvvZcCjkbcson3V8e2mbNPGmnrb4TMRgoMVojTqWeagi84SUGr3tDGDsx4z2giQCEZY2VnLbJSX"}],"nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}},{"slotId":1,"value":{"name":"User 2","email":"user2@org1","publicKeys":[{"chain":"bitcoin","key":"tpubDErqTuDP6DkzWqok2Q9ds8R1LETFfb54zp7nYAX1g52poa1ChV7cPbGxMeDjRJG3nDnVYGHudhuVzdBvNerSDU2ztwaNBbCjNyvW9ZajNfd"},{"chain":"censo","key":"Mdou45FSikR7CUGcsRBmWrubpWwgcDHao5NZQ9xEyoCw5VKoGHNWZySV7dKU3cZ9qG2LEUZDE8GgHj1AQAhezcbk"},{"chain":"ethereum","key":"PwXsHSb4Ke5rxxDu7iTQ99dhQ6375ag4LQ2VJxpqmtSkZhWnBsLPPvKGBeLHU2gBjtcHk5oPcAywEc4YMtUpYedG"},{"chain":"polygon","key":"RoRXHvy73y27139ZAiAzkrrbhgAJ6SRdf9wHgTJkmaLTPpxFgBzUHRFtq6tDRBhxXoRRBnvdaNKi1868Qsq21YVT"}],"nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}}]},"currentOnChainPolicies":[{"chain":"ethereum","owners":["0xc3c1c38e46ee66c12ce5bb8b19f104b852e20edf"],"threshold":1},{"chain":"polygon","owners":["0x86f922a45f704811df20115eb91ac6848653dff6"],"threshold":1}],"signingData":[{"type":"ethereum","transaction":{"safeNonce":0,"chainId":31337,"priorityFee":null,"orgVaultAddress":"0x983e5b1a007bc24bf9c16139f64ebe5ad10d3154", "vaultAddress":"0x983e5b1a007bc24bf9c16139f64ebe5ad10d3154","contractAddresses":[]}},{"type":"polygon","transaction":{"safeNonce":0,"chainId":31337,"priorityFee":null,"orgVaultAddress":"0x983e5b1a007bc24bf9c16139f64ebe5ad10d3154","vaultAddress":"0xc23b2e57a1b1665a6f2f1358e5a25e9d6477e079","contractAddresses":[]}}]},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"30f51c6c-6f89-44d5-807a-cc24d16789c2","submitDate":"2023-01-31T15:23:32.595+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":2,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"EthereumWalletCreation","identifier":"4cce3a9e-e8fa-4a06-8712-87ff54fa83db","name":"Ethereum Wallet 1","approvalPolicy":{"approvalsRequired":2,"approvalTimeout":3600000,"approvers":[{"slotId":0,"value":{"name":"User 2","email":"user2@org1","publicKey":"S7KRVJ54Q6DyS4Tu91zqNujQdpTFoyM26uGfeSvxwv37fx48zb7qYp2KREmTJFtSLax2itPrG2aHNnQ5CgJMne44","nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}},{"slotId":1,"value":{"name":"User 1","email":"authorized1@org1","publicKey":"RabgW6pBcwjEQsvr6a672kJCj4r9hLzAzBkRA7n2zxZKGQGfHAhvjrVGqvW7nMZQS6KobDF3xXPqaMAzXEM7UmgF","nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}}]},"whitelistEnabled":"Off","dappsEnabled":"Off","fee":{"value":"0.0096600000","nativeValue":"0.0096600000","usdEquivalent":"42.86"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"}},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"26d3d448-8486-42c5-8123-a250eb2df762","submitDate":"2023-01-31T15:23:36.446+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":2,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"EthereumTransferPolicyUpdate","wallet":{"identifier":"4cce3a9e-e8fa-4a06-8712-87ff54fa83db","name":"Ethereum Wallet 1","address":"0x54b67baf1676f53f32ea4cbbed5c59f24ed3e214"},"currentOnChainPolicy":{"chain":"ethereum","owners":["0xacbfa93c84191041b6cc4cbbd6fb6b874b0f9284","0x62c92a2c5f6d8426876b06c0d9745efc4106729d"],"threshold":2},"approvalPolicy":{"approvalsRequired":1,"approvalTimeout":3600000,"approvers":[{"slotId":0,"value":{"name":"User 1","email":"authorized1@org1","publicKey":"RabgW6pBcwjEQsvr6a672kJCj4r9hLzAzBkRA7n2zxZKGQGfHAhvjrVGqvW7nMZQS6KobDF3xXPqaMAzXEM7UmgF","nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}},{"slotId":1,"value":{"name":"User 2","email":"user2@org1","publicKey":"S7KRVJ54Q6DyS4Tu91zqNujQdpTFoyM26uGfeSvxwv37fx48zb7qYp2KREmTJFtSLax2itPrG2aHNnQ5CgJMne44","nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}}]},"fee":{"value":"0.0008712018","nativeValue":"0.0008712018","usdEquivalent":"3.87"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"},"signingData":{"type":"ethereum","transaction":{"safeNonce":0,"chainId":31337,"priorityFee":null,"vaultAddress":"0x2aa5d06545c77c7128958c1288dca679c23ef592","contractAddresses":[]}}},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"78edd512-2ada-4da1-8d39-41a74122f68a","submitDate":"2023-01-31T17:02:30.613+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":2,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"EthereumWalletSettingsUpdate","wallet":{"identifier":"f1c1896b-8ad8-4e66-8bd5-cd14cd4d8dca","name":"Ethereum Wallet 1","address":"0x02fa50cbe7bafda4c25a4f152545e255c0cf4da1"},"whitelistEnabled":"On","dappsEnabled":null,"fee":{"value":"0.0006747643","nativeValue":"0.0006747643","usdEquivalent":"2.99"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"},"currentGuardAddress":"0xcfb8f2880159b8a2aa1e382da7e1e62041c6be6a","signingData":{"type":"ethereum","transaction":{"safeNonce":0,"chainId":31337,"priorityFee":null,"vaultAddress":"0xaab37107ca0f888deabebb7993f5fa899b9bbc4c","contractAddresses":[{"name":"CensoGuard","address":"0xcfb8f2880159b8a2aa1e382da7e1e62041c6be6a"},{"name":"CensoTransfersOnlyGuard","address":"0x15d96af797b05005255c8ffaa793e7bb39c38ce2"},{"name":"CensoTransfersOnlyWhitelistingGuard","address":"0x4d21b409e1c33f902f81761e0ae58432a3a7402f"},{"name":"CensoWhitelistingGuard","address":"0xe4b88be4c876001f818ce42b62ebd6482b0ad5f5"}]}}},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"34bd84e3-d538-464f-ae6a-6de9af54a2cb","submitDate":"2023-01-31T17:02:33.521+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":2,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"CreateAddressBookEntry","chain":"ethereum","address":"0x6E01aF3913026660Fcebb93f054345eCCd972251","name":"Address 1"},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"e1d5accd-5f64-43be-b4bc-3e065eb23160","submitDate":"2023-01-31T17:02:36.593+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":2,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"EthereumWalletWhitelistUpdate","wallet":{"identifier":"f1c1896b-8ad8-4e66-8bd5-cd14cd4d8dca","name":"Ethereum Wallet 1","address":"0x02fa50cbe7bafda4c25a4f152545e255c0cf4da1"},"destinations":[{"address":"0x6E01aF3913026660Fcebb93f054345eCCd972251","name":"Address 1"}],"currentOnChainWhitelist":[],"fee":{"value":"0.0014742980","nativeValue":"0.0014742980","usdEquivalent":"6.54"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"},"signingData":{"type":"ethereum","transaction":{"safeNonce":1,"chainId":31337,"priorityFee":null,"vaultAddress":"0xaab37107ca0f888deabebb7993f5fa899b9bbc4c","contractAddresses":[{"name":"CensoFallbackHandler","address":"0xe7762ed24aeb9edc413b3debd32824b9b5a3a163"}]}}},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"c00085c6-4350-4f99-a05e-14dff709b68c","submitDate":"2023-01-31T17:04:08.132+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"EthereumWithdrawalRequest","wallet":{"identifier":"98a36396-d4b5-4c7b-9789-a55d8d7e0c40","name":"Ethereum Wallet 1","address":"0xF0e86822Cf7bD35588235C8Eef342767C4d4F2Ec"},"amount":{"value":"1","nativeValue":"1","usdEquivalent":null},"symbolInfo":{"symbol":"DAPE","description":"0xae6df2a79a225978ce278dcc9f0dde21b1be3029 at ethereum","imageUrl":"https://arweave.net/F4q5hB2bkAhqZvLfJVqcncxTGWUSG8toaS78s4QDx_Y","tokenInfo":{"type":"ERC721","contractAddress":"0xAe6df2A79a225978ce278dCc9F0DdE21B1BE3029","tokenId":"8333"},"nftMetadata":{"name":"Degenerate Ape Academy"}},"fee":{"value":"0.0006458281","nativeValue":"0.0006458281","usdEquivalent":"2.87"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"},"destination":{"name":"0x6E01aF3913026660Fcebb93f054345eCCd972251","address":"0x6E01aF3913026660Fcebb93f054345eCCd972251"},"signingData":{"type":"ethereum","transaction":{"safeNonce":0,"chainId":31337,"priorityFee":5000000000,"vaultAddress":null,"contractAddresses":[]}}},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"8e5816d4-f7a0-4a0a-a240-1ef3a950c81a","submitDate":"2023-01-31T17:04:09.285+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"EthereumWithdrawalRequest","wallet":{"identifier":"98a36396-d4b5-4c7b-9789-a55d8d7e0c40","name":"Ethereum Wallet 1","address":"0xF0e86822Cf7bD35588235C8Eef342767C4d4F2Ec"},"amount":{"value":"1","nativeValue":"1","usdEquivalent":null},"symbolInfo":{"symbol":"The Six Dragons SFT","description":"0x0a009fdc8bbf6b0bb19b11d0c627a1ad02ebb076 at ethereum","imageUrl":"https://thesixdragons.com/enjintokens/FinalTokensResizedSmall/SFT.png","tokenInfo":{"type":"ERC1155","contractAddress":"0x0A009fDc8bBf6B0bB19b11d0C627a1ad02ebB076","tokenId":"50659039041325842496780740897450011362710825167765800721389013580860476096512"},"nftMetadata":{"name":"The Six Dragons SFT"}},"fee":{"value":"0.0006413488","nativeValue":"0.0006413488","usdEquivalent":"2.85"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"},"destination":{"name":"0x6E01aF3913026660Fcebb93f054345eCCd972251","address":"0x6E01aF3913026660Fcebb93f054345eCCd972251"},"signingData":{"type":"ethereum","transaction":{"safeNonce":0,"chainId":31337,"priorityFee":5000000000,"vaultAddress":null,"contractAddresses":[]}}},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"79be3491-fe1b-4558-ae5c-9f6ff45a59b4","submitDate":"2023-01-31T17:04:23.452+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"EthereumWithdrawalRequest","wallet":{"identifier":"98a36396-d4b5-4c7b-9789-a55d8d7e0c40","name":"Ethereum Wallet 1","address":"0xF0e86822Cf7bD35588235C8Eef342767C4d4F2Ec"},"amount":{"value":"10.000000","nativeValue":"10.000000","usdEquivalent":"10.00"},"symbolInfo":{"symbol":"USDC","description":"USD Coin","imageUrl":"https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/CpMah17kQEL2wqyMKt3mZBdTnZbkbfx4nqmQMFDP5vwp/logo.png","tokenInfo":{"type":"ERC20","contractAddress":"0x977021634d6f7cE65Ce9904FF91aeDd6ccbD2165"}},"fee":{"value":"0.0006355750","nativeValue":"0.0006355750","usdEquivalent":"2.82"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"},"destination":{"name":"0x6E01aF3913026660Fcebb93f054345eCCd972251","address":"0x6E01aF3913026660Fcebb93f054345eCCd972251"},"signingData":{"type":"ethereum","transaction":{"safeNonce":3,"chainId":31337,"priorityFee":5000000000,"vaultAddress":null,"contractAddresses":[]}}},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"316d0247-a1cb-47d9-b447-04ba544967e8","submitDate":"2023-01-31T17:04:24.359+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"EthereumWithdrawalRequest","wallet":{"identifier":"98a36396-d4b5-4c7b-9789-a55d8d7e0c40","name":"Ethereum Wallet 1","address":"0xF0e86822Cf7bD35588235C8Eef342767C4d4F2Ec"},"amount":{"value":"1","nativeValue":"1","usdEquivalent":null},"symbolInfo":{"symbol":"GGSG","description":"0x2bddb61d0bf888de57eb5060d7f69317694431ff at ethereum","imageUrl":"https://www.arweave.net/r_K7MRot4iVWht_QKp9wiPpfCvC39bXi9cQsEn0B6WY?ext=jpeg","tokenInfo":{"type":"ERC721","contractAddress":"0x2BDDb61d0bF888De57EB5060d7F69317694431fF","tokenId":"5055"},"nftMetadata":{"name":"Galactic Gecko #5055"}},"fee":{"value":"0.0006353371","nativeValue":"0.0006353371","usdEquivalent":"2.82"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"},"destination":{"name":"0x6E01aF3913026660Fcebb93f054345eCCd972251","address":"0x6E01aF3913026660Fcebb93f054345eCCd972251"},"signingData":{"type":"ethereum","transaction":{"safeNonce":3,"chainId":31337,"priorityFee":5000000000,"vaultAddress":null,"contractAddresses":[]}}},"vaultName":"Test Organization 1","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"b9c39bf0-65e9-4598-8526-d7576ef76534","submitDate":"2023-02-22T20:16:51.238+00:00","submitterName":"Paul Rudd","submitterEmail":"paul.rudd@blue.rock","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":2,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"VaultCreation","name":"BlueRock Securities","approvalPolicy":{"approvalsRequired":1,"approvalTimeout":3600000,"approvers":[{"name":"Paul Rudd","email":"paul.rudd@blue.rock","publicKeys":[{"chain":"bitcoin","key":"tpubDFFpopNKAaGy3JGjCKkDExtoWbq48hyGxXDGaLuUykShMdEsUUnYesjqkWo4M261DmVxSusN4ip3xMZopQMcA4AhjRh6DLrjgPrQWKm2Ugo"},{"chain":"ethereum","key":"NpARHNbC51CgDdDtdmE5VTAv796vwVr3zkhfimfTB891MowDboKqzEn9nZX8A3QiemoRxUYnDN7AUd6EV9YkhrLo"},{"chain":"offchain","key":"NUDAubqyzGP13zYr1R9fdA95vpXnGsuRQen7RRFBYjPDbgmEFmuBS2L749GAajcC1nfWZqcEQ6Nzag7C7htra1or"}],"nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}]},"signingData":[],"chainFees":[]},"vaultName":"Org Vault","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"2f4bdeb0-ffaa-44ba-876b-ad0f3db3c6aa","submitDate":"2023-03-24T23:33:15.713+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"OrgAdminPolicyUpdate","approvalPolicy":{"approvalsRequired":2,"approvalTimeout":18000000,"approvers":[{"name":"User 1","email":"authorized1@org1","publicKeys":[{"chain":"bitcoin","key":"xpub6Efp9E12RNBCDC9Z4U8D2ouzGH7hfCXzdNaojqixfBHZujFNKWQinxChVdcde9FbP1DVUaQJFVpiMvm536fYj8vDy9xjeu9eSzPtZGvp477"},{"chain":"ethereum","key":"QceoBLd3h4giKQJVAUdsLGcFXPHGZvizcn43n3vqctV4s8zF6VmUfWoUypU9WK5drdd4N2DTA1vvYTrvd7JvsAL5"},{"chain":"offchain","key":"PrSy5kksqUAB7PTHTKqrzykDtrP3sv1DW7c4N3HkEsanaTbMfvyeqXwtPqNMRPXFSYqmGghXuppuMdHbbdjQpEJt"}],"nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="},{"name":"User 2","email":"user2@org1","publicKeys":[{"chain":"bitcoin","key":"xpub6EkEJvpaG1JmEUtDkKNS2uZX5REnETWzJtxcbNU9otNo3oT4PHBCbTMeNqe1tYNULNe7USUbip9sjWTx3uXvCmY2jtp3yvZLwALVhWUdrLV"},{"chain":"ethereum","key":"SaFpJaeSztbbejsrPUoocrUHjaEJ17coYqhJN1Uv2Bc44AWRLMXYv1nzJW6uQUecZtwihedhd3zV4R4J4WY1yZ88"},{"chain":"offchain","key":"SPDMi6yFfxEkXAn55UVQYqw2MQeFegxpiFcDnLsMsEfQeSkdAevZ3awPhXMMMmrsB1QFfqC62ckaZVwrSH2jJ3HQ"}],"nameHashIsEmpty":false,"jpegThumbnail":"/9j/ .. 2Q=="}]},"currentOnChainPolicies":[{"chain":"ethereum","owners":["0xfbaf9d33478db5e6b3933506eb6eccf715e223aa"],"threshold":1},{"chain":"polygon","owners":["0xfbaf9d33478db5e6b3933506eb6eccf715e223aa"],"threshold":1}],"signingData":[{"type":"ethereum","transaction":{"safeNonce":0,"chainId":31337,"vaultAddress":"0x88962b6946be64bd340eb825799523716b8f5019","orgVaultAddress":null,"contractAddresses":[]}},{"type":"polygon","transaction":{"safeNonce":0,"chainId":31337,"vaultAddress":"0x88962b6946be64bd340eb825799523716b8f5019","orgVaultAddress":null,"contractAddresses":[]}}],"chainFees":[{"chain":"ethereum","fee":{"value":"0.0010509168","nativeValue":"0.001050916800761000","usdEquivalent":"1.73"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"}},{"chain":"polygon","fee":{"value":"0.0044759168","nativeValue":"0.004475916800761000","usdEquivalent":"0.01"},"feeSymbolInfo":{"symbol":"MATIC","description":"Polygon","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/MATIC.svg"}}],"shardingPolicyChangeInfo":{"currentPolicyRevisionGuid":"f2e007e0-2fdc-4981-9371-bbc1ec57b249","targetPolicy":{"policyRevisionGuid":"4a58ef17-d164-42d3-8e67-0e754c6dcf8a","threshold":2,"participants":[{"participantId":"e059cda1136827ee1124d664cff34b5bf97bc31a2b4f22a4643809453b6856b0","devicePublicKeys":["RxbVrngH5W4Lpn4hCLziEVgv58mxjo5JUwd8w3NYGu8uVf2iSq2Q2AhyJvubwQy1a9eA38nMM2m5zD8PEwwmBvUY"]},{"participantId":"b305a0af50611820ee1d31b596328a938a9ed18f42fe842aafb813767146aa48","devicePublicKeys":["R42p5JP9jcNBMPPvxHbn7dNEjwJVTBdEBDkk3sh2D2h7HQ4HB6BvPJWLLfBB5bEBe4TUqnbbdiCbb1ihL2Aa4ALp"]}]}}},"vaultName":"Org Vault","initiationOnly":false}
    """.trimIndent(),
    """
        {"id":"ec51035a-1bd9-4ad5-b5d0-cfeafc4cf6d7","submitDate":"2023-03-01T23:35:43.332+00:00","submitterName":"User 1","submitterEmail":"authorized1@org1","approvalTimeoutInSeconds":null,"numberOfDispositionsRequired":2,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"VaultNameUpdate","oldName":"Main","newName":"Secondary","signingData":[{"type":"ethereum","transaction":{"safeNonce":0,"chainId":31337,"vaultAddress":"0x05f452bb3198f375b6f33204b0b618fa3ed89198","orgVaultAddress":"0xe9f827f98d6d81f5f62adf79b94b3fd9471a4580","contractAddresses":[{"name":"CensoFallbackHandler","address":"0x0059861115b078b9f34538b04bced76fa279dad1"}]}},{"type":"polygon","transaction":{"safeNonce":0,"chainId":31337,"vaultAddress":"0x05f452bb3198f375b6f33204b0b618fa3ed89198","orgVaultAddress":"0xe9f827f98d6d81f5f62adf79b94b3fd9471a4580","contractAddresses":[{"name":"CensoFallbackHandler","address":"0x0059861115b078b9f34538b04bced76fa279dad1"}]}}],"chainFees":[{"chain":"ethereum","fee":{"value":"0.0009457006","nativeValue":"0.000945700550496000","usdEquivalent":"4.20"},"feeSymbolInfo":{"symbol":"ETH","description":"Ethereum","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/ETH.svg"}},{"chain":"polygon","fee":{"value":"0.0045457006","nativeValue":"0.004545700550496000","usdEquivalent":"0.01"},"feeSymbolInfo":{"symbol":"MATIC","description":"Polygon","imageUrl":"https://s3.us-east-1.amazonaws.com/strike-public-assets/logos/MATIC.svg"}}]},"vaultName":"Org Vault","initiationOnly":false}
    """.trimIndent()
)
//endregion