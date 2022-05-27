package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.common.toHexString
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.approval.*
import org.junit.Assert
import org.junit.Test

class SignableDataTest {

    @Test
    fun testSignersUpdateSerializedOp() {
        val request = getSignersUpdateWalletRequest(nonceAccountAddresses = listOf("123455"))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("12345")),
            email = "dont care"
        )
        Assert.assertEquals(
            "05d79ee6b8ae98d572459d5d6572f088a8f6b1f40655eee8c981056b205e41a37500010156b088482c6882a3def445509a410c837a27476140df0c0da4be446071000e",
            approvalRequest.opHashData().toHexString(),
        )
    }

    @Test
    fun testSignersUpdateApprovalDisposition() {
        val walletApproval = getWalletApprovalRequest(
            solanaApprovalRequestType = getSignersUpdateRequest(
                listOf("BzZpoiceSXQTtrrZUMU67s6pCJzqCDJAVvgJCRw64fJV")
            )
        )

        val approvalDispositionRequest =
            ApprovalDispositionRequest(
                approvalDisposition = ApprovalDisposition.APPROVE,
                requestId = walletApproval.id!!,
                requestType = walletApproval.getSolanaApprovalRequestType(),
                nonces = listOf(Nonce("HPaFoRv9A6T14AhGu5nJWMWTb6YuJYCNZEGnteXe728v")),
                email = "dont care"
            )

        val signableData = approvalDispositionRequest.retrieveSignableData(
            approverPublicKey = "CDrdR8xX8t83eXxB2ESuHp9AxkiJkUuKnD98zyDfMtrG"
        )

        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34a6bba3eafd49e6bf5e8facf0faeea7cf500c019cd18cfa625f764213df7b8bd5a3541700f919ae296291c89fcff67de5d3cc0d941dfd342c85e641f6cea2cb56067de40aba79d99d4939c2d114f77607a1b4bb284b5ccf6c5b8bfe7df8307bd506a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09bf3835ed0ddfb443583764c93f133c341bdcde7a0c5cd2a40348b67c20722edaf02060302040004040000000703030105220901172d281d591babce5353660adac4a2d3deecd7bb68c92be44fc9643700880a0d",
            signableData.toHexString()
        )
    }


    //Issue here seems to be in data length on the compiledInstruction serializing. That uses the encodeLength from ShortvecEncoding
    @Test
    fun testSignersUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )

        val requestType: SolanaApprovalRequestType =
            getSignersUpdateRequest(nonceAccountAddresses = listOf("5Fx8Nk98DbUcNUe4izqJkcYwfaMGmKRbXi1A7fVPfzj7"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)

        val privateKey = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = "4ec605d194c0279e9b615464d8c6a723f8995e951b1d192b4123c602389af046"
        )

        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("6HeTZQvWzhX8aLpm7K213scyGExytur2qiXxqLAMKnBb")),
            email = "dont care",
            opAccountPrivateKey = privateKey,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34067de40aba79d99d4939c2d114f77607a1b4bb284b5ccf6c5b8bfe7df8307bd538e70bc45546b0d63742dee544ecc6870f66da475c800d2d793c766b03266cca3f4336251703628ce12796daa20166a1f0da8a9a972775f9f04a256482e2bede06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000c43a8066d7b0612d9abc74bf23e9bc1230258306dcb561755b8d71c5ad38d5f406a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b4e8e14d4ca1428da3032a7ca98a145dde83e2c86a7a5996eca2a42865f13f62a030703030400040400000007020001340000000000a7670000000000500300000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b080401050206230c010272808ca267038e63a906c4ce421be377743b634b97fc1726fd6de085d2bcc9b1",
            initiationRequest.retrieveSignableData(approverPublicKey = "4q8ApWsB3rSW2HPFwc1aWmGgcBMfj7tSKBbb5sBGAB6h")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountCreationApprovalDisposition() {
        val request = getWalletApprovalRequest(
            getBalanceAccountCreationRequest(
                nonceAccountAddresses = listOf("9R4VxgBXwFZr244eb8mf3hD7NdCw87pfEDbWwSV7Hvy4")
            )
        )

        val approvalRequest = ApprovalDispositionRequest(
                approvalDisposition = ApprovalDisposition.APPROVE,
                requestId = request.id!!,
                requestType = request.getSolanaApprovalRequestType(),
                nonces =  listOf(Nonce("8kNEgm8XHszsu1wfMxTJ5ggn6pTLs1ieoKqfAxjEP3mK")),
                email = "dont care"
        )

        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34a6bba3eafd49e6bf5e8facf0faeea7cf500c019cd18cfa625f764213df7b8bd57d076439fee3c5087b8680e43f511afa72542f4a94d570aec3cf2b8b4392efc7fc48f2475f5472e0addaaacfa28b931f9484fc10830fe4f0728331a1e7ca486a06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b731dcb7b2e2119f31a81d06bb59a14fde556d82fdeb76be9aa96a9b53a9ade1202060302040004040000000703030105220901a3791fd7186a9f424f24267d4cdaafb07dfb0cf54d1b2e9e533e305d03e22e40",
            approvalRequest.retrieveSignableData(approverPublicKey = "CDrdR8xX8t83eXxB2ESuHp9AxkiJkUuKnD98zyDfMtrG").toHexString()
        )
    }

    @Test
    fun testBalanceAccountCreationInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )

        val privateKey = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = "5d4596a82bc0381481cb5facc5851a5558b9472f705d410f7272a7c8efed33f3"
        )

        val requestType: SolanaApprovalRequestType =
            getBalanceAccountCreationRequest(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = privateKey,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34fc48f2475f5472e0addaaacfa28b931f9484fc10830fe4f0728331a1e7ca486a38e70bc45546b0d63742dee544ecc6870f66da475c800d2d793c766b03266cca8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000c43a8066d7b0612d9abc74bf23e9bc1230258306dcb561755b8d71c5ad38d5f406a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030703030400040400000007020001340000000000a7670000000000500300000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b080401050206700346834450499b8d2edd17b54df5b3cd21a7e40369f8e3f8f072470cac3a271a7400b94e0c79c1fb7db6ff3380f8bd8f09376fb8f87c488f98ec920164e1e3a7417101100e0000000000000100984f81da5180cf06a171042d2f5f04de68f366c53ae858b0dde1585b1539ec0e000000",
                    initiationRequest.retrieveSignableData(approverPublicKey = "4q8ApWsB3rSW2HPFwc1aWmGgcBMfj7tSKBbb5sBGAB6h")
                .toHexString()
        )
    }

    @Test
    fun testSolWithdrawalRequestApprovalDisposition() {
        val request =
            getWalletApprovalRequest(getSolWithdrawalRequest(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af3401d86d390e73db0061cc718bad82036d774f115923c2e5e6c675ca99dd41c4fd8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd77c4c8d158b8f7d6a29a29e8f938db7344b356d823531737b5405d71c995eab1a06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000899214152f11043e929bf192a199efcb1e835a247acd4a25f1b7e5c61537bc967c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a020603020400040400000007030301052209017776689e72de20ea0d0b74a954741c7f8538a915a8d036d908465d565c5ab0cc",
                    approvalRequest.retrieveSignableData(approverPublicKey = "8CpMnz9RNojAZWMyzWirH3Y7vBebkf2965SGmcwgYSY")
                .toHexString(),
        )
    }

    @Test
    fun testSolWithdrawalRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getSolWithdrawalRequest(nonceAccountAddresses = listOf("9NDFtaczqouZ9SGTfd489EfN3KvMQgrAjpuu4QEr9Kys"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("0c7c37ea5a2f70937405de74bee9bb7a5c161d161789aa8ed7c3f78be106fa70")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("Dht1NBhu5uzMknbEYNzK5XCi8cXaJ51bHM8XQTqVx7eP")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "0301080ed5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34ef4aeae3692a6c880884390c98c6845b8c30ab27796e67c7063f5a247685034066c194d74223f4faec86f9bfe1c062355769d1f415e63bc4dce76df30cc31a5a7c4c8d158b8f7d6a29a29e8f938db7344b356d823531737b5405d71c995eab1a1157eecaf1721efd1027585b06cee0d40a758851373ad1df33637ac50c3dc940000000000000000000000000000000000000000000000000000000000000000006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000aa9fadc4043a369c55a1f0b81d537ce2f4188a038505f6052b898f9f25863b2094872d1d5b8a164cfe33e3d64bf11a8b8f17a4113d4f85ff9e6bd95cb990b06b06a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f859899214152f11043e929bf192a199efcb1e835a247acd4a25f1b7e5c61537bc96bcc5260305811f01df6bf98f587eea24c46dbf26c4ad74591df9bfd0bfde69a8030503030600040400000005020001340000000000a76700000000005003000000000000899214152f11043e929bf192a199efcb1e835a247acd4a25f1b7e5c61537bc960d0d010704080209050500050a0b0c4907b7aa84f48ee4708075c4b01fd18c8cae39d6564eb2bd6564abd3ae9ce46a33e500c2eb0b00000000cd62deaa07b0058a44cc8ec7b3a5fc67156a8b6d82bb4223b58d554a624256be",
                    initiationRequest.retrieveSignableData(approverPublicKey = "7v7no3zjzuxAkmEijayVa2Xom4LPdQJwrLNDrSkv5xcZ")
                .toHexString()
        )
    }

    @Test
    fun testSplWithdrawalRequestApprovalDisposition() {
        val request =
            getWalletApprovalRequest(getSplWithdrawalRequest(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af3401d86d390e73db0061cc718bad82036d774f115923c2e5e6c675ca99dd41c4fd8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd7515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f23006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000000ec4916daf26706bf27b89ecfff6ba56155f1d4ab734f92f008b81d4176076267c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a020603020400040400000007030301052209011c0a4bfe5d78c21d08e264df641758a6c3bccda1deef997b360a7e0c56555968",
                    approvalRequest.retrieveSignableData(approverPublicKey = "8CpMnz9RNojAZWMyzWirH3Y7vBebkf2965SGmcwgYSY")
                .toHexString()
        )
    }

    @Test
    fun testSplWithdrawalRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getSplWithdrawalRequest(nonceAccountAddresses = listOf("6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("4ba2d7074c6fa66dca792b64260b0513a229fa0849b1ceaa5b9cff1285fedee7")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9kV51VcoGhA1YFkBxBhd7rG1nz7ZCVcsBpqaaGa1hgCD")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010a10d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34e34e9cdb62a817da76d5163666c93570c210e7287f7f268c77236b3002156ca08ea1bcb15aa292a9b51b7f05f19d0e27669957b9990a4aa8f9cddc1cf4c56c55515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f230f46cdc8655b9add6c8905bd1247a2ef15870d4076fb39915885d08628a9f22a493c4b438027247811fa84db55c2e6ede640264dad0876d56a8bf819f2cb17bfa06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000631f065d4a6b93340e8b8a8c3061fd6eb0b7d402fe560fc40a84e9c8ce1ac3035c66b35c237860fd27d95409d452edbd91300bbfe80850fd4841759722b5073106a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000008df1a38eac809d9806f4e9502bc085aadb4975b84dc3ba062b1ce416efd4b1c5000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f8590ec4916daf26706bf27b89ecfff6ba56155f1d4ab734f92f008b81d4176076268201354297cec572707724dac5f6c92613c5a8f04e34fe284c882de8d09a0826030b0303060004040000000b020001340000000000a767000000000050030000000000000ec4916daf26706bf27b89ecfff6ba56155f1d4ab734f92f008b81d4176076260f0d0107040802090a05000b0c0d0e4907c381ac8c68089013328ad37eda42b285abedc13fef404fc34e56528011ded600f401000000000000f7d1a2ef642101c041a4523de1dd26652402149065ae308c55e1924cb217cb48",
                    initiationRequest.retrieveSignableData(approverPublicKey = "AbmwBa52qPj5zpWQxeJJ3ZSDRDxnyZMWitrE8nd4mrmi")
                .toHexString()
        )
    }

    @Test
    fun testUSDCconversionRequestApprovalDisposition() {
        val request =
            getWalletApprovalRequest(getConversionRequest(nonceAccountAddresses = listOf("6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("9kV51VcoGhA1YFkBxBhd7rG1nz7ZCVcsBpqaaGa1hgCD")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010307d2c2e3ac53223ce6b5a6e04fe0f98071cf10a62646b6c1c100f9829afcced04e5335831f99da167bf80d87be098dbaafc9309035be4aedd53460c3571c05b6a0515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f230000000000000000000000000000000000000000000000000000000000000000006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000a78bdd1907176367f56f7fab4bee90dabaa7372794fb0403f75f2a96580998478201354297cec572707724dac5f6c92613c5a8f04e34fe284c882de8d09a082602030302040004040000000603030105220901a4403ca23bc4f76030f2b2159bb991b66a696acdc81e72cedfa7d028be999b1e",
                    approvalRequest.retrieveSignableData(approverPublicKey = "6bpAbKqWrtXBtdnWqA8YSybGTeyD91u9MNzQuP7641MH")
                .toHexString()
        )
    }

    @Test
    fun testUSDCConversionRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getConversionRequest(nonceAccountAddresses = listOf("6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("5f27be66e3eb697a4274f9359c87f9069762a5e2cb7a63a622e923bd6119b963")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9kV51VcoGhA1YFkBxBhd7rG1nz7ZCVcsBpqaaGa1hgCD")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010a10d2c2e3ac53223ce6b5a6e04fe0f98071cf10a62646b6c1c100f9829afcced04e17ce130f4d1b123ff7f5f840aee4e9fa5665106de0cf2d1245c2b60f6ade6e245335831f99da167bf80d87be098dbaafc9309035be4aedd53460c3571c05b6a0515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f230d1e5bffc491a1b7890805d162a2cf8f0a2facae1df8579eddfed575e44f958108e829493f87ba7dc9497154a2cf8e656ee9979277f9ac88b5570775e7cb447d106a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea94000001bbc7e99fc43d0c442a698780fa1c7e4bcfbe5f100df263390ef0ab695e1b85aa1a993efade361c637af59e4d387db1aec381df43083f38e789f4bd57280889906a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000008ac94d970e27bc29711d382b1d5fac3fe82f590485b065e57fcc6e83424110cd000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f859a78bdd1907176367f56f7fab4bee90dabaa7372794fb0403f75f2a96580998478201354297cec572707724dac5f6c92613c5a8f04e34fe284c882de8d09a0826030b0303060004040000000b020001340000000000a76700000000005003000000000000a78bdd1907176367f56f7fab4bee90dabaa7372794fb0403f75f2a96580998470f0d0107040802090a05000b0c0d0e4907138543b25e89429dae0ec18a0fa198dc5006898f91b3b99d80a58d65bcdff9d00065cd1d00000000455c311d68d6d25a36bb09d58c4f62e6e637031c29b7fd3bd205b658500739cf",
                    initiationRequest.retrieveSignableData(approverPublicKey = "6bpAbKqWrtXBtdnWqA8YSybGTeyD91u9MNzQuP7641MH")
                .toHexString()
        )
    }

    @Test
    fun testWrapConversionRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getWrapConversionRequest(nonceAccountAddresses = listOf("6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("46b397c81d81f9c745bb61baf28337888907696c5e653a08a98b5ecbcc1c82c8")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9kV51VcoGhA1YFkBxBhd7rG1nz7ZCVcsBpqaaGa1hgCD")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "0301090fd5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af342a6b6e29ec48d15d528b864b1d58f441b263ed5f24db504928f6090efc8cb41d0b5e9dd920eed912053e5333449d7a92d82d80ebea0f12829aa36e93559b000e515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f2309b0ed81b27ca1d63c6a994c30755027b44c213a3a5948040c8d4e1703ed539fb5abb3bbf8838f5129b8032b1f4ffac9f4043ef034e9d9dab4d32f25055c7496f06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000ca16efb68a8429558cd821a7c0942d5960f0b2c5b7f3a54caf6920e4555ac75c069b8857feab8184fb687f634618c035dac439dc1aeb3b5598a0f0000000000106a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f859bad1dda43bb63a1a1841895eae8fc1398f8e943ccf637d87c6f25aa82b25067d8201354297cec572707724dac5f6c92613c5a8f04e34fe284c882de8d09a0826030a0303060004040000000a020001340000000000a76700000000005003000000000000bad1dda43bb63a1a1841895eae8fc1398f8e943ccf637d87c6f25aa82b25067d0e0b010704050802090a0b0c0d2a0ac344bc80949c53bf0f257f570c1beea68dbc9563a595d46d5c9a7367bd12a5cc0065cd1d0000000000",
                    initiationRequest.retrieveSignableData(approverPublicKey = "mPAWwEkDygfLX7A8Tzox6wyZRBrEudpRN2frKRXtLoX")
                .toHexString()
        )
    }

    @Test
    fun testUnwrapConversionRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getUnwrapConversionRequest(nonceAccountAddresses = listOf("6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("4cdbc626f9cb68219d52d49d80041ab0b3b130d1880323a763b3eed8d4f8ff0f")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9kV51VcoGhA1YFkBxBhd7rG1nz7ZCVcsBpqaaGa1hgCD")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "0301090fd5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34e9afcff207f5614ebfa3a3522dfdaac0bc90d89768e6ee6b0a700d41dada06180b5e9dd920eed912053e5333449d7a92d82d80ebea0f12829aa36e93559b000e515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f2309b0ed81b27ca1d63c6a994c30755027b44c213a3a5948040c8d4e1703ed539fb5abb3bbf8838f5129b8032b1f4ffac9f4043ef034e9d9dab4d32f25055c7496f06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000ca16efb68a8429558cd821a7c0942d5960f0b2c5b7f3a54caf6920e4555ac75c069b8857feab8184fb687f634618c035dac439dc1aeb3b5598a0f0000000000106a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f859bad1dda43bb63a1a1841895eae8fc1398f8e943ccf637d87c6f25aa82b25067d8201354297cec572707724dac5f6c92613c5a8f04e34fe284c882de8d09a0826030a0303060004040000000a020001340000000000a76700000000005003000000000000bad1dda43bb63a1a1841895eae8fc1398f8e943ccf637d87c6f25aa82b25067d0e0b010704050802090a0b0c0d2a0ac344bc80949c53bf0f257f570c1beea68dbc9563a595d46d5c9a7367bd12a5cc00a3e1110000000001",
            initiationRequest.retrieveSignableData(approverPublicKey = "mPAWwEkDygfLX7A8Tzox6wyZRBrEudpRN2frKRXtLoX")
                .toHexString(),
        )
    }

    @Test
    fun testDAppTransactionRequestInitiationRequest() {
        val opAccountPk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("bc020c43289b29e89674d4d2ef381508583b7894f5e7957e91ec0bf90e58476b")
        )
        val dataAccountPk = generateEphemeralPrivateKeyFromText(keyValueAsHex = ("170c7dbfca9b52c9bb5572d32053e56ca422fe4e409fa6ab30784e3dfd9f9493")
        )

        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = MultiSigAccountCreationInfo(
                accountSize = 2696,
                minBalanceForRentExemption = 19655040
            )
        )

        val nonceAccountAddresses = listOf(
            "CeQNynfs9Mx1MGTeGQaDZDZnCiUWTkUsUyYf4qT51Cek",
            "HSwjDt3MYHutJcXtryaQEG3SBfRktqkuoU8cVyWoRE7P"
        )
        val nonces = listOf(
            Nonce("HqdbyB576ggyavQPaodKiS8XNPHBoo95rb75Le7XzXrr"),
            Nonce("6PuZZuAFFYutbMY1JuYFHQUnZMXQeBe9ZrzoCaL4YGD5")
        )

        val requestType = getDAppTransactionRequest(nonceAccountAddresses = nonceAccountAddresses)
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = nonces,
            email = "dont care",
            opAccountPrivateKey = opAccountPk,
            dataAccountPrivateKey = dataAccountPk
        )

        Assert.assertEquals(
            "0401050ad5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34910d0175251385b1ee8fd8a19a360563f04ebdf43e6a7d0040f85ed2e6ec00e29c361e34407dab66e0d07e010c882241a9e967912a087b517092cb177093de69f94ef38875324ed73252ad52b51449f4615b2f1da3645635582db54dafe4a56cad057b14f59401a098a66e40e1bed86e00f949ba796e9a5af604d1e84cd96f1906a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000051a5c79873c30d67e853c870935f0b76c02dfbc48624afc1188ede15558fb77a06a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000baec1d8d88c2a1f9c53d90c084f0288926771f41a2a363e9493b3d35e9b7fdfcfa3041b6b74ec346f1134c06b64afd166282100d3fc894c8be32c7ce6bcc90cb040803040500040400000008020001340000000000a76700000000005003000000000000baec1d8d88c2a1f9c53d90c084f0288926771f41a2a363e9493b3d35e9b7fdfc08020002340000000080e92b0100000000880a000000000000baec1d8d88c2a1f9c53d90c084f0288926771f41a2a363e9493b3d35e9b7fdfc0905010206030762105858d7574baea93cb0dd0e2c9a5c9c6a18d4b58fd0079d0fbf02aee8e7ca93814a2401cbeb9bb1c99935b6775fa040b4f0e4c90b5e473c28d871e8f240b5b5432e90c13b0d471b592c2984568133a41676460f72c885db57ad26bd5b628f938201",
            initiationRequest.retrieveSignableData(approverPublicKey = "HnCLpPZrMdXdogoesmx6bX3z3tPo8mvvaRXXkaonzZtf")
                .toHexString()
        )

        val supplyInstructions = initiationRequest.supplyInstructions
        Assert.assertEquals(supplyInstructions.size, 1)
        Assert.assertEquals(supplyInstructions[0].nonce, nonces[1])
        Assert.assertEquals(supplyInstructions[0].nonceAccountAddress, nonceAccountAddresses[1])
        Assert.assertEquals(
            "02010308d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34f94ef38875324ed73252ad52b51449f4615b2f1da3645635582db54dafe4a56cf46064647351baf7c5594ff5df2c40351745fc44521994a2d22c76243775c506910d0175251385b1ee8fd8a19a360563f04ebdf43e6a7d0040f85ed2e6ec00e29c361e34407dab66e0d07e010c882241a9e967912a087b517092cb177093de6906a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea94000000000000000000000000000000000000000000000000000000000000000000000baec1d8d88c2a1f9c53d90c084f0288926771f41a2a363e9493b3d35e9b7fdfc5028a4d782430b75cf8637ccc7df21fe6d186a69ddb048cc9dc925a78e1472800206030205000404000000070303040193021c0001008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f8590700035916cb79b8ad2bae04f10515239d1c14887f73176f3e999782551840df5e982e0159fba9d95655503acdbd5e4817229ecd4fe12aca67536b5ddc9d4782ca22d11a035916cb79b8ad2bae04f10515239d1c14887f73176f3e999782551840df5e982e00c726a968258c654f7622595c108fc3333d676e45b15b9d541c1034d2a68031440000000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a90006a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000000400010203ac",
            supplyInstructions[0].retrieveSignableData(approverPublicKey = "HnCLpPZrMdXdogoesmx6bX3z3tPo8mvvaRXXkaonzZtf").toHexString()
        )
    }

    @Test
    fun testAddDAppBookEntryApprovalRequest() {
        val request =
            getWalletApprovalRequest(getAddDAppBookEntry(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34cdd65bdd5302de9e0457368e03c37dcd1e9029c3ab0facdcfc5889a81d0cf6138e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd7f9437a782883b62d38738b3da7fada188510e6d57d1e09bdbde19cf7ed16e60206a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a020603020400040400000007030301052209018d6b5e5ef60fb6e4f56efd65c050a066d2195a8c3ec205bfd21116e175126792",
            approvalRequest.retrieveSignableData(approverPublicKey = "ErWAApTUwunKAobwFrVe2fTwtqdsQecQqWKSQJzysg4z")
                .toHexString(),
        )
    }

    @Test
    fun testAddDAppBookEntryInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getAddDAppBookEntry(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("72ff8f7fb4a441c93d4003e2bf67dd367e3293311c4f9433c422a2fbf305c477")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34f9437a782883b62d38738b3da7fada188510e6d57d1e09bdbde19cf7ed16e602982acb779028b0afdd5da5a26d78e1b82804ae449ce2fd2767d15f4325a7f1118e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000064fd89d243e47f6bd6ea9c7462d7ca1d504c02bf67d2b6738f892897ecaeab206a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030703030400040400000007020001340000000000a767000000000050030000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c08040105020644140100e4523ff383e6bb5f73d3745e3554f53a56c61ba17c7bc49e481a9d01a96fdbd6a9037bac86a669c3470c8da04dcec8f3a3ec671cd157264078954f38c387efb000",
            initiationRequest.retrieveSignableData(approverPublicKey = "BEzpSizrNZpCeLWTk23nozu4T4wEzxoDJGoUUYBBhVbE")
                .toHexString()
        )
    }

    @Test
    fun testRemoveDAppBookEntryApprovalRequest() {
        val request =
            getWalletApprovalRequest(getRemoveDAppBookEntry(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34cdd65bdd5302de9e0457368e03c37dcd1e9029c3ab0facdcfc5889a81d0cf6138e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd779dac0b298597dcbf810eb17709c9a75e1f4e569efe90f323c91c4ef084882c206a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a02060302040004040000000703030105220901b5b1693386fac4e14cbba717c482605fedd188df4188ef1f9f0046c94ab5b729",
            approvalRequest.retrieveSignableData(approverPublicKey = "ErWAApTUwunKAobwFrVe2fTwtqdsQecQqWKSQJzysg4z")
                .toHexString()
        )
    }

    @Test
    fun testRemoveDAppBookEntryInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getRemoveDAppBookEntry(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("1d3462075eae5a46257981c00c20982dd27a88b70a88ff95455dad6bc88859aa")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af3479dac0b298597dcbf810eb17709c9a75e1f4e569efe90f323c91c4ef084882c2982acb779028b0afdd5da5a26d78e1b82804ae449ce2fd2767d15f4325a7f1118e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000064fd89d243e47f6bd6ea9c7462d7ca1d504c02bf67d2b6738f892897ecaeab206a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030703030400040400000007020001340000000000a767000000000050030000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c0804010502064414000100e4523ff383e6bb5f73d3745e3554f53a56c61ba17c7bc49e481a9d01a96fdbd6a9037bac86a669c3470c8da04dcec8f3a3ec671cd157264078954f38c387efb0",
            initiationRequest.retrieveSignableData(approverPublicKey = "BEzpSizrNZpCeLWTk23nozu4T4wEzxoDJGoUUYBBhVbE")
                .toHexString()
        )
    }

    @Test
    fun testAddAddressBookEntryInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getAddAddressBookEntry(nonceAccountAddresses = listOf("AFpfUonk56y9aZdjnbs1N2VUsUrtPQfVgFncAMyTReeH"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("d593c3bc464cf65719a8881ef79c66d8a4684870ccdbf314f012ff8ed879295a")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("F2MWViB8wyK77MHVUbzyWDABgXu8SFBcM3iEREUdevAd")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af3414f77b30cf799b059605b79ab2be10641f9c65a9bc4f226e8c8b290557ed0c7e5ab9e373e1af4f5248eaefd355eb8e6d5853f11bdc95fcf3fdd0a85add6eac98898534f677e9f6b843ebcf3e53c93f9382e9e02287d16066540638f00fa2718606a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000dc4dc6b8f1d08cfa8efcce33cd97d55a46422050a94f87ca154768f0676220a506a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000008629739ae31d052f08b5534253dac669eb963e39b38fdfa02dc4b52ce55db0ced05c6308b8f9874498edf6cfda59d7a1ee89e5661d4196385d502db9a16c5cca030703030400040400000007020001340000000000a767000000000050030000000000008629739ae31d052f08b5534253dac669eb963e39b38fdfa02dc4b52ce55db0ce0804010502064516010150a93021a0aaaba0128166790f8450472dccded2f9d9809f4e89ccad52e80b7e3c788df5ccf194a18ab2990c7e57f4b837320f003b6d726d14c04bbe6dc371430000",
            initiationRequest.retrieveSignableData(approverPublicKey = "77A6RbdEjz8JQFFfXBepk7ssX5QUxunQ3TTJdayjkqw5")
                .toHexString(),
        )
    }

    @Test
    fun testAddAddressBookEntryApprovalRequest() {
        val request =
            getWalletApprovalRequest(getAddAddressBookEntry(nonceAccountAddresses = listOf("Hd4srpDtCzUsySDVZVeaBNJA7txQrJfvaZjhVpWePnK9")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("F2MWViB8wyK77MHVUbzyWDABgXu8SFBcM3iEREUdevAd")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af347554e641b3f3724c0660945d56ca6c51979b4f8f8dfdb8969bb4f1633a8f88d4f6f848968b4992558e8a75c9f6dcffe090932a702d806fece59c015babc5391014f77b30cf799b059605b79ab2be10641f9c65a9bc4f226e8c8b290557ed0c7e06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000008629739ae31d052f08b5534253dac669eb963e39b38fdfa02dc4b52ce55db0ced05c6308b8f9874498edf6cfda59d7a1ee89e5661d4196385d502db9a16c5cca020603020400040400000007030301052209015a481b8a2b4d64f4ee2fd22776f0b5c32a5a6e9ef384dbe460fe03e1469d9bdf",
            approvalRequest.retrieveSignableData(approverPublicKey = "8u1nbZ2Zv42ouiCVcJPqKQa7VZRoFSQhH6q7Ghkiurkb")
                .toHexString()
        )
    }

    @Test
    fun testWalletConfigPolicyUpdateApprovalRequest() {
        val request =
            getWalletApprovalRequest(getWalletConfigPolicyUpdate(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af342ae5404ca4d115addf760a932a2564636c071f3d93077c7722926026963d760e8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd7d17a6a48d07bbbf8d76e02379e0758f4580f3cb34a56980929e72e9b0d58e97206a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000046b0ea2883f065b5469948ac86ffcda8d7fb98f891b0c6f805c4cccb9dabcacf7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a020603020400040400000007030301052209019a8396d2fa315bafcfe5ca0d78946f4bf31297feb3036fd82998f28c0af3332c",
            approvalRequest.retrieveSignableData(approverPublicKey = "3tSshpPL1WyNR7qDfxPffinndQmgfvTGoZc3PgL65Z9o")
                .toHexString()
        )
    }

    @Test
    fun testWalletConfigPolicyUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getWalletConfigPolicyUpdate(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("9fa146dbd7f5bdaef9b0b4c99980a0acb0bfc4b874d02c86694f1193acfbb87f")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010409d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34d17a6a48d07bbbf8d76e02379e0758f4580f3cb34a56980929e72e9b0d58e9724a3e400b6f36e0b517ed08ced47959f691ac7badf37f3894b745a78ae4c4a01a8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd70a4b19fe3af610a9e087ccad29c92dbcbc2a3a6671794cd819a6004877bb0ea006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000046b0ea2883f065b5469948ac86ffcda8d7fb98f891b0c6f805c4cccb9dabcacf7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030703030500040400000007020001340000000000a7670000000000500300000000000046b0ea2883f065b5469948ac86ffcda8d7fb98f891b0c6f805c4cccb9dabcacf0804010402062e0e035046000000000000030001024041109cb8f8611bd2813af557df74e80cb9da3a2599894d5d990fc13536d917",
            initiationRequest.retrieveSignableData(approverPublicKey = "5zpDzYujD8xnZ5B9m93qHCGMSeLDb7eAKCo4kWha7knV")
                .toHexString(),
        )
    }

    @Test
    fun testBalanceAccountSettingsUpdateApprovalRequest() {
        val request =
            getWalletApprovalRequest(getBalanceAccountSettingsUpdate(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34e6e137f1b3e582e55db0f594a6cb6f05d5a08fc71d7413042921bf24f72e73eb8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd7e40128881204af69745129ee3357a788ce003ce6171a9e92a011afc775b8ce6006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000064424795ac2edb4b21b281bd120d0ababb12d4ae690773f41f5f61027a7add9f7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a020603020400040400000007030301052209011df7ee9884d25dddac4fa0b133456174394207dba45a16fbd963f07c8c5447f4",
            approvalRequest.retrieveSignableData(approverPublicKey = "GYFxPGjuBXYKg1S91zgpVZCLP4guLGRho27bTAkAzjVL")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountSettingsUpdateUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getBalanceAccountSettingsUpdate(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("43ae13e0827d8034dbb880c3210ab7f7b7c49c5d2b3120933c184c8942110ed0")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34e40128881204af69745129ee3357a788ce003ce6171a9e92a011afc775b8ce609e94ede101ab5be0734b6500e0fc10b51ca23e89e67391657197fd7b2529c13e8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea94000003468bd8cddd071cd3bb0a3c50c4b5cab7dfe4ae3328081889ebabd48d8b7c9c006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000064424795ac2edb4b21b281bd120d0ababb12d4ae690773f41f5f61027a7add9f7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030703030400040400000007020001340000000000a7670000000000500300000000000064424795ac2edb4b21b281bd120d0ababb12d4ae690773f41f5f61027a7add9f08040105020625123c69f1851b7318b7f14f04992a80df6054b8bc4f325f24bce0d378d770e870c401010000",
            initiationRequest.retrieveSignableData(approverPublicKey = "Bg38YKHxGQrVRMB254yCKgVjtRapi68H4SD1RCiwWo7b")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountPolicyUpdateApprovalRequest() {
        val request =
            getWalletApprovalRequest(getBalanceAccountPolicyUpdate(nonceAccountAddresses = listOf("BzZpoiceSXQTtrrZUMU67s6pCJzqCDJAVvgJCRw64fJV")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("BeAVku8zsY9b1SzKU1UPkyqr6feVtiK7FS5bGHjfFArp")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34a6bba3eafd49e6bf5e8facf0faeea7cf500c019cd18cfa625f764213df7b8bd5a3541700f919ae296291c89fcff67de5d3cc0d941dfd342c85e641f6cea2cb56bb2b351f441f46df2039f49e8cd3f01079a908cad599a84079cb8189b218f57806a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b9e1a189ce3273e79eef0d169e15ced5902ca2a4a680fc7f710ef4513ef02ebdd020603020400040400000007030301052209013805e2569f36e15a83d558a3092cfd97e9932b34a06ec76bc04fa649f7c23d40",
            approvalRequest.retrieveSignableData(approverPublicKey = "CDrdR8xX8t83eXxB2ESuHp9AxkiJkUuKnD98zyDfMtrG")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountPolicyUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getBalanceAccountPolicyUpdate(nonceAccountAddresses = listOf("5Fx8Nk98DbUcNUe4izqJkcYwfaMGmKRbXi1A7fVPfzj7"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("370349daceb62cb1ad6f37fbaba12dc72e36367c57b2ee976527609cd8d3f63e")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("DvKqKEBaJ71C5Hw8Yn45NvsYhpXfAYHybBbUa17nHcUm")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010409d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34bb2b351f441f46df2039f49e8cd3f01079a908cad599a84079cb8189b218f57838e70bc45546b0d63742dee544ecc6870f66da475c800d2d793c766b03266cca3f4336251703628ce12796daa20166a1f0da8a9a972775f9f04a256482e2bedec43a8066d7b0612d9abc74bf23e9bc1230258306dcb561755b8d71c5ad38d5f406a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09bbff5520f20afb88a51a9a0630fb5bc2738f26b68af438c6a1750a68a4c2fc3c6030703030500040400000007020001340000000000a7670000000000500300000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b0804010402064d1a46834450499b8d2edd17b54df5b3cd21a7e40369f8e3f8f072470cac3a271a7402100e0000000000000200011618435becfcd77198205d44019be2254d324294b97ef819e0c77d3af8b0e446",
            initiationRequest.retrieveSignableData(approverPublicKey = "4q8ApWsB3rSW2HPFwc1aWmGgcBMfj7tSKBbb5sBGAB6h")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountAddressWhitelistUpdateApprovalRequest() {
        val request = getWalletApprovalRequest(
            getBalanceAccountAddressWhitelistUpdate(
                nonceAccountAddresses = listOf("481dDMZGAiATXnLkBw1mEMdsJSwWWg3F2zHEsciaXZ98")
            )
        )
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("5XUBQaLXvXGvFArDtYpj3FW1TbNtZ3hP8fkrbzzY3VZJ")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34dd896d086f2c63e124ed47d94ee7b4932644e826e8280cb345893312aa199bc92e5ed518e5ea088f46ae95e2cb452fc7be22322d0a63e0ef7a820e8aa2593d7759427bbc05d796626ca3c12d0b3553f51e4a0a0582be08acfed19d4d8fa6ca3106a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000007bd238678c7de6666b1fa72f0423be16b875681575a02d9bfb142bed5c64ea35433ce76291f054c3712f68b3fc11a56a6a3f2e5447b3eb7d387ddc739ce41961020603020400040400000007030301052209018dea73bf1bb3c32d97ec901ae9e136e17dfc042025d048361cb82981ddbc7e5b",
            approvalRequest.retrieveSignableData(approverPublicKey = "FuneCbHNcAmaG9gEyisDYiZFLiYTGsuVsFMArXJDR3np")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountAddressWhitelistUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getBalanceAccountAddressWhitelistUpdate(nonceAccountAddresses = listOf("9LGMMPep1WKdiNNwicDvx8JiwgtBKPWhidaSv3rVUNz"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("a9989f27d789b3c2266db5dbd1420e2831cacbb161d6e95bd48323911560fd11")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("Bb2XveQNyXVBJvJUPnsbhRtYqePUi8xbvBaJxk96BKDG")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af3459427bbc05d796626ca3c12d0b3553f51e4a0a0582be08acfed19d4d8fa6ca312f1c6ccaca0b0a0d12b938444f2ec6a9ec82b810394c64237a4651c5a41d4cd902226dd9d2e98d75fab1c2b62b8b007bab361b66ddffaa2362d30e8d8b915e3706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000002827f15215947e7af780bb61613d832c508101f217f8c259828ffc4680fbcde06a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000007bd238678c7de6666b1fa72f0423be16b875681575a02d9bfb142bed5c64ea359d4c59b0c3139035ef4ad8c1c52ed58a7daf2db23646c0704575e009c35fac6f030703030400040400000007020001340000000000a767000000000050030000000000007bd238678c7de6666b1fa72f0423be16b875681575a02d9bfb142bed5c64ea3508040105020644215560c327edbddd0faa5fe1ed8ff2e8da684374eb45e2cdd67cba4f3bb258fbd50201021b642a192de6a4165d92cf0e3d0c00e6ec86f02d6c71c537a879749da2200b91",
            initiationRequest.retrieveSignableData(approverPublicKey = "4AuJTW9fTnbPUq3LDAehK1CHsENF3x8X9vKnDwCUbTpk")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountNameUpdateApprovalRequest() {
        val request =
            getWalletApprovalRequest(getBalanceAccountNameUpdate(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34e6e137f1b3e582e55db0f594a6cb6f05d5a08fc71d7413042921bf24f72e73eb8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd75c5c48251d37fc912ce1ac482a5b79e5f904d3202d47287f39edf2e1b6bb241006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000064424795ac2edb4b21b281bd120d0ababb12d4ae690773f41f5f61027a7add9f7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a020603020400040400000007030301052209010c2b34abcc84e4ae92d3120231ba6a13303976d34e1e8951565dbe2700ca6538",
            approvalRequest.retrieveSignableData(approverPublicKey = "GYFxPGjuBXYKg1S91zgpVZCLP4guLGRho27bTAkAzjVL")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountNameUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getBalanceAccountNameUpdate(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("7707e53ddb688826e19d5d1d651450222c3d6cf73680fd331430278bba237328")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af345c5c48251d37fc912ce1ac482a5b79e5f904d3202d47287f39edf2e1b6bb24109e94ede101ab5be0734b6500e0fc10b51ca23e89e67391657197fd7b2529c13e8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea94000003468bd8cddd071cd3bb0a3c50c4b5cab7dfe4ae3328081889ebabd48d8b7c9c006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000064424795ac2edb4b21b281bd120d0ababb12d4ae690773f41f5f61027a7add9f7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030703030400040400000007020001340000000000a7670000000000500300000000000064424795ac2edb4b21b281bd120d0ababb12d4ae690773f41f5f61027a7add9f08040105020641183c69f1851b7318b7f14f04992a80df6054b8bc4f325f24bce0d378d770e870c44e637072f628e09a14c28a2559381705b1674b55541eb62eb6db926704666ac5",
            initiationRequest.retrieveSignableData(approverPublicKey = "Bg38YKHxGQrVRMB254yCKgVjtRapi68H4SD1RCiwWo7b")
                .toHexString()
        )
    }

    @Test
    fun testSPLTokenAccountCreationInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            dataAccountCreationInfo = null
        )
        val requestType =
            getSPLTokenAccountCreation(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("affd2d3a1283a92c72347c5075126b7868a9bc17b32dbb06eaf90ec8fdb51f3e")
        )
        val initiationRequest = InitiationRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = pk,
            dataAccountPrivateKey = null
        )

        Assert.assertEquals(
            "0301070bd5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af344b6a3d93450d66eb4dc907e6f3c8f478feb3c8afd69ea70a6f73eb771d87cb14ab2d202d4ab70a619c12c35cd765878d7711743f57c555940b27087173491fd68e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000c43a8066d7b0612d9abc74bf23e9bc1230258306dcb561755b8d71c5ad38d5f4069b8857feab8184fb687f634618c035dac439dc1aeb3b5598a0f0000000000106a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000008269b01bf858c755348eccb7fd606a006e63d0cd6c0eb0b1a88694fbd26ffae0000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030903030400040400000009020001340000000000a7670000000000500300000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b0a06010502060708621d794b77f810f9c71db95d8fd3a9adc5805b501983c8e0b50ee675c3dc13eca3f601794b77f810f9c71db95d8fd3a9adc5805b501983c8e0b50ee675c3dc13eca3f6069b8857feab8184fb687f634618c035dac439dc1aeb3b5598a0f00000000001",
            initiationRequest.retrieveSignableData(approverPublicKey = "CXCdHsyMVVKEQbRorowkBBnRtmC7QSAmg4QFqQJAMt85")
                .toHexString()
        )
    }

    @Test
    fun testSPLTokenAccountCreationApprovalRequest() {
        val request =
            getWalletApprovalRequest(getSPLTokenAccountCreation(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care"
        )
        Assert.assertEquals(
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34e6e137f1b3e582e55db0f594a6cb6f05d5a08fc71d7413042921bf24f72e73eb8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd7067de40aba79d99d4939c2d114f77607a1b4bb284b5ccf6c5b8bfe7df8307bd506a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a02060302040004040000000703030105220901609e5e67b61a6817c2071005eff4f663226b78e78fd533c6f8d3ce80c564bd5c",
            approvalRequest.retrieveSignableData(approverPublicKey = "GYFxPGjuBXYKg1S91zgpVZCLP4guLGRho27bTAkAzjVL")
                .toHexString()
        )
    }

}