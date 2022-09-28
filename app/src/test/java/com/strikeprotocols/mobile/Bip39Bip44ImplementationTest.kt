package com.strikeprotocols.mobile

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.google.gson.Gson
import com.strikeprotocols.mobile.common.*
import org.bitcoinj.core.Base58
import org.junit.Assert.*
import org.junit.Test
import java.util.*

fun ByteArray.toBase58() = Base58.encode(this)

class Bip39Bip44ImplementationTest {
    data class TestItem(
        val mnemonic: String,
        val privateKey: String,
        val publicKey: String
    )

    data class TestItems(
        val items: List<TestItem>
    )

    @Test
    fun testSolanaWithKnownDataSet() {
        val testItems = Gson().fromJson(ClassLoader.getSystemResource("bip39-bip44/solana-test-data.json").readText().trimEnd('\n'), TestItems::class.java).items
        for (testItem in testItems) {
            val ed25519PrivateKey = Ed25519HierarchicalPrivateKey.fromRootSeed(Mnemonics.MnemonicCode(testItem.mnemonic).toSeed())
            // @solana/web3j code which generated this data set represents the private key as the actual private key(32 bytes) appended with public key (another 32 bytes)
            assertEquals(testItem.privateKey, (ed25519PrivateKey.privateKeyBytes + ed25519PrivateKey.publicKeyBytes).toHexString().lowercase())
            assertEquals(testItem.publicKey, ed25519PrivateKey.publicKeyBytes.toBase58())
        }
    }

    @Test
    fun testBitcoinWithKnownDataSet() {
        val testItems = Gson().fromJson(ClassLoader.getSystemResource("bip39-bip44/bitcoin-test-data.json").readText().trimEnd('\n'), TestItems::class.java).items
        testItems.forEachIndexed { i, testItem ->
            println("Checking item $i, ${testItem.mnemonic}")
            val bitcoinKey = BitcoinHierarchicalKey.fromSeedPhrase(testItem.mnemonic).derive(ChildPathNumber(0, false))
            assertEquals(testItem.privateKey, bitcoinKey.getBase58ExtendedPrivateKey())
            assertEquals(testItem.publicKey, bitcoinKey.getBase58ExtendedPublicKey())
        }
    }

