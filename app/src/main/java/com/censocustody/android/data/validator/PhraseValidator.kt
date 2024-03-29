package com.censocustody.android.data.validator

import cash.z.ecc.android.bip39.Mnemonics
import com.censocustody.android.data.validator.PhraseValidator.Companion.EXPECTED_LENGTH
import com.censocustody.android.data.validator.PhraseValidator.Companion.NOT_ENOUGH_WORDS
import com.censocustody.android.data.validator.PhraseValidator.Companion.TOO_MANY_WORDS
import java.util.*

interface PhraseValidator {
    fun isPhraseValid(phrase: String): Boolean
    fun userInputValidPhrase(originalPhrase: String, inputtedPhrase: String): Boolean
    fun format(text: String): String

    companion object {
        const val EXPECTED_LENGTH = 24
        const val NOT_ENOUGH_WORDS = "not enough words in phrase"
        const val TOO_MANY_WORDS = "too many words in phrase"
    }
}

class PhraseValidatorImpl() : PhraseValidator {

    private fun getSuffixForWordIndex(index: Int): String =
        when (index) {
            1, 21 -> "st"
            2, 22 -> "nd"
            3, 23 -> "rd"
            in (4..20) -> "th"
            else -> "th"
        }

    override fun isPhraseValid(phrase: String): Boolean {
        val phraseWords = phrase.split(" ")
        val wordCount = phraseWords.size

        if (wordCount < EXPECTED_LENGTH) {
            return false
        } else if (wordCount > EXPECTED_LENGTH) {
            return false
        }

        for ((_, word) in phraseWords.withIndex()) {
            if (word !in words) {
                return false
            }
        }

        return true
    }

    override fun userInputValidPhrase(originalPhrase: String, inputtedPhrase: String): Boolean {
        if (originalPhrase == inputtedPhrase) {
            return true
        }

        val originalWords = originalPhrase.split(" ")
        val inputtedWords = inputtedPhrase.split(" ")

        val originalSize = originalWords.size
        val inputtedSize = inputtedWords.size

        if (originalSize != inputtedSize) {
            if (originalSize > inputtedSize && inputtedSize < EXPECTED_LENGTH) {
                throw Exception(NOT_ENOUGH_WORDS)
            }

            if (originalSize < inputtedSize && inputtedSize > EXPECTED_LENGTH) {
                throw Exception(TOO_MANY_WORDS)
            }
        }

        for ((index, word) in originalWords.withIndex()) {
            val phraseTwoWord = inputtedWords[index]

            if (word != phraseTwoWord) {
                throw Exception("${index + 1}${getSuffixForWordIndex(index + 1)} does not match original phrase.")
            }
        }

        return false
    }

    override fun format(text: String) =
        text.replace("[^A-Za-z ]".toRegex(), " ").replace("\\s+".toRegex(), " ").trim()

    companion object {
        val words = Mnemonics.getCachedWords(Locale.ENGLISH.language)
    }

}