package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.data.PhraseValidator.Companion.NOT_ENOUGH_WORDS
import com.strikeprotocols.mobile.data.PhraseValidator.Companion.TOO_MANY_WORDS
import com.strikeprotocols.mobile.data.PhraseValidatorImpl
import junit.framework.Assert.assertEquals
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

        try {
            phraseValidator.isPhraseValid(words.joinToString(" "))
            assert(false)
        } catch(e: Exception) {
            assertEquals(e.message, NOT_ENOUGH_WORDS)
        }

    }

    @Test
    fun testLongPhrase() {
        val words = originalPhrase.split(" ").toMutableList()
        words.add("hello")

        try {
            phraseValidator.isPhraseValid(words.joinToString(" "))
            assert(false)
        } catch(e: Exception) {
            assertEquals(e.message, TOO_MANY_WORDS)
        }
    }

    @Test
    fun invalidWordAt5thSpot() {
        val words = originalPhrase.split(" ").toMutableList()
        words[4] = (invalidWord)

        try {
            phraseValidator.isPhraseValid(words.joinToString(" "))
            assert(false)
        } catch(e: Exception) {
            assertEquals(e.message, "The 5th word is not a valid word.")
        }
    }
}