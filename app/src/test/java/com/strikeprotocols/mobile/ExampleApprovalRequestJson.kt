package com.strikeprotocols.mobile


//region Multi Sign Op Approvals

// Initiation for a balance account creation:
val multiSigWithBalanceAccountCreationJson = """
    {"id":"7b6a84f6-53cf-42d4-9923-997b26b3afef","walletType":"Solana","submitDate":"2022-05-10T13:03:58.032+00:00","submitterName":"Sam Harris","submitterEmail":"sharris@blue.rock","approvalTimeoutInSeconds":1800,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"MultisigOpInitiation","details":{"type":"BalanceAccountCreation","accountSlot":3,"accountInfo":{"identifier":"b0cc02d2-0af4-4736-be6e-d6e0d2fc1271","name":"SamDev","accountType":"BalanceAccount"},"approvalPolicy":{"approvalsRequired":2,"approvalTimeout":10800000,"approvers":[{"slotId":9,"value":{"publicKey":"AithTQwaVwvvamSk15Kon4e8rU1Jw79Wjo5Fop61yUfw","name":"Zakarie Ortiz","email":"zortiz@blue.rock"}},{"slotId":10,"value":{"publicKey":"BrEiGBArXzSXkNmyTKiNb754qqqHHYi26Dh4F2ypnJR5","name":"Sam Harris","email":"sharris@blue.rock"}}]},"whitelistEnabled":"Off","dappsEnabled":"Off","addressBookSlot":8,"stakingValidator":null,"signingData":{"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer":"DUcwYFXaSp3te5B56KtdXwaQGAj39MSYFzRznf2J1g3p","walletProgramId":"74VNF7BHs2iBWkNB8hCKwKC4ygEqP59ui3ZDtcPBc3up","multisigOpAccountAddress":"11111111111111111111111111111111","walletAddress":"7q7JVUxWN3m2mUJeVoQyAaCgALAh2yPaJtmYAoZ8MxMS","nonceAccountAddresses":["DybefrurMKtpNsKgQDQvyNcimk5UD1Me8zpMuT8s1Xs2"], "initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"}},"opAccountCreationInfo":{"accountSize":848,"minBalanceForRentExemption":6792960}}}
    """.trim()

    // Initiation for removing an signer
    val multiSigWithSignersUpdateJson = """
    {"id": "40a7fb51-a39c-4afa-aa3b-5c4f77939026", "walletType": "Solana", "submitDate": "2022-04-05T14:43:19.120+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 1800, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": {"type": "MultisigOpInitiation", "details": {"type": "SignersUpdate", "slotUpdateType": "Clear", "signer": {"slotId": 2, "value": {"publicKey": "HwW3iudK4dcqcMm76iy7iBziJU4htaYZGTjzCBnYA6rH", "name": "User 3", "email": "user3@org1"}}, "signingData": {"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "GN694sm2Ex1GcnamYwqfjSs6XJ7xadTXiZqBwvGvQyT8", "multisigOpAccountAddress": "11111111111111111111111111111111", "walletAddress": "6JmmkmowSLQ3jFQacREDNwbrD3Hj7Eyj9MvK8eBTzV5q", "nonceAccountAddresses":["57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"], "initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"}}, "opAccountCreationInfo": {"accountSize": 848, "minBalanceForRentExemption": 6792960}}}
    """.trim()

    // Transfer initiation:
    val multiSigWithWithdrawalRequestJson = """
    {"id":"c9721d40-5e51-4d77-8389-7e5399d0fc04","walletType":"Solana","submitDate":"2022-05-10T13:02:19.550+00:00","submitterName":"Sam Harris","submitterEmail":"sharris@blue.rock","approvalTimeoutInSeconds":1800,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"MultisigOpInitiation","details":{"type":"WithdrawalRequest","account":{"identifier":"ee14520f-0305-4d01-aa34-31d726057d84","name":"Reserves","accountType":"BalanceAccount","address":"8aa3H9SdPz55R6bWMtsRnNJkmxE4n7z3ZG35eytd35dP"},"symbolAndAmountInfo":{"symbolInfo":{"symbol":"SOL","symbolDescription":"Solana","tokenMintAddress":"11111111111111111111111111111111"},"amount":"1.000000000","nativeAmount":"1.000000000","usdEquivalent":"88.77"},"destination":{"name":"Transfers","address":"VPJxxgoEFsJPZ3as2LpkWiJABYksQnpUWg9ssPrtjAo"},"signingData":{"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer":"DUcwYFXaSp3te5B56KtdXwaQGAj39MSYFzRznf2J1g3p","walletProgramId":"74VNF7BHs2iBWkNB8hCKwKC4ygEqP59ui3ZDtcPBc3up","multisigOpAccountAddress":"11111111111111111111111111111111","walletAddress":"7q7JVUxWN3m2mUJeVoQyAaCgALAh2yPaJtmYAoZ8MxMS","nonceAccountAddresses":["57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"], "initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"}},"opAccountCreationInfo":{"accountSize":848,"minBalanceForRentExemption":6792960}}}
    """.trim()


    // DApp initiation
    val multiSignWithDAppRequestJson = """
    {"id":"8ab353ee-7c91-4d39-b041-042352455c63","walletType":"Solana","submitDate":"2022-04-05T19:39:50.166+00:00","submitterName":"Ben Holzman","submitterEmail":"bholzman2@blue.rock","approvalTimeoutInSeconds":1800,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"MultisigOpInitiation","details":{"type":"DAppTransactionRequest","account":{"identifier":"5096c2f1-74de-4c2e-8a61-0024a83f14b3","name":"Trading","accountType":"BalanceAccount","address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD"},"balanceChanges":[{"symbolInfo":{"symbol":"SOL","symbolDescription":"Solana"},"amount":"0.023981600","nativeAmount":"0.023981600","usdEquivalent":"3.13"},{"symbolInfo":{"symbol":"SRM","symbolDescription":"Serum","tokenMintAddress":"SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt"},"amount":"2.000000","nativeAmount":"2.000000","usdEquivalent":"5.94"}],"instructions":[{"from":0,"instructions":[{"programId":"11111111111111111111111111111111","accountMetas":[{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true}],"data":"AAAAAPAdHwAAAAAApQAAAAAAAAAG3fbh12Whk9nL4UbO63msHLSF7V9bN5E6jPWFfv8AqQ=="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"So11111111111111111111111111111111111111112","signer":false,"writable":false},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"SysvarRent111111111111111111111111111111111","signer":false,"writable":false}],"data":"AQ=="},{"programId":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","accountMetas":[{"address":"jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai","signer":false,"writable":true},{"address":"5KrN1vytDRuRxRDZc5EoTKGhXFqZiR7evy1zTR8irhwZ","signer":false,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"EhAJTsW745jiWjViB7Q4xXcgKf6tMF7RcMX9cbTuXVBk","signer":false,"writable":true},{"address":"HFSNnAxfhDt4DnmY9yVs2HNFnEMaDJ7RxMVNB9Y5Hgjr","signer":false,"writable":true},{"address":"FUH3FvpU6M7zNpaJ7fSyVD8UiaTWGxmbciwHxJACcEbA","signer":false,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"6vBhv2L33KVJvAQeiaW3JEZLrJU7TtGaqcwPdrhytYWG","signer":false,"writable":false},{"address":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","signer":false,"writable":false}],"data":"AAUAAAA="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true}],"data":"CQ=="}]},{"from":0,"instructions":[{"programId":"11111111111111111111111111111111","accountMetas":[{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true}],"data":"AAAAAPAdHwAAAAAApQAAAAAAAAAG3fbh12Whk9nL4UbO63msHLSF7V9bN5E6jPWFfv8AqQ=="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"So11111111111111111111111111111111111111112","signer":false,"writable":false},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"SysvarRent111111111111111111111111111111111","signer":false,"writable":false}],"data":"AQ=="},{"programId":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","accountMetas":[{"address":"jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai","signer":false,"writable":true},{"address":"5KrN1vytDRuRxRDZc5EoTKGhXFqZiR7evy1zTR8irhwZ","signer":false,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"EhAJTsW745jiWjViB7Q4xXcgKf6tMF7RcMX9cbTuXVBk","signer":false,"writable":true},{"address":"HFSNnAxfhDt4DnmY9yVs2HNFnEMaDJ7RxMVNB9Y5Hgjr","signer":false,"writable":true},{"address":"FUH3FvpU6M7zNpaJ7fSyVD8UiaTWGxmbciwHxJACcEbA","signer":false,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"6vBhv2L33KVJvAQeiaW3JEZLrJU7TtGaqcwPdrhytYWG","signer":false,"writable":false},{"address":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","signer":false,"writable":false}],"data":"AAUAAAA="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true}],"data":"CQ=="}]},{"from":0,"instructions":[{"programId":"11111111111111111111111111111111","accountMetas":[{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true}],"data":"AAAAAPAdHwAAAAAApQAAAAAAAAAG3fbh12Whk9nL4UbO63msHLSF7V9bN5E6jPWFfv8AqQ=="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"So11111111111111111111111111111111111111112","signer":false,"writable":false},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"SysvarRent111111111111111111111111111111111","signer":false,"writable":false}],"data":"AQ=="},{"programId":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","accountMetas":[{"address":"jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai","signer":false,"writable":true},{"address":"5KrN1vytDRuRxRDZc5EoTKGhXFqZiR7evy1zTR8irhwZ","signer":false,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"EhAJTsW745jiWjViB7Q4xXcgKf6tMF7RcMX9cbTuXVBk","signer":false,"writable":true},{"address":"HFSNnAxfhDt4DnmY9yVs2HNFnEMaDJ7RxMVNB9Y5Hgjr","signer":false,"writable":true},{"address":"FUH3FvpU6M7zNpaJ7fSyVD8UiaTWGxmbciwHxJACcEbA","signer":false,"writable":true},{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"6vBhv2L33KVJvAQeiaW3JEZLrJU7TtGaqcwPdrhytYWG","signer":false,"writable":false},{"address":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","signer":false,"writable":false}],"data":"AAUAAAA="},{"programId":"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA","accountMetas":[{"address":"5AtDseqdHrq8X62QHwqr3gfZRLhDk7XDP6wVTUgMJXWN","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true},{"address":"CpWpnyS9equ4dALyAHpVPGEhZkAgxD1RYuxZncMLYuSD","signer":true,"writable":true}],"data":"CQ=="}]}],"signingData":{"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer":"59CH4KuZWQpyGbkEtUhzM9KbYEsssJ7NgM89db5GehLj","walletProgramId":"6m1icfABEiCG3vm4w9YL9QBTc7AN4ApU9VY38XmQH9VC","multisigOpAccountAddress":"11111111111111111111111111111111","walletAddress":"HNNg5RDk1o35APqnrDUJniT6ngyZbisEcsAPTjzcPuPK","nonceAccountAddresses":["57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"],"initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"},"dappInfo":{"address":"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin","name":"Serum dApp","logo":"https://raw.githubusercontent.com/project-serum/awesome-serum/master/logo-serum.png","url":"https://serum-demo2.strikeprotocols.com"}},"opAccountCreationInfo":{"accountSize":848,"minBalanceForRentExemption":6792960}}}
    """.trim()

    // Conversion initiation
    val multiSigWithConversionRequestJson = """
        {"id": "ffc2b7de-ad6a-4a89-b57c-513d2cfb42a3", "walletType": "Solana", "submitDate": "2022-04-06T09:45:38.678+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 1800, "numberOfDispositionsRequired": 2, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": {"type": "MultisigOpInitiation", "details": {"type": "ConversionRequest", "account": {"identifier": "22b5c380-9049-4905-af4d-cce44284cce6", "name": "Account 1", "accountType": "BalanceAccount", "address": "s4HnJVY6B4yBPY57csiYinGEeJsrHFZZScLxMNJr6Kk"}, "symbolAndAmountInfo": {"symbolInfo": {"symbol": "USDC", "symbolDescription": "USD Coin", "tokenMintAddress": "ALmJ9wWY2o1FiLcSDuvHN3xH5UHLkYsVbz2JWD37MuUY"}, "amount": "500.000000", "nativeAmount": "500.000000", "usdEquivalent": "500.00"}, "destination": {"name": "USDC Redemption Address", "address": "Emfuy1FWbVtNLgTum38rrK3EbkXonwxtNyPztwFi3r8a"}, "destinationSymbolInfo": {"symbol": "USD", "symbolDescription": "US Dollar", "tokenMintAddress": "11111111111111111111111111111111"}, "signingData": {"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "ALKWER79Nt7AzhHwS99wWjXNtLvBCAHJYyAhEDYDVEpF", "multisigOpAccountAddress": "11111111111111111111111111111111", "walletAddress": "AYsBiTxSFnqRooYiH2B6rrMRtVXRCrUTTXa3L121fUMB","nonceAccountAddresses":["57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"], "initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"}}, "opAccountCreationInfo": {"accountSize": 848, "minBalanceForRentExemption": 6792960}}}
    """.trim()
    //endregion

    //region Standard Approvals

    // Approval for a new signer - (just an approval - no initiatiion)
    val signersUpdateJson = """
        {"id": "13cd643e-393e-4b38-91cb-5f1ab2655223", "walletType": "Solana", "submitDate": "2022-04-05T14:42:53.015+00:00", "submitterName": "User 2", "submitterEmail": "user2@org1", "approvalTimeoutInSeconds": 18000, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": {"type": "SignersUpdate", "slotUpdateType": "SetIfEmpty", "signer": {"slotId": 1, "value": {"publicKey": "HhNwcVMrJX8newbDVderrnsmvG6uGYuxUUvzm6BqdjzH", "name": "User 2", "email": "user2@org1"}}, "signingData": {"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "GN694sm2Ex1GcnamYwqfjSs6XJ7xadTXiZqBwvGvQyT8", "multisigOpAccountAddress": "DHgNPbMHz66DQacFdo4rN8pks9Lw1zpfqqqAymHUgQkg", "walletAddress": "6JmmkmowSLQ3jFQacREDNwbrD3Hj7Eyj9MvK8eBTzV5q","nonceAccountAddresses":["57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"], "initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"}}}
    """.trim()

    // Approval for balance account creation:
    val balanceAccountCreationJson = """
    {"id":"7b6a84f6-53cf-42d4-9923-997b26b3afef","walletType":"Solana","submitDate":"2022-05-10T13:03:58.032+00:00","submitterName":"Sam Harris","submitterEmail":"sharris@blue.rock","approvalTimeoutInSeconds":1800,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"BalanceAccountCreation","accountSlot":3,"accountInfo":{"identifier":"b0cc02d2-0af4-4736-be6e-d6e0d2fc1271","name":"SamDev","accountType":"BalanceAccount"},"approvalPolicy":{"approvalsRequired":2,"approvalTimeout":10800000,"approvers":[{"slotId":9,"value":{"publicKey":"AithTQwaVwvvamSk15Kon4e8rU1Jw79Wjo5Fop61yUfw","name":"Zakarie Ortiz","email":"zortiz@blue.rock"}},{"slotId":10,"value":{"publicKey":"BrEiGBArXzSXkNmyTKiNb754qqqHHYi26Dh4F2ypnJR5","name":"Sam Harris","email":"sharris@blue.rock"}}]},"whitelistEnabled":"Off","dappsEnabled":"Off","addressBookSlot":8,"stakingValidator":null,"signingData":{"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer":"DUcwYFXaSp3te5B56KtdXwaQGAj39MSYFzRznf2J1g3p","walletProgramId":"74VNF7BHs2iBWkNB8hCKwKC4ygEqP59ui3ZDtcPBc3up","multisigOpAccountAddress":"11111111111111111111111111111111","walletAddress":"7q7JVUxWN3m2mUJeVoQyAaCgALAh2yPaJtmYAoZ8MxMS","nonceAccountAddresses":["DybefrurMKtpNsKgQDQvyNcimk5UD1Me8zpMuT8s1Xs2"], "initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"}}}""".trim()

    // Approval for removing a signer
    val signersUpdateRemovalJson = """
        {"id": "40a7fb51-a39c-4afa-aa3b-5c4f77939026", "walletType": "Solana", "submitDate": "2022-04-05T14:43:19.120+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 18000, "numberOfDispositionsRequired": 2, "numberOfApprovalsReceived": 1, "numberOfDeniesReceived": 0, "details": {"type": "SignersUpdate", "slotUpdateType": "Clear", "signer": {"slotId": 2, "value": {"publicKey": "HwW3iudK4dcqcMm76iy7iBziJU4htaYZGTjzCBnYA6rH", "name": "User 3", "email": "user3@org1"}}, "signingData": {"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "GN694sm2Ex1GcnamYwqfjSs6XJ7xadTXiZqBwvGvQyT8", "multisigOpAccountAddress": "CngursCC8PNA7x3tP2jVXXrvgSWu1NtNCYMnzT5v4jnp", "walletAddress": "6JmmkmowSLQ3jFQacREDNwbrD3Hj7Eyj9MvK8eBTzV5q","nonceAccountAddresses":["57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"], "initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"}}}
    """.trim()

    // Transfer approval
    val withdrawalRequestJson = """
        {"id":"c9721d40-5e51-4d77-8389-7e5399d0fc04","walletType":"Solana","submitDate":"2022-05-10T13:02:19.550+00:00","submitterName":"Sam Harris","submitterEmail":"sharris@blue.rock","approvalTimeoutInSeconds":1800,"numberOfDispositionsRequired":1,"numberOfApprovalsReceived":0,"numberOfDeniesReceived":0,"details":{"type":"WithdrawalRequest","account":{"identifier":"ee14520f-0305-4d01-aa34-31d726057d84","name":"Reserves","accountType":"BalanceAccount","address":"8aa3H9SdPz55R6bWMtsRnNJkmxE4n7z3ZG35eytd35dP"},"symbolAndAmountInfo":{"symbolInfo":{"symbol":"SOL","symbolDescription":"Solana","tokenMintAddress":"11111111111111111111111111111111"},"amount":"1.000000000","nativeAmount":"1.000000000","usdEquivalent":"88.77"},"destination":{"name":"Transfers","address":"VPJxxgoEFsJPZ3as2LpkWiJABYksQnpUWg9ssPrtjAo"},"signingData":{"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer":"DUcwYFXaSp3te5B56KtdXwaQGAj39MSYFzRznf2J1g3p","walletProgramId":"74VNF7BHs2iBWkNB8hCKwKC4ygEqP59ui3ZDtcPBc3up","multisigOpAccountAddress":"11111111111111111111111111111111","walletAddress":"7q7JVUxWN3m2mUJeVoQyAaCgALAh2yPaJtmYAoZ8MxMS","nonceAccountAddresses":["57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"], "initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"}}}
    """.trim()

    // Conversion approval:
    val conversionRequestJson = """
        {"id": "ffc2b7de-ad6a-4a89-b57c-513d2cfb42a3", "walletType": "Solana", "submitDate": "2022-04-06T09:45:38.678+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 3600, "numberOfDispositionsRequired": 2, "numberOfApprovalsReceived": 1, "numberOfDeniesReceived": 0, "details": {"type": "ConversionRequest", "account": {"identifier": "22b5c380-9049-4905-af4d-cce44284cce6", "name": "Account 1", "accountType": "BalanceAccount", "address": "s4HnJVY6B4yBPY57csiYinGEeJsrHFZZScLxMNJr6Kk"}, "symbolAndAmountInfo": {"symbolInfo": {"symbol": "USDC", "symbolDescription": "USD Coin", "tokenMintAddress": "ALmJ9wWY2o1FiLcSDuvHN3xH5UHLkYsVbz2JWD37MuUY"}, "amount": "500.000000", "nativeAmount": "500.000000", "usdEquivalent": "500.00"}, "destination": {"name": "USDC Redemption Address", "address": "Emfuy1FWbVtNLgTum38rrK3EbkXonwxtNyPztwFi3r8a"}, "destinationSymbolInfo": {"symbol": "USD", "symbolDescription": "US Dollar", "tokenMintAddress": "11111111111111111111111111111111"}, "signingData": {"strikeFeeAmount":"0","feeAccountGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","walletGuidHash":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=","feePayer": "FM36ah2bH8nQWJNPCRzu7R69gE5o6UhujqJFtDpWN5as", "walletProgramId": "ALKWER79Nt7AzhHwS99wWjXNtLvBCAHJYyAhEDYDVEpF", "multisigOpAccountAddress": "DpBrGAFpqspN2Fe46NJUPBxV6AnWKenuTpj4doK6Gt4p", "walletAddress": "AYsBiTxSFnqRooYiH2B6rrMRtVXRCrUTTXa3L121fUMB","nonceAccountAddresses":["57bGarSm6DxPnWds3KVWMVkDZ9s4D8WGnqm6DSzBiLpN"], "initiator":"3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ"}}}
    """.trim()

    // Login Approval
    const val EXAMPLE_JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZCI6IlNvbHIifQ.SWCJDd6B_m7xr_puQH-wgbxvXyJYXH9lTpldOU0eQKc"
    val loginApprovalJson = """
        {"id": "13cd643e-393e-4b38-91cb-5f1ab2655223", "walletType": "Solana", "submitDate": "2022-04-05T14:42:53.015+00:00", "submitterName": "User 2", "submitterEmail": "user2@org1", "approvalTimeoutInSeconds": 18000, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "details": { "type": "LoginApproval", "jwtToken": "$EXAMPLE_JWT_TOKEN"}}
    """.trim()

    // dapp transaction
    val dappTransactionJson = "{\"id\":\"6e905b8f-04a3-4d27-939b-1cdb7a9c6ad7\",\"walletType\":\"Solana\",\"submitDate\":\"2022-06-16T20:23:35.580+00:00\",\"submitterName\":\"Brendan4 Android\",\"submitterEmail\":\"brendan4@android.com\",\"approvalTimeoutInSeconds\":86565,\"numberOfDispositionsRequired\":1,\"numberOfApprovalsReceived\":1,\"numberOfDeniesReceived\":0,\"details\":{\"type\":\"DAppTransactionRequest\",\"account\":{\"identifier\":\"242488f1-2646-4682-a489-98169b3300b1\",\"name\":\"Trading\",\"accountType\":\"BalanceAccount\",\"address\":\"F8UMXRbEG3gCiLwxo2W8YA7iozozeEUvTvpxhPfUMxQz\"},\"balanceChanges\":[{\"symbolInfo\":{\"symbol\":\"SOL\",\"symbolDescription\":\"Solana\"},\"amount\":\"-1.500000000\",\"nativeAmount\":\"-1.500000000\",\"usdEquivalent\":\"-133.16\"}],\"instructions\":[{\"from\":0,\"instructions\":[{\"programId\":\"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin\",\"accountMetas\":[{\"address\":\"jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai\",\"signer\":false,\"writable\":true},{\"address\":\"Fx15MivJTQokQZKazxGCsbWxRsx3uGrawkTidoBDrHv8\",\"signer\":false,\"writable\":true},{\"address\":\"nyZdeD16L5GxJq7Pso8R6KFfLA8R9v7c5A2qNaGWR44\",\"signer\":false,\"writable\":true},{\"address\":\"4ZTJfhgKPizbkFXNvTRNLEncqg85yJ6pyT7NVHBAgvGw\",\"signer\":false,\"writable\":true},{\"address\":\"7hLgwZhHD1MRNyiF1qfAjfkMzwvP3VxQMLLTJmKSp4Y3\",\"signer\":false,\"writable\":true},{\"address\":\"EhAJTsW745jiWjViB7Q4xXcgKf6tMF7RcMX9cbTuXVBk\",\"signer\":false,\"writable\":true},{\"address\":\"HFSNnAxfhDt4DnmY9yVs2HNFnEMaDJ7RxMVNB9Y5Hgjr\",\"signer\":false,\"writable\":true}],\"data\":\"AAIAAAAFAA==\"},{\"programId\":\"11111111111111111111111111111111\",\"accountMetas\":[{\"address\":\"F8UMXRbEG3gCiLwxo2W8YA7iozozeEUvTvpxhPfUMxQz\",\"signer\":true,\"writable\":true},{\"address\":\"A2kfCYZig74k2Tcee5M1fFqW3EdUqhiQe3aCaFsZ1YGu\",\"signer\":true,\"writable\":true}],\"data\":\"AAAAAECn5VoAAAAApQAAAAAAAAAG3fbh12Whk9nL4UbO63msHLSF7V9bN5E6jPWFfv8AqQ==\"},{\"programId\":\"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA\",\"accountMetas\":[{\"address\":\"A2kfCYZig74k2Tcee5M1fFqW3EdUqhiQe3aCaFsZ1YGu\",\"signer\":true,\"writable\":true},{\"address\":\"So11111111111111111111111111111111111111112\",\"signer\":false,\"writable\":false},{\"address\":\"F8UMXRbEG3gCiLwxo2W8YA7iozozeEUvTvpxhPfUMxQz\",\"signer\":true,\"writable\":true},{\"address\":\"SysvarRent111111111111111111111111111111111\",\"signer\":false,\"writable\":false}],\"data\":\"AQ==\"}]},{\"from\":3,\"instructions\":[{\"programId\":\"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin\",\"accountMetas\":[{\"address\":\"jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai\",\"signer\":false,\"writable\":true},{\"address\":\"8feNVBiz2tuxze1Su2iXjrwtHHsDhGRDu35NrcQ6D3Vu\",\"signer\":false,\"writable\":true},{\"address\":\"Fx15MivJTQokQZKazxGCsbWxRsx3uGrawkTidoBDrHv8\",\"signer\":false,\"writable\":true},{\"address\":\"nyZdeD16L5GxJq7Pso8R6KFfLA8R9v7c5A2qNaGWR44\",\"signer\":false,\"writable\":true},{\"address\":\"4ZTJfhgKPizbkFXNvTRNLEncqg85yJ6pyT7NVHBAgvGw\",\"signer\":false,\"writable\":true},{\"address\":\"7hLgwZhHD1MRNyiF1qfAjfkMzwvP3VxQMLLTJmKSp4Y3\",\"signer\":false,\"writable\":true},{\"address\":\"A2kfCYZig74k2Tcee5M1fFqW3EdUqhiQe3aCaFsZ1YGu\",\"signer\":true,\"writable\":true},{\"address\":\"F8UMXRbEG3gCiLwxo2W8YA7iozozeEUvTvpxhPfUMxQz\",\"signer\":true,\"writable\":true},{\"address\":\"EhAJTsW745jiWjViB7Q4xXcgKf6tMF7RcMX9cbTuXVBk\",\"signer\":false,\"writable\":true},{\"address\":\"HFSNnAxfhDt4DnmY9yVs2HNFnEMaDJ7RxMVNB9Y5Hgjr\",\"signer\":false,\"writable\":true},{\"address\":\"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA\",\"signer\":false,\"writable\":false},{\"address\":\"SysvarRent111111111111111111111111111111111\",\"signer\":false,\"writable\":false},{\"address\":\"6sT9rbAzecVn5D3A9uLEBogzWnrx3fKMXuxG52DFp1SA\",\"signer\":false,\"writable\":false}],\"data\":\"AAoAAAAAAAAAHgAAAAAAAAD0AQAAAAAAAAAvaFkAAAAAAAAAAAAAAAAAAAAAAAAAAP//\"},{\"programId\":\"TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA\",\"accountMetas\":[{\"address\":\"A2kfCYZig74k2Tcee5M1fFqW3EdUqhiQe3aCaFsZ1YGu\",\"signer\":true,\"writable\":true},{\"address\":\"F8UMXRbEG3gCiLwxo2W8YA7iozozeEUvTvpxhPfUMxQz\",\"signer\":true,\"writable\":true},{\"address\":\"F8UMXRbEG3gCiLwxo2W8YA7iozozeEUvTvpxhPfUMxQz\",\"signer\":true,\"writable\":true}],\"data\":\"CQ==\"}]},{\"from\":5,\"instructions\":[{\"programId\":\"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin\",\"accountMetas\":[{\"address\":\"jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai\",\"signer\":false,\"writable\":true},{\"address\":\"Fx15MivJTQokQZKazxGCsbWxRsx3uGrawkTidoBDrHv8\",\"signer\":false,\"writable\":true},{\"address\":\"nyZdeD16L5GxJq7Pso8R6KFfLA8R9v7c5A2qNaGWR44\",\"signer\":false,\"writable\":true},{\"address\":\"4ZTJfhgKPizbkFXNvTRNLEncqg85yJ6pyT7NVHBAgvGw\",\"signer\":false,\"writable\":true},{\"address\":\"7hLgwZhHD1MRNyiF1qfAjfkMzwvP3VxQMLLTJmKSp4Y3\",\"signer\":false,\"writable\":true},{\"address\":\"EhAJTsW745jiWjViB7Q4xXcgKf6tMF7RcMX9cbTuXVBk\",\"signer\":false,\"writable\":true},{\"address\":\"HFSNnAxfhDt4DnmY9yVs2HNFnEMaDJ7RxMVNB9Y5Hgjr\",\"signer\":false,\"writable\":true}],\"data\":\"AAIAAAAFAA==\"}]}],\"signingData\":{\"strikeFeeAmount\":\"0\",\"feeAccountGuidHash\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\",\"walletGuidHash\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=\",\"feePayer\":\"DUcwYFXaSp3te5B56KtdXwaQGAj39MSYFzRznf2J1g3p\",\"walletProgramId\":\"9Pimpz1iYqkP72bT3ZQ6Lw9xXKRoVptqSjVsk1YZeLKA\",\"multisigOpAccountAddress\":\"DW3abYyxVPcFq9fB6KDK233guZFU3fR6fbq1pBB7j93o\",\"walletAddress\":\"A9ogNyTwYqTi5R1H6S5ZAYfrFYTwsJq2bCzyaWD9qXb5\",\"nonceAccountAddresses\":[\"Dxo6uzf3dbwiyaQ4wx5HTXbNyHifmDxm6dfQqRLzGBSq\"],\"initiator\":\"6Zrs8eEtB53CVjdwRCgh1e3ieZj95JLHopHQRsEc8ZGQ\"},\"dappInfo\":{\"address\":\"9xQeWvG816bUx9EPjHmaT23yvVM2ZWbrrpZb9PusVFin\",\"name\":\"Serum dApp\",\"logo\":\"https://raw.githubusercontent.com/project-serum/awesome-serum/master/logo-serum.png\",\"url\":\"https://serum-prerelease.strikeprotocols.com\"}}}"

    val acceptVaultInvitationJson = """{"id": "422e3504-4eea-493a-a0dd-64a001115540", "walletType": "Solana", "submitDate": "2022-06-21T14:20:38.145+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 9223372036854775807, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "programVersion": null, "details": {"type": "AcceptVaultInvitation", "vaultGuid": "58e03f93-b9bc-4f22-b485-8e7a0abd8440", "vaultName": "Test Organization 1"}, "vaultName": "Test Organization 1"}"""
    val passwordResetJson = """{"id": "422e3504-4eea-493a-a0dd-64a001115540", "walletType": "Solana", "submitDate": "2022-06-21T14:20:38.145+00:00", "submitterName": "User 1", "submitterEmail": "authorized1@org1", "approvalTimeoutInSeconds": 9223372036854775807, "numberOfDispositionsRequired": 1, "numberOfApprovalsReceived": 0, "numberOfDeniesReceived": 0, "programVersion": null, "details": {"type": "PasswordReset"}, "vaultName": null}"""

//endregion