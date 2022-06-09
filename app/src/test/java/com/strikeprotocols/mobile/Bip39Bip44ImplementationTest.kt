package com.strikeprotocols.mobile

import cash.z.ecc.android.bip39.Mnemonics
import com.google.gson.Gson
import com.strikeprotocols.mobile.common.Base58
import com.strikeprotocols.mobile.common.Ed25519HierarchicalPrivateKey
import com.strikeprotocols.mobile.common.toHexString
import org.junit.Assert.*
import org.junit.Test
import java.util.*

fun ByteArray.toBase58() = Base58.encode(this)

class Bip39Bip44ImplementationTest {
    data class TestItem(
        val mnemonic: String,
        val privateKeyHex: String,
        val publicKeyBase58: String
    )

    data class TestItems(
        val items: List<TestItem>
    )

    @Test
    fun testWithKnownDataSet() {
        val testItems = Gson().fromJson(ClassLoader.getSystemResource("bip39-bip44/test-data.json").readText().trimEnd('\n'), TestItems::class.java).items
        for (testItem in testItems) {
            val ed25519PrivateKey = Ed25519HierarchicalPrivateKey.fromSeedPhrase(testItem.mnemonic)
            // @solana/web3j code which generated this data set represents the private key as the actual private key(32 bytes) appended with public key (another 32 bytes)
            assertEquals(testItem.privateKeyHex, (ed25519PrivateKey.privateKeyBytes + ed25519PrivateKey.publicKeyBytes).toHexString().lowercase())
            assertEquals(testItem.publicKeyBase58, ed25519PrivateKey.publicKeyBytes.toBase58())
        }
    }

    @Test
    fun testMnemonicGenerationAndVerification() {
        for (i in 0 until 100) {
            val phrase = String(Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_24).chars)
            val keyFromPhrase = Ed25519HierarchicalPrivateKey.fromSeedPhrase(phrase)
            val phraseWords = phrase.split(" ")
            assertEquals(phraseWords.size, 24)
            assertEquals(phrase, phraseWords.joinToString(" "))
            val keyFromPhraseWords = Ed25519HierarchicalPrivateKey.fromSeedPhrase(phraseWords.joinToString(" "))
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
        val privateKey = Ed25519HierarchicalPrivateKey.fromSeedPhrase(originalPhrase)

        // radio and forget are flipped - Mnenomics implementation detects words are transposed based on checksum validations
        val transposedWords = "echo flat radio forget apology old until elite keep fine clock parent cereal ticket dutch whisper flock junior pet six uphold gorilla trend spare"
        val e = assertThrows(Mnemonics.ChecksumException::class.java) {
            Ed25519HierarchicalPrivateKey.fromSeedPhrase(transposedWords)
        }
        assertEquals(e.message, "Error: The checksum failed. Verify that none of the words have been transposed.")

        // change the word old to new (which is not a valid word)
        val wrongWords = "echo flat forget radio apology new until elite keep fine clock parent cereal ticket dutch whisper flock junior pet six uphold gorilla trend spare"
        val e2 = assertThrows(Mnemonics.InvalidWordException::class.java) {
            Ed25519HierarchicalPrivateKey.fromSeedPhrase(wrongWords)
        }
        assertEquals(e2.message, "Error: <new> was not found in the word list.")

        // change forget to forged (it will give the index from start of the phrase where the letter is wrong)
        val incorrectSpelledWords = "echo flat forged radio apology old until elite keep fine clock parent cereal ticket dutch whisper flock junior pet six uphold gorilla trend spare"
        val e3 = assertThrows(Mnemonics.InvalidWordException::class.java) {
            Ed25519HierarchicalPrivateKey.fromSeedPhrase(incorrectSpelledWords)
        }
        assertEquals(e3.message, "Error: invalid word encountered at index 15.")

        // put in a valid word set but not for same key
        val otherValidPhrase = "refuse hedgehog nerve insect silent sunset regret slush walnut illness visit slim advance mobile shrug initial grid topple inch okay bunker marriage bench chapter"
        val otherPrivateKey = Ed25519HierarchicalPrivateKey.fromSeedPhrase(otherValidPhrase)

        assertNotEquals(privateKey.privateKeyBytes.toHexString(), otherPrivateKey.privateKeyBytes.toHexString())
    }
}
