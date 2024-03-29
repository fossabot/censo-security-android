package com.censocustody.android

import com.censocustody.android.common.wrapper.BaseWrapper
import com.censocustody.android.common.wrapper.toHexString
import com.censocustody.android.data.cryptography.Secp256k1HierarchicalKey
import com.censocustody.android.data.cryptography.Secp256k1HierarchicalKey.Companion.ethereumDerivationPath
import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.recovery.*
import org.junit.Assert.*
import org.junit.Test
import org.web3j.crypto.Hash


class OrgRecoveryTest {

    private val orgRecoveryRequestSingleChange = """
        {"request": {"deviceKey":"Rsoiwa7Te1UKqhb4rkCvad94LF2DWP73dAhCEhcGtD7KxGp7Ldyu7uG1be8W9jGRugkSX11VhLwNFUkd3sxWbw5w","chainKeys":[{"key":"xpub6F7vQRQK5uzkU9VmcuMyg2s3rb1eBRq8LBQFbfsLgP1UB6pw5MbYnhxsGAfXgLXzWkxEZ2iize523UN5t7ptsJHc5X6oZCDjCuyQYQRczK2","chain":"bitcoin"},{"key":"QbXeE69QrL4AP6BhDvWq1y9WXdd2SsW2JCbFViS7ofMaYbuSRrKhjcaQZPCU6cJdDPb8HLeZbq3CML6HVaZ4CF8U","chain":"ethereum"},{"key":"SJHmXxa1NDADNZV7hPayQ8DiBjaXmTacJ72eQVD1pPiEWRUtPDYghu3M3TChKDqByhrEKkKubUe1cfPBzgWYsqvv","chain":"offchain"}],"recoveryTxs":[{"chain":"ethereum","recoveryContractAddress":"0x7ab922404511f866efcf2dbfeb8e31d098ac52be","orgVaultSafeAddress":"0x98342c7ef2a33226a06c59c5238b2e1db07ec20e","oldOwnerAddress":"0xa0ecfee0dd249e76aaab0d91ac6e6b39c547e89d","newOwnerAddress":"0xe72687c0fd1dfdb93501fe7559e9ad6c4f638424","txs":[{"type":"OrgVaultSwapOwner","prev":"0x0000000000000000000000000000000000000001"}]},{"chain":"polygon","recoveryContractAddress":"0x7ab922404511f866efcf2dbfeb8e31d098ac52be","orgVaultSafeAddress":"0x98342c7ef2a33226a06c59c5238b2e1db07ec20e","oldOwnerAddress":"0xa0ecfee0dd249e76aaab0d91ac6e6b39c547e89d","newOwnerAddress":"0xe72687c0fd1dfdb93501fe7559e9ad6c4f638424","txs":[{"type":"OrgVaultSwapOwner","prev":"0x0000000000000000000000000000000000000001"}]}],"signingData":[{"type":"ethereum","transaction":{"safeNonce":1,"chainId":31337,"vaultAddress":null,"orgVaultAddress":null,"contractAddresses":[]}},{"type":"polygon","transaction":{"safeNonce":1,"chainId":31337,"vaultAddress":null,"orgVaultAddress":null,"contractAddresses":[]}}]},"recoveryPolicy":{"threshold":2,"addresses":["0xe8C76461cd5203081E6600467d8CC1248647e887","0x8Aab4D23dCD82CDF12b17b46085b1bAE26B084b9","0xB5E42d80883746606021F2D00a69d3f7e2a682cd"]},"signaturesReceivedFrom":[]}
    """.trimIndent()