    @Test
    fun testMnemonicGenerationAndVerification() {
        for (i in 0 until 100) {
            val phrase = String(Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_24).chars)
            val keyFromPhrase = Ed25519HierarchicalPrivateKey.fromRootSeed(Mnemonics.MnemonicCode(phrase).toSeed())
            val phraseWords = phrase.split(" ")
            assertEquals(phraseWords.size, 24)
            assertEquals(phrase, phraseWords.joinToString(" "))
            val keyFromPhraseWords = Ed25519HierarchicalPrivateKey.fromRootSeed(Mnemonics.MnemonicCode(phraseWords.joinToString(" ")).toSeed())
            assertEquals(keyFromPhrase.privateKeyBytes.toHexString(), keyFromPhraseWords.privateKeyBytes.toHexString())
            assertEquals(keyFromPhrase.publicKeyBytes.toBase58(), keyFromPhraseWords.publicKeyBytes.toBase58())
        }
    }

    @Test
    fun testValidWords() {
        assert("zoo" in Mnemonics.getCachedWords(Locale.ENGLISH.language))
        assert("zoom" !in Mnemonics.getCachedWords(Locale.ENGLISH.language))
        assert("forget" in Mnemonics.getCachedWords(Locale.ENGLISH.language))
        assert("forged" !in Mnemonics.getCachedWords(Locale.ENGLISH.language))
    }

    @Test
    fun testVerificationError() {
        val originalPhrase = "echo flat forget radio apology old until elite keep fine clock parent cereal ticket dutch whisper flock junior pet six uphold gorilla trend spare"
        val privateKey = Ed25519HierarchicalPrivateKey.fromRootSeed(Mnemonics.MnemonicCode(originalPhrase).toSeed())

        // radio and forget are flipped - Mnenomics implementation detects words are transposed based on checksum validations
        val transposedWords = "echo flat radio forget apology old until elite keep fine clock parent cereal ticket dutch whisper flock junior pet six uphold gorilla trend spare"
        val e = assertThrows(Mnemonics.ChecksumException::class.java) {
            Ed25519HierarchicalPrivateKey.fromRootSeed(Mnemonics.MnemonicCode(transposedWords).toSeed())
        }
        assertEquals(e.message, "Error: The checksum failed. Verify that none of the words have been transposed.")

        // change the word old to new (which is not a valid word)
        val wrongWords = "echo flat forget radio apology new until elite keep fine clock parent cereal ticket dutch whisper flock junior pet six uphold gorilla trend spare"
        val e2 = assertThrows(Mnemonics.InvalidWordException::class.java) {
            Ed25519HierarchicalPrivateKey.fromRootSeed(Mnemonics.MnemonicCode(wrongWords).toSeed())
        }
        assertEquals(e2.message, "Error: <new> was not found in the word list.")

        // change forget to forged (it will give the index from start of the phrase where the letter is wrong)
        val incorrectSpelledWords = "echo flat forged radio apology old until elite keep fine clock parent cereal ticket dutch whisper flock junior pet six uphold gorilla trend spare"
        val e3 = assertThrows(Mnemonics.InvalidWordException::class.java) {
            Ed25519HierarchicalPrivateKey.fromRootSeed(Mnemonics.MnemonicCode(incorrectSpelledWords).toSeed())
        }
        assertEquals(e3.message, "Error: invalid word encountered at index 15.")

        // put in a valid word set but not for same key
        val otherValidPhrase = "refuse hedgehog nerve insect silent sunset regret slush walnut illness visit slim advance mobile shrug initial grid topple inch okay bunker marriage bench chapter"
        val otherPrivateKey = Ed25519HierarchicalPrivateKey.fromRootSeed(Mnemonics.MnemonicCode(otherValidPhrase).toSeed())

        assertNotEquals(privateKey.privateKeyBytes.toHexString(), otherPrivateKey.privateKeyBytes.toHexString())
    }

    @Test
    fun testBitcoinKeyGeneration() {
        val seedPhrase =
            "bacon loop helmet quarter exist notice laundry auction rain bus vanish buyer drama icon response"
        val expectedXprv =
            "xprvA3HQivyehcyaqxxDcV3Niye157nbJKpuETYf6aZZup65XHNbyrXrkVZuT5T3i7bTsoCjTXnqWfjLxWdMUgDL3kTM4XftmSQnz7LP6RGiShr"
        val expectedXpub =
            "xpub6GGm8SWYXzXt4T2giWaP67ajd9d5hnYkbgUFtxyBU9d4Q5hkXPr7JHtPJN5dD6uNVXb7EEpdZeXvG5XwFVWhUj4Q2ufhYH38fuHK9ERTy3d"
        val expectedPubKey = "0318287a66643f9db1956c812c533bb3d6b22dce7955d7169d6f8ed39d4e96c909"

        val signingKey = BitcoinHierarchicalKey.fromSeedPhrase(seedPhrase).derive(ChildPathNumber(0, false))

        assertEquals(expectedPubKey, signingKey.getPublicKeyBytes().toHexString().lowercase())
        assertEquals(expectedXprv, signingKey.getBase58ExtendedPrivateKey())
        assertEquals(expectedXpub, signingKey.getBase58ExtendedPublicKey())

        // create a verify key from the signing keys extended pub key
        // have the verifying key verify the signature from the signing key
        val verifyingKey = BitcoinHierarchicalKey.fromExtendedKey(signingKey.getBase58ExtendedPublicKey())
        val dataToSign = "hello world".toByteArray(charset("utf-8"))
        assertTrue(verifyingKey.verifySignature(dataToSign, signingKey.signData(dataToSign)))

        // check new keys from the extended public/private key match the key they came from
        val keyFromExtendedPriv = BitcoinHierarchicalKey.fromExtendedKey(expectedXprv)
        assertEquals(keyFromExtendedPriv.getBase58ExtendedPrivateKey(), signingKey.getBase58ExtendedPrivateKey())
        assertEquals(keyFromExtendedPriv.getBase58ExtendedPublicKey(), signingKey.getBase58ExtendedPublicKey())

        val keyFromExtendedPub = BitcoinHierarchicalKey.fromExtendedKey(expectedXpub)
        assertNull(keyFromExtendedPub.privateKey)
        assertEquals(keyFromExtendedPub.getBase58ExtendedPublicKey(), signingKey.getBase58ExtendedPublicKey())

        // check we can derive new children from the extended public and private keys and that the
        // extended public keys for the new children match
        val childPubKey = keyFromExtendedPub.derive(ChildPathNumber(1, false))
        val childPrivateKey = keyFromExtendedPriv.derive(ChildPathNumber(1, false))
        assertEquals(childPrivateKey.getBase58ExtendedPublicKey(), childPubKey.getBase58ExtendedPublicKey())
    }
}