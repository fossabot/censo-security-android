package com.strikeprotocols.mobile

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.strikeprotocols.mobile.common.toHexString
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.approval.*
import junit.framework.Assert.*
import org.junit.Assert
import org.junit.Test

class SignableDataTest {

    @Test
    fun testSignersUpdateSerializedOp() {
        val request = getSignersUpdateWalletRequest(nonceAccountAddresses = listOf("6XQy8HrrMmsfmajFpWo5tWhDUiESg8obA4NGWJwjNgpR"))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("EHEtZUo7aAh5VqFvo5PUsfFm3oquJfK3hb8fEJgC4nwM")),
            email = "dont care"
        )
        Assert.assertEquals(
            "050b421acb7dfcb45b3fa41c345677eeee6a360e933ebfff2a321e6d6bf23f703a69ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c715000000000000000000000000000000000000000000000000000000000000000000000000000000002f2bb19a6e87510629e1b70680b474648391dc5d898c1f8431eb10c6fbb4cebb00016d50ef7d464988ec20f2782dccf74d4416779bbc5073d0168b12b6f720140656",
            approvalRequest.opHashData().toHexString(),
        )
    }

    @Test
    fun testSignersUpdateApprovalDisposition() {
        val walletApproval = getWalletApprovalRequest(
            solanaApprovalRequestType = getSignersUpdateRequestForApproval(
                listOf("Deuspj2g5crN81b6GocKANAhB3Y5D6XseXjiT1bery7Z")
            )
        )

        val approvalDispositionRequest =
            ApprovalDispositionRequest(
                approvalDisposition = ApprovalDisposition.APPROVE,
                requestId = walletApproval.id!!,
                requestType = walletApproval.getSolanaApprovalRequestType(),
                nonces = listOf(Nonce("FURnHTusWpktnZfRCEWi3iCEY7D8FJTizNX2z9A4byps")),
                email = "dont care"
            )

        val signableData = approvalDispositionRequest.retrieveSignableData(
            approverPublicKey = "AztEBjLYWtNgBN3Gzqz5a2g4npNmtZQ29eidUzrKZwhX"
        )

        Assert.assertEquals(
            "0201040869ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c715948d361ce4f9bafead45f21c7aec862985622a4c48cee98a4c6293ff70901446bc02853a9410a410b2d6ef666f054e84f35cc47885bf7e9c618eb581938f23941a8dd233c196130bce1a6d6bd2a122cea83c741ae3221705fb0d2074047956b906a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000000753be561ec13b0065ff6cdef40ac6ae7931c5b82501159ebc2751f3d72720f1d70a51898096a604916bc64710e1e55a46f6d82afd7b54c11d2982282c6f8f5802060302040004040000000703030105220901de59db38ce9ead210c8446f5aae7d31936bbd30c91e3b0fbca116ef80f8db7b6",
            signableData.toHexString()
        )
    }


    @Test
    fun testSignersUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(3649, 26287920),
            initiatorIsApprover = true
        )

        val requestType: SolanaApprovalRequestType =
            getSignersUpdateRequest(nonceAccountAddresses = listOf("BEhPWZKJoFDHaaJ7qTqZ6cfkR1YKDssHwttBYnp9kZir"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)

        val privateKey = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = "e754f36891ada48a4cfc71ee4673aa2c77ceaa47da3021e4b64cf56bc91df5d8"
        )

        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("4oEYPeBwJBWzrbQ1c7jeeUGKWc1afDW7wFuhesE84GLr")),
            email = "dont care",
            opAccountPrivateKey = privateKey
        )

        Assert.assertEquals(
            "0301050969ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c715e9cd9aec3dbf86f831f3a958fe0cacf766c96fe73a870935ae4a1dbc32f9690e0b421acb7dfcb45b3fa41c345677eeee6a360e933ebfff2a321e6d6bf23f703a981716351da1f0ee427fdbcb78f5c2d68ea47b7799000aaf333de3786d72c65306a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea94000002f2bb19a6e87510629e1b70680b474648391dc5d898c1f8431eb10c6fbb4cebb06a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000003fdc67ec334cbd120c7c9a2826105ef499faac1ec7ca09e6f91ba1cdc31dc236386b16ceef633922978f5ab1386e428471a2a8f74e2619f84101c2aec41821a30307030304000404000000070200013400000000301f910100000000410e0000000000003fdc67ec334cbd120c7c9a2826105ef499faac1ec7ca09e6f91ba1cdc31dc236080501050206006c0c000000000000000000000000000000000000000000000000000000000000000000000000000000000000016d50ef7d464988ec20f2782dccf74d4416779bbc5073d0168b12b6f72014065688c02d478b22653c2576e020b3482da4209fbdd77d35ee43cb522173e4c1b376",
            initiationRequest.retrieveSignableData(approverPublicKey = "kwwyzySTUJHWBF64u15iwv8bMvcHTNmZzfEh9iDeC7j")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountCreationApprovalDisposition() {
        val request = getWalletApprovalRequest(
            getBalanceAccountCreationRequest(
                nonceAccountAddresses = listOf("Hy4Ztych4X12wWieCaFbSEbwZRxmjRTMgbm7RDybYTpD")
            )
        )

        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces =  listOf(Nonce("QRKqHqP5SNEngXrcK2QeAR2nqx9AmwbHrmYF49ZbkEK")),
            email = "dont care"
        )

        Assert.assertEquals(
            "0201040869ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c7155b867b0ff902f8bd3503e7a6c3ba5a94b90bd26858f6de78aa0913af0d8be7c6fc178caf216681e7b5d744beeca4c54681c9df12ffbd88f3bb1629ec0d56475211fa69aa5bb02ddd4a80e4673e541767116f2034406d2f917725045d215bdd3f06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d05ffdcd59d8f12aab0f8644eb0a57db2187baf6cc1e0115a977082f2ecd54120020603020400040400000007030301052209010aba765b69604b25cb6cca94eb09179fb0c181d1ead2e25d4430fd8c52e89419",
            approvalRequest.retrieveSignableData(approverPublicKey = "7AH35qStXtrUgRkmqDmhjufNHjF74R1A9cCKT3C3HaAR").toHexString()
        )
    }

    @Test
    fun testBalanceAccountCreationInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )

        val privateKey = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = "2d7db52f8ff35aec03cd7be8d26c45d798774a4d5dfa9a9c559778752fb87d11"
        )

        val requestType: SolanaApprovalRequestType =
            getBalanceAccountCreationRequest(nonceAccountAddresses = listOf("CL8fZq5BzjCBXmixSMKqBsFoCLSFxqN6GvheDQ68HP44"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("QRKqHqP5SNEngXrcK2QeAR2nqx9AmwbHrmYF49ZbkEK")),
            email = "dont care",
            opAccountPrivateKey = privateKey
        )

        Assert.assertEquals(
            "0301050969ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c71511fa69aa5bb02ddd4a80e4673e541767116f2034406d2f917725045d215bdd3f2ba22d14fd3198775d3b9d1b22d6b48743f502ac2129cdf0094af144febf0666a8574221c4298fd6dd12f8d67ac57a7e3586087ff177defef319a8f2b7ae8a9906a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000ff90e321f14ded704cbb267d1cbd7c0e9ae8c5e3ccbb6f47c95bbf75d5a924a606a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d05ffdcd59d8f12aab0f8644eb0a57db2187baf6cc1e0115a977082f2ecd54120030703030400040400000007020001340000000080b2720000000000b803000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d080501050206009901030000000000000000000000000000000000000000000000000000000000000000000000000000000000b59460e652df36f1ffb509cdf44bb3469f9054f93f8f707fe56685cdc6fc9c3300b94e0c79c1fb7db6ff3380f8bd8f09376fb8f87c488f98ec920164e1e3a7417101100e0000000000000100b33db8d45a74ca5c0593ea113efc73528320af0a70713d08f1ec3fa085c9c74c000001",
            initiationRequest.retrieveSignableData(approverPublicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ")
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
            "0201040869ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c71501d86d390e73db0061cc718bad82036d774f115923c2e5e6c675ca99dd41c4fd8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd77c4c8d158b8f7d6a29a29e8f938db7344b356d823531737b5405d71c995eab1a06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a0206030204000404000000070303010522090132577d9ee7c270f02040c9da6af65a135da4b69c5d57bebdb52e245ecc0f16d8",
            approvalRequest.retrieveSignableData(approverPublicKey = "8CpMnz9RNojAZWMyzWirH3Y7vBebkf2965SGmcwgYSY")
                .toHexString(),
        )
    }

    @Test
    fun testSolWithdrawalRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getSolWithdrawalRequest(nonceAccountAddresses = listOf("CL8fZq5BzjCBXmixSMKqBsFoCLSFxqN6GvheDQ68HP44"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("7de2dc4f438213a502317cc43a58cbe4a23e8680a2438e9166334393effd726f")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("4LTSSabuoUArWeLyAS2nstT4EjmP8L3y8qXYgJqRs6RC")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "0301080e69ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c715f6e8d816af808b1e4407ef06675d067e201881fc785606e3a12eec7c8bccb0712ba22d14fd3198775d3b9d1b22d6b48743f502ac2129cdf0094af144febf0666a8574221c4298fd6dd12f8d67ac57a7e3586087ff177defef319a8f2b7ae8a990be476bc2de9162c6f52a1170d16d263bd60cca5c76b924b8b94690e4c22a10f000000000000000000000000000000000000000000000000000000000000000006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000ff90e321f14ded704cbb267d1cbd7c0e9ae8c5e3ccbb6f47c95bbf75d5a924a61209c6930f6cb4d8e110eb62f6f9133ec338123ab6822970d5c055e0e0adc33b06a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f859095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d318efd8bd8dbc9ddab3329f58c5b16054ef3506a1c7c284eb6a13411a94f913f030503030600040400000005020001340000000080b2720000000000b803000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d0d0d010704080209000505050a0b0c72070000000000000000000000000000000000000000000000000000000000000000000000000000000000b59460e652df36f1ffb509cdf44bb3469f9054f93f8f707fe56685cdc6fc9c330065cd1d00000000cd62deaa07b0058a44cc8ec7b3a5fc67156a8b6d82bb4223b58d554a624256be",
            initiationRequest.retrieveSignableData(approverPublicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ")
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
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af3401d86d390e73db0061cc718bad82036d774f115923c2e5e6c675ca99dd41c4fd8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd7515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f23006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000000ec4916daf26706bf27b89ecfff6ba56155f1d4ab734f92f008b81d4176076267c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a02060302040004040000000703030105220901f5b64c094158e4d4b31dfc38b6c2b28b458a5d9fe0c33f1cb3736318f77f5a52",
            approvalRequest.retrieveSignableData(approverPublicKey = "8CpMnz9RNojAZWMyzWirH3Y7vBebkf2965SGmcwgYSY")
                .toHexString()
        )
    }

    @Test
    fun testSplWithdrawalRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getSplWithdrawalRequest(nonceAccountAddresses = listOf("6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("4ba2d7074c6fa66dca792b64260b0513a229fa0849b1ceaa5b9cff1285fedee7")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9kV51VcoGhA1YFkBxBhd7rG1nz7ZCVcsBpqaaGa1hgCD")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "03010a10d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34e34e9cdb62a817da76d5163666c93570c210e7287f7f268c77236b3002156ca08ea1bcb15aa292a9b51b7f05f19d0e27669957b9990a4aa8f9cddc1cf4c56c55515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f230f46cdc8655b9add6c8905bd1247a2ef15870d4076fb39915885d08628a9f22a493c4b438027247811fa84db55c2e6ede640264dad0876d56a8bf819f2cb17bfa06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000631f065d4a6b93340e8b8a8c3061fd6eb0b7d402fe560fc40a84e9c8ce1ac3035c66b35c237860fd27d95409d452edbd91300bbfe80850fd4841759722b5073106a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000008df1a38eac809d9806f4e9502bc085aadb4975b84dc3ba062b1ce416efd4b1c5000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f8590ec4916daf26706bf27b89ecfff6ba56155f1d4ab734f92f008b81d4176076268201354297cec572707724dac5f6c92613c5a8f04e34fe284c882de8d09a0826030b0303060004040000000b020001340000000080b2720000000000b8030000000000000ec4916daf26706bf27b89ecfff6ba56155f1d4ab734f92f008b81d4176076260f0d010704080209000a050b0c0d0e72070000000000000000000000000000000000000000000000000000000000000000000000000000000000c381ac8c68089013328ad37eda42b285abedc13fef404fc34e56528011ded600f401000000000000f7d1a2ef642101c041a4523de1dd26652402149065ae308c55e1924cb217cb48",
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
            "02010307d2c2e3ac53223ce6b5a6e04fe0f98071cf10a62646b6c1c100f9829afcced04e5335831f99da167bf80d87be098dbaafc9309035be4aedd53460c3571c05b6a0515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f230000000000000000000000000000000000000000000000000000000000000000006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000a78bdd1907176367f56f7fab4bee90dabaa7372794fb0403f75f2a96580998478201354297cec572707724dac5f6c92613c5a8f04e34fe284c882de8d09a082602030302040004040000000603030105220901db20e8ec87e412a88078e810a7dda408df916bd03f429a92a6d3b00fd819e304",
            approvalRequest.retrieveSignableData(approverPublicKey = "6bpAbKqWrtXBtdnWqA8YSybGTeyD91u9MNzQuP7641MH")
                .toHexString()
        )
    }

    @Test
    fun testUSDCConversionRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getConversionRequest(nonceAccountAddresses = listOf("6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("5f27be66e3eb697a4274f9359c87f9069762a5e2cb7a63a622e923bd6119b963")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9kV51VcoGhA1YFkBxBhd7rG1nz7ZCVcsBpqaaGa1hgCD")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "03010a10d2c2e3ac53223ce6b5a6e04fe0f98071cf10a62646b6c1c100f9829afcced04e17ce130f4d1b123ff7f5f840aee4e9fa5665106de0cf2d1245c2b60f6ade6e245335831f99da167bf80d87be098dbaafc9309035be4aedd53460c3571c05b6a0515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f230d1e5bffc491a1b7890805d162a2cf8f0a2facae1df8579eddfed575e44f958108e829493f87ba7dc9497154a2cf8e656ee9979277f9ac88b5570775e7cb447d106a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea94000001bbc7e99fc43d0c442a698780fa1c7e4bcfbe5f100df263390ef0ab695e1b85aa1a993efade361c637af59e4d387db1aec381df43083f38e789f4bd57280889906a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000008ac94d970e27bc29711d382b1d5fac3fe82f590485b065e57fcc6e83424110cd000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f859a78bdd1907176367f56f7fab4bee90dabaa7372794fb0403f75f2a96580998478201354297cec572707724dac5f6c92613c5a8f04e34fe284c882de8d09a0826030b0303060004040000000b020001340000000080b2720000000000b803000000000000a78bdd1907176367f56f7fab4bee90dabaa7372794fb0403f75f2a96580998470f0d010704080209000a050b0c0d0e72070000000000000000000000000000000000000000000000000000000000000000000000000000000000138543b25e89429dae0ec18a0fa198dc5006898f91b3b99d80a58d65bcdff9d00065cd1d00000000455c311d68d6d25a36bb09d58c4f62e6e637031c29b7fd3bd205b658500739cf",
            initiationRequest.retrieveSignableData(approverPublicKey = "6bpAbKqWrtXBtdnWqA8YSybGTeyD91u9MNzQuP7641MH")
                .toHexString()
        )
    }

    @Test
    fun testWrapConversionRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getWrapConversionRequest(nonceAccountAddresses = listOf("6UcFAr9rqGfFEtLxnYdW6QjeRor3aej5akLpYpXUkPWX"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("46b397c81d81f9c745bb61baf28337888907696c5e653a08a98b5ecbcc1c82c8")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9kV51VcoGhA1YFkBxBhd7rG1nz7ZCVcsBpqaaGa1hgCD")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "0301090fd5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af342a6b6e29ec48d15d528b864b1d58f441b263ed5f24db504928f6090efc8cb41d0b5e9dd920eed912053e5333449d7a92d82d80ebea0f12829aa36e93559b000e515cf7a3ae636d0b1f0ac3f76dc5bafdf519e49df160e0d2f5eb77747a40f2309b0ed81b27ca1d63c6a994c30755027b44c213a3a5948040c8d4e1703ed539fb5abb3bbf8838f5129b8032b1f4ffac9f4043ef034e9d9dab4d32f25055c7496f06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000ca16efb68a8429558cd821a7c0942d5960f0b2c5b7f3a54caf6920e4555ac75c069b8857feab8184fb687f634618c035dac439dc1aeb3b5598a0f0000000000106a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f859bad1dda43bb63a1a1841895eae8fc1398f8e943ccf637d87c6f25aa82b25067d8201354297cec572707724dac5f6c92613c5a8f04e34fe284c882de8d09a0826030a0303060004040000000a020001340000000080b2720000000000b803000000000000bad1dda43bb63a1a1841895eae8fc1398f8e943ccf637d87c6f25aa82b25067d0e0c01070405080209000a0b0c0d530a0000000000000000000000000000000000000000000000000000000000000000000000000000000000c344bc80949c53bf0f257f570c1beea68dbc9563a595d46d5c9a7367bd12a5cc0065cd1d0000000000",
            initiationRequest.retrieveSignableData(approverPublicKey = "mPAWwEkDygfLX7A8Tzox6wyZRBrEudpRN2frKRXtLoX")
                .toHexString()
        )
    }

    @Test
    fun testUnwrapConversionRequestInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getUnwrapConversionRequest(nonceAccountAddresses = listOf("C2Fo1L8qFzmfucaVpLxVt7sYdUEorHYiYfNS2iPGXhxP"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("1f00703a5a544f81174fc90ddee2c11a1d43d5011c363a8f943b2adabc78ca52")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("3sW5nKBjmPkCtaXcBQperkPbjjq1zh55WBYzqtn9snjP")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "0301091069ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c71559dd5a48b688d5903bf3567f05edf0ca12745f9146356f9256033dae9a31cc4a2421b9558d9cad67bd5659f0331b3fde0d92a4c499163a4c2ccc11270a2d1d2ca3c2dd1d18d8e3fc1fa348bbf55e69e9e8a08ce9d200c25d75a4f249959f504c6275d74f500d64b47ff8dc6c9bfb82e3719880e03108b80f16d99216a7cab105691ced267cd43434095fd9cca6f91dfc377cd32477982218304ddbfb22f80dd4ef0bb334bbe5de40c74a349008b2e63384f665fc8b7fe3f5e78f72763a49127706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000f620464ffe688ce6d211cd6634828308f9ee848ee9c65e9b447f6cc110bcb1f3069b8857feab8184fb687f634618c035dac439dc1aeb3b5598a0f0000000000106a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a906a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a000000008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f85901e7592836b04fb733c4ba26fba673c11822b869e3d71275da54051523c747c72aa74c4491bcecb2fb4e5c9deb5e9e12aa1286114d50d7fcbd1948e10cb2701e030b0303070004040000000b020001340000000080b2720000000000b80300000000000001e7592836b04fb733c4ba26fba673c11822b869e3d71275da54051523c747c70f0e0108040509020a00060b0c0d0e04530af01d1f00000000000139ed553bc39b91b4368c79f3383ead20640d917fec1312697463a4b062b8ed5539ed553bc39b91b4368c79f3383ead20640d917fec1312697463a4b062b8ed5500a3e1110000000001",
            initiationRequest.retrieveSignableData(approverPublicKey = "3S3WAHv5h7gyEVTPQRuz6sf8poKM439zr14pHF43MtLK")
                .toHexString(),
        )
    }

    @Test
    fun testDAppTransactionRequestInitiationRequest() {
        val opAccountPk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("4ec791e7966d99d23e8bc41f3822a34d001e2c03605f33759bed2cd74fd40d0c")
        )

        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(3712, 26726400),
            initiatorIsApprover = true
        )

        val nonceAccountAddresses = listOf(
            "2qpuGj3H4iG632vWbayr5Yx3uVb9irMq1cYWiciFbLrG",
            "7Y9C78kSA8hg4z9QoUii1Eqog7qiQjtuzfSL71wKCzUS"
        )
        val nonces = listOf(
            Nonce("453hdmRBt3jt9KqHeJAC9pAduuJzTDCJZQBi1kemAQbG"),
            Nonce("8ZMPfGBZjmqCMe6ngR5sM9XWnsfaHcEGm1GF6M7ifkzL")
        )

        val requestType = getDAppTransactionRequest(nonceAccountAddresses = nonceAccountAddresses)
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = nonces,
            email = "dont care",
            opAccountPrivateKey = opAccountPk
        )

        Assert.assertEquals(
            "0301050969ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c71566f27f2db441c6844925694845bb7f91fa08bb705b9ffbf9f189a0af46a652fda1df05ed1b96a36d0c048af32557c335b46afae3c8f776640ae5f5cb0eb6b9df1b5dba547244d7e30522b076d82809741955522f259f5dee9a1e15fd218083e506a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000083a5c7a7d932826c8487798ee6cfd509c6ea293106e5b8ecca977d2c41e337e206a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000fa09491f04ac2239eb373bbef45c45f284a55a980487b9566a58b31bf44fb0352d9c7004f85156016de1317cebf952f64cdbbc7056d5b8a4e300f89a96672607030703030400040400000007020001340000000000d0970100000000800e000000000000fa09491f04ac2239eb373bbef45c45f284a55a980487b9566a58b31bf44fb035080501050206008b011000000000000000000000000000000000000000000000000000000000000000000000000000000000008fa39a0314aaeeb6edf40164c2ef98f4b759f451e6f13044ccbc2a9f8eb070fd0badb583fd3e78be40bd8be44103ef35bc46f819251c20ff210be3a97a62ebc82e90c13b0d471b592c2984568133a41676460f72c885db57ad26bd5b628f938201",
            initiationRequest.retrieveSignableData(approverPublicKey = "BtstAXkRrjBdpDvknnfPwnr6UquXpUvAPZSFZi8wDftS")
                .toHexString()
        )

        val supplyInstructions = initiationRequest.supplyInstructions
        Assert.assertEquals(supplyInstructions.size, 1)
        Assert.assertEquals(supplyInstructions[0].nonce, nonces[1])
        Assert.assertEquals(supplyInstructions[0].nonceAccountAddress, nonceAccountAddresses[1])
        Assert.assertEquals(
            "0201040869ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c715a1df05ed1b96a36d0c048af32557c335b46afae3c8f776640ae5f5cb0eb6b9df61206386ac12c66621091063ee33ff67766782dc8be4d092d563b3f0c69469d366f27f2db441c6844925694845bb7f91fa08bb705b9ffbf9f189a0af46a652fd06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000083a5c7a7d932826c8487798ee6cfd509c6ea293106e5b8ecca977d2c41e337e20000000000000000000000000000000000000000000000000000000000000000fa09491f04ac2239eb373bbef45c45f284a55a980487b9566a58b31bf44fb035704b733ffb52f99a655a10bd88bd595584952862bb9845d8058cc3f0d73e52c90206030204000404000000070303050190021c0001008c97258f4e2489f1bb3d1029148e0d830b5a1399daff1084048e7bd8dbe9f8590700034fd74e0317ab5320172983c9e619f60429b2cd1513230b6dda5c09611995ba5601b4ea2a426fee32cf5fb1e44cd4527e485c614107ee1222acfcf089f01c3ed0e7034fd74e0317ab5320172983c9e619f60429b2cd1513230b6dda5c09611995ba56007c85973e9d59ddddf27b0700a10b00857e9daa46708a4a2f0ef97e3ebb691a580000000000000000000000000000000000000000000000000000000000000000000006ddf6e1d765a193d9cbe146ceeb79ac1cb485ed5f5b37913a8cf5857eff00a90006a7d517192c5c51218cc94c3d4af17f58daee089ba1fd44e3dbd98a00000000010000",
            supplyInstructions[0].retrieveSignableData(approverPublicKey = "BtstAXkRrjBdpDvknnfPwnr6UquXpUvAPZSFZi8wDftS").toHexString()
        )
    }

    @Test
    fun testDAppTransactionRequestApprovalDisposition() {
        val request =
            getWalletApprovalRequest(getDAppTransactionRequest(nonceAccountAddresses = listOf("49BWf2pjTevVMZk1odBiaT5m1cx4z2Q87A6MPtwtPqAn")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("47bdPMECePMq44kc97JwNzGtMBynLTJGr9hcsYYb4qgG")),
            email = "dont care"
        )
        Assert.assertEquals(
            approvalRequest.retrieveSignableData(approverPublicKey = "4L3kZzJQVbHbpxNLdBK6SFtvJgnrgWDb62hJgNfaGaeS")
                .toHexString(),
            "0201040869ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c715317434f927df16dcdac690e2aa964c736536a77f81da2a342a03bdf2437651072eab97a901ae7ae5c9ea3844f680385928c4edc49624eebeb04938445aff421766f27f2db441c6844925694845bb7f91fa08bb705b9ffbf9f189a0af46a652fd06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000fa09491f04ac2239eb373bbef45c45f284a55a980487b9566a58b31bf44fb0352e43b2ea0eba51b2f359f97e77fca863364aaf03b43214d891d625140e75425d0206030204000404000000070303010522090113ee5f2a8938c2f77b07b1b403b54e8180906d1a8408fb65ffa221f6e02e1a7f"
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
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34cdd65bdd5302de9e0457368e03c37dcd1e9029c3ab0facdcfc5889a81d0cf6138e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd7f9437a782883b62d38738b3da7fada188510e6d57d1e09bdbde19cf7ed16e60206a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a02060302040004040000000703030105220901d0225d0606ab4392c16c7be80cbf674e9424767c3c9acdb8ae1661470c4ddb65",
            approvalRequest.retrieveSignableData(approverPublicKey = "ErWAApTUwunKAobwFrVe2fTwtqdsQecQqWKSQJzysg4z")
                .toHexString(),
        )
    }

    @Test
    fun testAddDAppBookEntryInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getAddDAppBookEntry(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("72ff8f7fb4a441c93d4003e2bf67dd367e3293311c4f9433c422a2fbf305c477")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34f9437a782883b62d38738b3da7fada188510e6d57d1e09bdbde19cf7ed16e602982acb779028b0afdd5da5a26d78e1b82804ae449ce2fd2767d15f4325a7f1118e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000064fd89d243e47f6bd6ea9c7462d7ca1d504c02bf67d2b6738f892897ecaeab206a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030703030400040400000007020001340000000080b2720000000000b8030000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c080501050206006d1400000000000000000000000000000000000000000000000000000000000000000000000000000000000100e4523ff383e6bb5f73d3745e3554f53a56c61ba17c7bc49e481a9d01a96fdbd6a9037bac86a669c3470c8da04dcec8f3a3ec671cd157264078954f38c387efb000",
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
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34cdd65bdd5302de9e0457368e03c37dcd1e9029c3ab0facdcfc5889a81d0cf6138e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd779dac0b298597dcbf810eb17709c9a75e1f4e569efe90f323c91c4ef084882c206a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a020603020400040400000007030301052209019268d8dd8d58ab95cf2d7f3ed9312dfbc131310a3068c8affa3b6518a359c85c",
            approvalRequest.retrieveSignableData(approverPublicKey = "ErWAApTUwunKAobwFrVe2fTwtqdsQecQqWKSQJzysg4z")
                .toHexString()
        )
    }

    @Test
    fun testRemoveDAppBookEntryInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getRemoveDAppBookEntry(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("1d3462075eae5a46257981c00c20982dd27a88b70a88ff95455dad6bc88859aa")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af3479dac0b298597dcbf810eb17709c9a75e1f4e569efe90f323c91c4ef084882c2982acb779028b0afdd5da5a26d78e1b82804ae449ce2fd2767d15f4325a7f1118e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000064fd89d243e47f6bd6ea9c7462d7ca1d504c02bf67d2b6738f892897ecaeab206a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030703030400040400000007020001340000000080b2720000000000b8030000000000002345d893870173ce1252d056dbc6c8bf6bb01f157832734623958d6249e63b5c080501050206006d140000000000000000000000000000000000000000000000000000000000000000000000000000000000000100e4523ff383e6bb5f73d3745e3554f53a56c61ba17c7bc49e481a9d01a96fdbd6a9037bac86a669c3470c8da04dcec8f3a3ec671cd157264078954f38c387efb0",
            initiationRequest.retrieveSignableData(approverPublicKey = "BEzpSizrNZpCeLWTk23nozu4T4wEzxoDJGoUUYBBhVbE")
                .toHexString()
        )
    }

    @Test
    fun testAddAddressBookEntryInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getAddAddressBookEntry(nonceAccountAddresses = listOf("8R4EuFv5f31D8HijRXA4eyebKMZ287ho2UyPpbtQ8Gos"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("a37195e87d53a831c1947e8afe02aec367e78f62fb912f7a614c3a387cd911fa")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("7r8cdEASTnapMjhk569Kwq7mtWwaqjMmkxYe75PBCCK5")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "0301050969ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c715be9052f2d7dc89487f3ec139a60d9cdda96f0d21895effdc81fc6906936345442ba22d14fd3198775d3b9d1b22d6b48743f502ac2129cdf0094af144febf06666e2b693808b4e0103f5b69faec8d19b9b92d12819052c214855f50e7dd3d1e2206a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000ff90e321f14ded704cbb267d1cbd7c0e9ae8c5e3ccbb6f47c95bbf75d5a924a606a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d65bc30ccd8e88f4b8d5cf5903523b520f1cf0dea124d428b55038269c03609bc030703030400040400000007020001340000000080b2720000000000b803000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d080501050206006e16000000000000000000000000000000000000000000000000000000000000000000000000000000000001001209c6930f6cb4d8e110eb62f6f9133ec338123ab6822970d5c055e0e0adc33bcd62deaa07b0058a44cc8ec7b3a5fc67156a8b6d82bb4223b58d554a624256be0000",
            initiationRequest.retrieveSignableData(approverPublicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ")
                .toHexString(),
        )
    }

    @Test
    fun testAddAddressBookEntryApprovalRequest() {
        val request =
            getWalletApprovalRequest(getAddAddressBookEntry(nonceAccountAddresses = listOf("Aj8MqPBaM8fSbgJiUtq2PXESGTSQPsgHqJ13JyzQZCRZ")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("7r8cdEASTnapMjhk569Kwq7mtWwaqjMmkxYe75PBCCK5")),
            email = "dont care"
        )
        Assert.assertEquals(
            "0201040869ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c7155b867b0ff902f8bd3503e7a6c3ba5a94b90bd26858f6de78aa0913af0d8be7c69083e5bc3e157aff14eb7d4db4331ce36f0b43c131c269bbe16f79ab36f98eacbe9052f2d7dc89487f3ec139a60d9cdda96f0d21895effdc81fc69069363454406a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d65bc30ccd8e88f4b8d5cf5903523b520f1cf0dea124d428b55038269c03609bc0206030204000404000000070303010522090116aa15d9a45731f7335bdda8a6ffb737d81fa305362c38ff4e9a8495bea126c9",
            approvalRequest.retrieveSignableData(approverPublicKey = "7AH35qStXtrUgRkmqDmhjufNHjF74R1A9cCKT3C3HaAR")
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
            "0201040869ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c7152ae5404ca4d115addf760a932a2564636c071f3d93077c7722926026963d760e8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd7d17a6a48d07bbbf8d76e02379e0758f4580f3cb34a56980929e72e9b0d58e97206a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a020603020400040400000007030301052209019eeebc4458643d9d4e03275c4e0c7fa3f6b9d550948e0e7a6768da4e9a5e1e51",
            approvalRequest.retrieveSignableData(approverPublicKey = "3tSshpPL1WyNR7qDfxPffinndQmgfvTGoZc3PgL65Z9o")
                .toHexString()
        )
    }

    @Test
    fun testWalletConfigPolicyUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getWalletConfigPolicyUpdate(nonceAccountAddresses = listOf("5osJEyGL1Ryiv9jedyhjnMqXHQaAM6A5PK253DTCTVdf"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("57cd2b0b1df0dd87e9001553716f2f4e9e1d2a8d0f4019f3048584cd3e83b385")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("7r8cdEASTnapMjhk569Kwq7mtWwaqjMmkxYe75PBCCK5")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "0301050969ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c7154bccd7a4522437daafe21e92b2b39dc596693b65aa9998ba1d961371884161272ba22d14fd3198775d3b9d1b22d6b48743f502ac2129cdf0094af144febf066647705611fa76d9ad3d1f58734c7ddc3476ef2e1317d807cb3b367a67ff75f83e06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000ff90e321f14ded704cbb267d1cbd7c0e9ae8c5e3ccbb6f47c95bbf75d5a924a606a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d65bc30ccd8e88f4b8d5cf5903523b520f1cf0dea124d428b55038269c03609bc030703030400040400000007020001340000000080b2720000000000b803000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d08050105020600560e0000000000000000000000000000000000000000000000000000000000000000000000000000000000025046000000000000020001358bd353729e02da524daff5551b39a3560970f30898fb5eb106986174088441",
            initiationRequest.retrieveSignableData(approverPublicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ")
                .toHexString(),
        )
    }

    @Test
    fun testBalanceAccountSettingsUpdateApprovalRequest() {
        val request =
            getWalletApprovalRequest(getBalanceAccountSettingsUpdate(nonceAccountAddresses = listOf("CpBzxGEDYnzi9jfGteSR6sCnmtT9XwirXQaCtSvmWnka")))
        val approvalRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = request.id!!,
            requestType = request.getSolanaApprovalRequestType(),
            nonces = listOf(Nonce("QRKqHqP5SNEngXrcK2QeAR2nqx9AmwbHrmYF49ZbkEK")),
            email = "dont care"
        )
        Assert.assertEquals(
            "0201040869ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c7155b867b0ff902f8bd3503e7a6c3ba5a94b90bd26858f6de78aa0913af0d8be7c6af874a8154f0de3ee6ff1668b3c18c49d62445d051d6b5cdcd6f5ada742c7d5bbe5ad75c51ec9e4c5d99214b8857b1d3ef76e40960b3ce2160a1c70fb0047e3606a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d05ffdcd59d8f12aab0f8644eb0a57db2187baf6cc1e0115a977082f2ecd541200206030204000404000000070303010522090105339c334afb37619a4fd2bb47a885e8e1a72c52696893d8188666d0995a17b9",
            approvalRequest.retrieveSignableData(approverPublicKey = "7AH35qStXtrUgRkmqDmhjufNHjF74R1A9cCKT3C3HaAR")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountSettingsUpdateUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getBalanceAccountSettingsUpdate(nonceAccountAddresses = listOf("CL8fZq5BzjCBXmixSMKqBsFoCLSFxqN6GvheDQ68HP44"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("514c07777ad9cd72f1493191f785170e1c14c5e37c9c6714863c9fa425965545")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("J2gVnUf56KpHARmwbagi3sX2TFHXNBUBGugXtvJrgxJq")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "0301050969ab8cb05413af9614f898a1f1fdfbc07e7ad5eb2eb1d0f1c49f448bd179c715be5ad75c51ec9e4c5d99214b8857b1d3ef76e40960b3ce2160a1c70fb0047e362ba22d14fd3198775d3b9d1b22d6b48743f502ac2129cdf0094af144febf0666a8574221c4298fd6dd12f8d67ac57a7e3586087ff177defef319a8f2b7ae8a9906a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000ff90e321f14ded704cbb267d1cbd7c0e9ae8c5e3ccbb6f47c95bbf75d5a924a606a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b21000000000000000000000000000000000000000000000000000000000000000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03dfd04eac9b2ed33f69fc778f2c3d9e413e041a3a07bf3fa787f1c14301000214e030703030400040400000007020001340000000080b2720000000000b803000000000000095f6d73715ba2affc6e25f8845e8f18908d92a129f233413b34d675d6fbd03d080501050206004e120000000000000000000000000000000000000000000000000000000000000000000000000000000000b59460e652df36f1ffb509cdf44bb3469f9054f93f8f707fe56685cdc6fc9c3301010000",
            initiationRequest.retrieveSignableData(approverPublicKey = "3wKxhgiogoCaA2uxPYeH7cy3cG4hxRPogrPmDPLS54iZ")
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
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34a6bba3eafd49e6bf5e8facf0faeea7cf500c019cd18cfa625f764213df7b8bd5a3541700f919ae296291c89fcff67de5d3cc0d941dfd342c85e641f6cea2cb56bb2b351f441f46df2039f49e8cd3f01079a908cad599a84079cb8189b218f57806a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b9e1a189ce3273e79eef0d169e15ced5902ca2a4a680fc7f710ef4513ef02ebdd020603020400040400000007030301052209019373d32f7e0c28081730f51d9bb7f4f2c52c488b4e706a133f52a5768fcf4453",
            approvalRequest.retrieveSignableData(approverPublicKey = "CDrdR8xX8t83eXxB2ESuHp9AxkiJkUuKnD98zyDfMtrG")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountPolicyUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getBalanceAccountPolicyUpdate(nonceAccountAddresses = listOf("5Fx8Nk98DbUcNUe4izqJkcYwfaMGmKRbXi1A7fVPfzj7"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("370349daceb62cb1ad6f37fbaba12dc72e36367c57b2ee976527609cd8d3f63e")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("DvKqKEBaJ71C5Hw8Yn45NvsYhpXfAYHybBbUa17nHcUm")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34bb2b351f441f46df2039f49e8cd3f01079a908cad599a84079cb8189b218f57838e70bc45546b0d63742dee544ecc6870f66da475c800d2d793c766b03266cca3f4336251703628ce12796daa20166a1f0da8a9a972775f9f04a256482e2bede06a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea9400000c43a8066d7b0612d9abc74bf23e9bc1230258306dcb561755b8d71c5ad38d5f406a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09bbff5520f20afb88a51a9a0630fb5bc2738f26b68af438c6a1750a68a4c2fc3c6030703030400040400000007020001340000000080b2720000000000b80300000000000074252b614aa502d0fa9eb2aef6eb7b43c25b6db7a61b70ae56b0cde9770fe09b08050105020600761a000000000000000000000000000000000000000000000000000000000000000000000000000000000046834450499b8d2edd17b54df5b3cd21a7e40369f8e3f8f072470cac3a271a7402100e0000000000000200011618435becfcd77198205d44019be2254d324294b97ef819e0c77d3af8b0e446",
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
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34dd896d086f2c63e124ed47d94ee7b4932644e826e8280cb345893312aa199bc92e5ed518e5ea088f46ae95e2cb452fc7be22322d0a63e0ef7a820e8aa2593d7759427bbc05d796626ca3c12d0b3553f51e4a0a0582be08acfed19d4d8fa6ca3106a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000007bd238678c7de6666b1fa72f0423be16b875681575a02d9bfb142bed5c64ea35433ce76291f054c3712f68b3fc11a56a6a3f2e5447b3eb7d387ddc739ce419610206030204000404000000070303010522090161b966b30dc8507d2afec35e43873267933214a601565865b80372ad926dca6d",
            approvalRequest.retrieveSignableData(approverPublicKey = "FuneCbHNcAmaG9gEyisDYiZFLiYTGsuVsFMArXJDR3np")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountAddressWhitelistUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getBalanceAccountAddressWhitelistUpdate(nonceAccountAddresses = listOf("9LGMMPep1WKdiNNwicDvx8JiwgtBKPWhidaSv3rVUNz"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("a9989f27d789b3c2266db5dbd1420e2831cacbb161d6e95bd48323911560fd11")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("Bb2XveQNyXVBJvJUPnsbhRtYqePUi8xbvBaJxk96BKDG")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af3459427bbc05d796626ca3c12d0b3553f51e4a0a0582be08acfed19d4d8fa6ca312f1c6ccaca0b0a0d12b938444f2ec6a9ec82b810394c64237a4651c5a41d4cd902226dd9d2e98d75fab1c2b62b8b007bab361b66ddffaa2362d30e8d8b915e3706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000002827f15215947e7af780bb61613d832c508101f217f8c259828ffc4680fbcde06a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b210000000000000000000000000000000000000000000000000000000000000000000000007bd238678c7de6666b1fa72f0423be16b875681575a02d9bfb142bed5c64ea359d4c59b0c3139035ef4ad8c1c52ed58a7daf2db23646c0704575e009c35fac6f030703030400040400000007020001340000000080b2720000000000b8030000000000007bd238678c7de6666b1fa72f0423be16b875681575a02d9bfb142bed5c64ea35080501050206006d2100000000000000000000000000000000000000000000000000000000000000000000000000000000005560c327edbddd0faa5fe1ed8ff2e8da684374eb45e2cdd67cba4f3bb258fbd50201021b642a192de6a4165d92cf0e3d0c00e6ec86f02d6c71c537a879749da2200b91",
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
            "02010408d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af34e6e137f1b3e582e55db0f594a6cb6f05d5a08fc71d7413042921bf24f72e73eb8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd75c5c48251d37fc912ce1ac482a5b79e5f904d3202d47287f39edf2e1b6bb241006a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea940000006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000064424795ac2edb4b21b281bd120d0ababb12d4ae690773f41f5f61027a7add9f7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a02060302040004040000000703030105220901ebdcefe98317428b88db3ec8c27fee8f20b12efaddbe4b6de661f8b74c0b509e",
            approvalRequest.retrieveSignableData(approverPublicKey = "GYFxPGjuBXYKg1S91zgpVZCLP4guLGRho27bTAkAzjVL")
                .toHexString()
        )
    }

    @Test
    fun testBalanceAccountNameUpdateInitiationRequest() {
        val initiation = MultiSigOpInitiation(
            opAccountCreationInfo = getOpAccountCreationInfo(),
            initiatorIsApprover = true
        )
        val requestType =
            getBalanceAccountNameUpdate(nonceAccountAddresses = listOf("AaFj4THN8CJmDPyJjPuDpsfC5FZys2Wmczust5UfmqeN"))
        val request = getWalletInitiationRequest(requestType, initiation = initiation)
        val pk = generateEphemeralPrivateKeyFromText(
            keyValueAsHex = ("7707e53ddb688826e19d5d1d651450222c3d6cf73680fd331430278bba237328")
        )
        val initiationRequest = InitiationRequest(
            requestId = request.id!!,
            approvalDisposition = ApprovalDisposition.APPROVE,
            initiation = initiation,
            requestType = requestType,
            nonces = listOf(Nonce("9PGftXH39kRKndTxL4hQppfonLMZQpWWWzvaHzYsAcLy")),
            email = "dont care",
            opAccountPrivateKey = pk
        )

        Assert.assertEquals(
            "03010509d5259a75898e5c16f1b0675c496a9f8ee74dd7687f234ba93c0ff09dfee8af345c5c48251d37fc912ce1ac482a5b79e5f904d3202d47287f39edf2e1b6bb24109e94ede101ab5be0734b6500e0fc10b51ca23e89e67391657197fd7b2529c13e8e3dffb3877aaf1f737715f58920c52f4fcec66fab4ac69bb95f0ad69e33bcd706a7d517192c568ee08a845f73d29788cf035c3145b21ab344d8062ea94000003468bd8cddd071cd3bb0a3c50c4b5cab7dfe4ae3328081889ebabd48d8b7c9c006a7d51718c774c928566398691d5eb68b5eb8a39b4b6d5c73555b2100000000000000000000000000000000000000000000000000000000000000000000000064424795ac2edb4b21b281bd120d0ababb12d4ae690773f41f5f61027a7add9f7c91fdec5cfa84288fc1e5d1732f0cc5ebba8ba90e1720fc85cf8f328bd1529a030703030400040400000007020001340000000080b2720000000000b80300000000000064424795ac2edb4b21b281bd120d0ababb12d4ae690773f41f5f61027a7add9f080501050206006a1800000000000000000000000000000000000000000000000000000000000000000000000000000000003c69f1851b7318b7f14f04992a80df6054b8bc4f325f24bce0d378d770e870c44e637072f628e09a14c28a2559381705b1674b55541eb62eb6db926704666ac5",
            initiationRequest.retrieveSignableData(approverPublicKey = "Bg38YKHxGQrVRMB254yCKgVjtRapi68H4SD1RCiwWo7b")
                .toHexString()
        )
    }

    @Test
    fun testAcceptVaultInvitation() {
        val deserializer = WalletApprovalDeserializer()
        val acceptVaultInvitationJson: JsonElement =
            JsonParser.parseString(acceptVaultInvitationJson.trim())
        val acceptVaultInvitationApproval = deserializer.parseData(acceptVaultInvitationJson)


        val approvalDispositionRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = acceptVaultInvitationApproval.id!!,
            requestType = acceptVaultInvitationApproval.getSolanaApprovalRequestType(),
            nonces = listOf(),
            email = "dont care"
        )

        when (approvalDispositionRequest.requestType) {
            is SolanaApprovalRequestType.AcceptVaultInvitation -> {
                val signableData =
                    approvalDispositionRequest.retrieveSignableData(approverPublicKey = "GYFxPGjuBXYKg1S91zgpVZCLP4guLGRho27bTAkAzjVL")
                assertEquals(
                    String(signableData, charset = Charsets.UTF_8),
                    "Test Organization 1"
                )
            }
            else ->
                assertTrue("should not get here", false)
        }
    }

    @Test
    fun testPasswordReset() {
        val deserializer = WalletApprovalDeserializer()
        val passwordResetJson: JsonElement =
            JsonParser.parseString(passwordResetJson.trim())
        val passwordResetApproval = deserializer.parseData(passwordResetJson)


        val approvalDispositionRequest = ApprovalDispositionRequest(
            approvalDisposition = ApprovalDisposition.APPROVE,
            requestId = passwordResetApproval.id!!,
            requestType = passwordResetApproval.getSolanaApprovalRequestType(),
            nonces = listOf(),
            email = "dont care"
        )

        when (approvalDispositionRequest.requestType) {
            is SolanaApprovalRequestType.PasswordReset -> {
                val signableData =
                    approvalDispositionRequest.retrieveSignableData(approverPublicKey = "GYFxPGjuBXYKg1S91zgpVZCLP4guLGRho27bTAkAzjVL")
                assertEquals(
                    String(signableData, charset = Charsets.UTF_8),
                    ""
                )
            }
            else ->
                assertTrue("should not get here", false)
        }
    }

}