package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.data.PhraseValidatorImpl
import junit.framework.Assert.assertFalse
import org.junit.Test

class PhraseValidatorTest {

    private val phraseValidator = PhraseValidatorImpl()

    val originalPhrase =
        "echo flat forget radio apology old until elite keep fine clock parent cereal ticket dutch whisper flock junior pet six uphold gorilla trend spare"

    val invalidWord = "belly"

        @Test
    fun testValidPhrase() {
        assert(phraseValidator.isPhraseValid(originalPhrase))
    }

    @Test
    fun testShortPhrase() {
        val words = originalPhrase.split(" ").toMutableList()
        words.removeAt(0)

        val validPhrase = phraseValidator.isPhraseValid(words.joinToString(" "))
        assertFalse(validPhrase)
    }

    @Test
    fun testLongPhrase() {
        val words = originalPhrase.split(" ").toMutableList()
        words.add("hello")

        val validPhrase = phraseValidator.isPhraseValid(words.joinToString(" "))
        assertFalse(validPhrase)
    }

    @Test
    fun invalidWordAt5thSpot() {
        val words = originalPhrase.split(" ").toMutableList()
        words[4] = (invalidWord)

        val validPhrase = phraseValidator.isPhraseValid(words.joinToString(" "))
        assertFalse(validPhrase)
    }
}