    private val orgRecoveryRequestMultipleChanges = """
        {"request": {"deviceKey":"S6Mw6aRhrmBYJ4RgExdspTbHAbSMBWpgRZ3zDrrJ1ZVBvk1fsAy8t8Ug4kJ4ZopTajzMzXmvkF2oDEEkC4j38WJK","chainKeys":[{"key":"xpub6F2ev2cCX8LkbLfnRPtriabFHYSBJCwSoUSTmTTN5Dsoc5qZXL3kB59uzr45aRjfqczKXFZQRpJvqfa2fCyohExy5m8SVRhGMgtJyJyeqSU","chain":"bitcoin"},{"key":"QcfJhL7k4725ygLnfQECfDG6RepH4PCGgHTif22Es1TMmig3TVgbjQNYGepVba5ENnaQnAwkdhYiQ8grDHXfe5Ue","chain":"ethereum"},{"key":"N8ATWFKzYcayUWxbkkqBKK4Gzfhy6i3d8L2LZZ181Xpr1Kmha9TGeEExndAakes2tbot6Bvioefat7y8hA4NnY4D","chain":"offchain"}],"recoveryTxs":[{"chain":"ethereum","recoveryContractAddress":"0x7ab922404511f866efcf2dbfeb8e31d098ac52be","orgVaultSafeAddress":"0x98342c7ef2a33226a06c59c5238b2e1db07ec20e","oldOwnerAddress":"0x856b96d540c16c851b76405920742143fe33016b","newOwnerAddress":"0xb98ae9ac60883f68064bd40aa74982ba9db1ec14","txs":[{"type":"OrgVaultSwapOwner","prev":"0x0000000000000000000000000000000000000001"},{"type":"VaultSwapOwner","prev":"0xcc64cd16d077ff897101f5fc38138a2abc58f0e1","vaultSafeAddress":"0x82a0a0ef63130b67d40092934ba39ae4e064b475"},{"type":"WalletSwapOwner","prev":"0xcc64cd16d077ff897101f5fc38138a2abc58f0e1","vaultSafeAddress":"0x82a0a0ef63130b67d40092934ba39ae4e064b475","walletSafeAddress":"0xcb1d6517bb2d4dd8df60b790d0c23417670ada1e"}]},{"chain":"polygon","recoveryContractAddress":"0x7ab922404511f866efcf2dbfeb8e31d098ac52be","orgVaultSafeAddress":"0x98342c7ef2a33226a06c59c5238b2e1db07ec20e","oldOwnerAddress":"0x856b96d540c16c851b76405920742143fe33016b","newOwnerAddress":"0xb98ae9ac60883f68064bd40aa74982ba9db1ec14","txs":[{"type":"OrgVaultSwapOwner","prev":"0x0000000000000000000000000000000000000001"},{"type":"VaultSwapOwner","prev":"0xcc64cd16d077ff897101f5fc38138a2abc58f0e1","vaultSafeAddress":"0x82a0a0ef63130b67d40092934ba39ae4e064b475"},{"type":"WalletSwapOwner","prev":"0xcc64cd16d077ff897101f5fc38138a2abc58f0e1","vaultSafeAddress":"0x82a0a0ef63130b67d40092934ba39ae4e064b475","walletSafeAddress":"0x0c7c08a72af16f8dcef70bfc4df01ac4153fbfdc"}]}],"signingData":[{"type":"ethereum","transaction":{"safeNonce":0,"chainId":31337,"vaultAddress":null,"orgVaultAddress":null,"contractAddresses":[]}},{"type":"polygon","transaction":{"safeNonce":0,"chainId":31337,"vaultAddress":null,"orgVaultAddress":null,"contractAddresses":[]}}]},"recoveryPolicy":{"threshold":2,"addresses":["0xe8C76461cd5203081E6600467d8CC1248647e887","0x8Aab4D23dCD82CDF12b17b46085b1bAE26B084b9","0xB5E42d80883746606021F2D00a69d3f7e2a682cd"]},"signaturesReceivedFrom":[]}
    """.trimIndent()

    @Test
    fun testSingleChangeRecoveryTx() {
        val orgRecoveryRequestEnvelope = OrgAdminRecoveryRequestEnvelope.fromString(orgRecoveryRequestSingleChange)
        val recoveryAppSigningRequest = orgRecoveryRequestEnvelope.request.toRecoveryAppSigningRequest()
        assertEquals(
            "cb8a7e65872c8594abfdbe1ca5e0ab5df5f98258b6b9f35a44e535426f33a0bb",
            BaseWrapper.decodeFromBase64(recoveryAppSigningRequest.items.first { it.chain == Chain.ethereum }.dataToSign).toHexString()
        )
        assertEquals(
            "cb8a7e65872c8594abfdbe1ca5e0ab5df5f98258b6b9f35a44e535426f33a0bb",
            BaseWrapper.decodeFromBase64(recoveryAppSigningRequest.items.first { it.chain == Chain.polygon }.dataToSign).toHexString()
        )
        assertEquals(
            Hash.sha256(orgRecoveryRequestEnvelope.request.toJson().toByteArray()).toHexString(),
            BaseWrapper.decodeFromBase64(recoveryAppSigningRequest.items.first { it.chain == Chain.offchain }.dataToSign).toHexString()
        )
    }

    @Test
    fun testMultipleChangesRecoveryTx() {
        val orgRecoveryRequestEnvelope = OrgAdminRecoveryRequestEnvelope.fromString(orgRecoveryRequestMultipleChanges)
        val recoveryAppSigningRequest = orgRecoveryRequestEnvelope.request.toRecoveryAppSigningRequest()
        assertEquals(
            "334ca7d1168e12b626a827def790a75b79fb08bfa311ab571f955db6c8a1fc72",
            BaseWrapper.decodeFromBase64(recoveryAppSigningRequest.items.first { it.chain == Chain.ethereum }.dataToSign).toHexString()
        )
        assertEquals(
            "40fae325732650391653f60720ad2a11363ca9e6a95d72878c503784e0ace340",
            BaseWrapper.decodeFromBase64(recoveryAppSigningRequest.items.first { it.chain == Chain.polygon }.dataToSign).toHexString()
        )
        assertEquals(
            Hash.sha256(orgRecoveryRequestEnvelope.request.toJson().toByteArray()).toHexString(),
            BaseWrapper.decodeFromBase64(recoveryAppSigningRequest.items.first { it.chain == Chain.offchain }.dataToSign).toHexString()
        )
    }

    @Test
    fun testRecoveryAppFlow() {
        val seedPhrase = "whip spatial call cream base decorate tobacco life below lobster arena movie cat fix buffalo vibrant victory jungle category picnic way raise hazard exact"
        val recoveryAppKey = Secp256k1HierarchicalKey.fromSeedPhrase(seedPhrase, ethereumDerivationPath)
        println(recoveryAppKey.getBase58UncompressedPublicKey())

        val orgRecoveryRequestEnvelope = OrgAdminRecoveryRequestEnvelope.fromString(orgRecoveryRequestMultipleChanges)

        // get JSON request that will be string-ified in QR code for recovery app to read
        val recoveryAppSigningRequest = orgRecoveryRequestEnvelope.request.toRecoveryAppSigningRequest()

        // perform the steps recovery app does so sign with the key and send back json response via QR code
        val recoveryAppSigningResponse = RecoveryAppSigningResponse(
            recoveryAppKey.getEthereumAddress(),
            recoveryAppSigningRequest.items.map {
                RecoverySignatureItem(
                    it.chain,
                    BaseWrapper.encodeToBase64(recoveryAppKey.signData(BaseWrapper.decodeFromBase64(it.dataToSign)))
                )
            }
        )

        // map to the signature request to send to brooklyn
        val orgAdminRecoverySignaturesRequest = OrgAdminRecoverySignaturesRequest.fromRecoveryAppSigningResponse(
            orgRecoveryRequestEnvelope.request,
            recoveryAppSigningResponse
        )

        // verify signatures in request to brooklyn appear to be valid.
        assertTrue(
            recoveryAppKey.verifySignature(
                BaseWrapper.decodeFromBase64(recoveryAppSigningRequest.items.first { it.chain == Chain.ethereum }.dataToSign),
                BaseWrapper.decodeFromBase64(orgAdminRecoverySignaturesRequest.signatures.filterIsInstance<OrgAdminRecoverySignaturesRequest.Signature.Ethereum>().first().signature)
            )
        )

        assertTrue(
            recoveryAppKey.verifySignature(
                BaseWrapper.decodeFromBase64(recoveryAppSigningRequest.items.first { it.chain == Chain.polygon }.dataToSign),
                BaseWrapper.decodeFromBase64(orgAdminRecoverySignaturesRequest.signatures.filterIsInstance<OrgAdminRecoverySignaturesRequest.Signature.Polygon>().first().signature)
            )
        )

        assertTrue(
            recoveryAppKey.verifySignature(
                BaseWrapper.decodeFromBase64(recoveryAppSigningRequest.items.first { it.chain == Chain.offchain }.dataToSign),
                BaseWrapper.decodeFromBase64(orgAdminRecoverySignaturesRequest.signatures.filterIsInstance<OrgAdminRecoverySignaturesRequest.Signature.OffChain>().first().signature)
            )
        )

        assertEquals(
            recoveryAppSigningRequest.items.first { it.chain == Chain.offchain }.dataToSign,
            BaseWrapper.encodeToBase64(
                Hash.sha256(
                    BaseWrapper.decodeFromBase64(
                        orgAdminRecoverySignaturesRequest.signatures.filterIsInstance<OrgAdminRecoverySignaturesRequest.Signature.OffChain>().first().signedData
                    )
                )
            )
        )

        assertEquals(orgAdminRecoverySignaturesRequest.recoveryAddress, "0xD96fA11F6f86b648011dcD8cf047458932b043Df")
    }



}