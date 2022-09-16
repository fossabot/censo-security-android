package com.strikeprotocols.mobile.common

import com.strikeprotocols.mobile.data.PhraseException
import com.strikeprotocols.mobile.data.models.IndexedPhraseWord
import com.strikeprotocols.mobile.presentation.key_management.ConfirmPhraseWordsState
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementFlow
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.CHANGE_AMOUNT
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.FIRST_WORD_INDEX
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.LAST_SET_START_INDEX
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.LAST_WORD_INDEX
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.PHRASE_WORD_COUNT

object PhraseEntryUtil {

    fun generateConfirmPhraseWordsState(state: KeyManagementState): ConfirmPhraseWordsState? {
        return when (state.keyManagementFlow) {
            KeyManagementFlow.KEY_CREATION -> {
                try {
                    val phrase =
                        state.keyGeneratedPhrase ?: throw Exception(PhraseException.NULL_PHRASE_IN_STATE)
                    generateConfirmPhraseWordsStateForCreationFlow(phrase = phrase)
                } catch (e: Exception) {
                    null
                }
            }
            KeyManagementFlow.KEY_RECOVERY -> {
                generateConfirmPhraseWordsStateForRecoveryFlow()
            }
            KeyManagementFlow.KEY_REGENERATION,
            KeyManagementFlow.KEY_MIGRATION,
            KeyManagementFlow.UNINITIALIZED -> {
                null
            }
        }
    }

    private fun generateConfirmPhraseWordsStateForCreationFlow(phrase: String): ConfirmPhraseWordsState {
        val initialPhraseWord = getPhraseWordAtIndex(
            phrase = phrase,
            index = FIRST_WORD_INDEX
        )

        return ConfirmPhraseWordsState(
            phraseWordToVerify = initialPhraseWord.wordValue,
            phraseWordToVerifyIndex = initialPhraseWord.wordIndex,
            wordInput = "",
            errorEnabled = false,
            wordsVerified = 0,
            isCreationKeyFlow = true
        )
    }

    private fun generateConfirmPhraseWordsStateForRecoveryFlow() =
        ConfirmPhraseWordsState(
            phraseWordToVerify = "",
            phraseWordToVerifyIndex = FIRST_WORD_INDEX,
            wordInput = "",
            errorEnabled = false,
            wordsVerified = 0,
            isCreationKeyFlow = false
        )

    //This method is used to cycle through the words being displayed for the user to save them
    fun handleWordIndexChanged(increasing: Boolean, currentWordIndex: Int): Int {
        var newWordIndex =
            if (increasing) {
                currentWordIndex + CHANGE_AMOUNT
            } else {
                currentWordIndex - CHANGE_AMOUNT
            }

        if (newWordIndex > LAST_WORD_INDEX) {
            newWordIndex = FIRST_WORD_INDEX
        } else if (newWordIndex < FIRST_WORD_INDEX) {
            //We want to display the last 4 words
            newWordIndex = LAST_SET_START_INDEX
        }

        return newWordIndex
    }

    fun assemblePhraseFromWords(words: List<String>): String {
        val phraseBuilder = StringBuilder()
        for (word in words) {
            phraseBuilder.append("$word ")
        }

        return phraseBuilder.toString().trim()
    }

    //region Creation Flow Word Processing Logic
    fun getPhraseWordAtIndex(phrase: String, index: Int): IndexedPhraseWord {
        if (!phrase.contains(" ") || phrase.split(" ").size < PHRASE_WORD_COUNT) {
            //Does not contain space
            //Does not equal 24 word phrase
            throw Exception(PhraseException.INVALID_PHRASE_IN_STATE)
        }

        val phraseWords = phrase.split(" ")
        val phraseWord = phraseWords[index]

        return IndexedPhraseWord(wordIndex = index, wordValue = phraseWord)
    }

    fun updateConfirmPhraseWordsStateWithNextWordToVerify(
        nextWordToVerify: IndexedPhraseWord,
        confirmPhraseWordsState: ConfirmPhraseWordsState
    ) =
        confirmPhraseWordsState.copy(
            phraseWordToVerify = nextWordToVerify.wordValue,
            phraseWordToVerifyIndex = nextWordToVerify.wordIndex,
            wordInput = "",
            errorEnabled = false
        )

    fun incrementWordsVerified(state: ConfirmPhraseWordsState) =
        state.copy(wordsVerified = state.wordsVerified + 1)

    fun checkIfAllPhraseWordsAreVerifiedDuringKeyCreation(
        wordsVerified: Int, wordIndex: Int
    ) = (wordsVerified == PHRASE_WORD_COUNT && wordIndex == LAST_WORD_INDEX)
    //endregion

    //region Recovery Flow Word Processing Logic
    private fun getNextWordToDisplay(index: Int, state: ConfirmPhraseWordsState): ConfirmPhraseWordsState {
        val words = state.words.toMutableList()

        val wordInput = getNextWordInput(words, index)

        return state.copy(
            phraseWordToVerifyIndex = index + 1,//Increment by one
            wordInput = wordInput,
            errorEnabled = false
        )
    }

    private fun getNextWordInput(words: List<String>, index: Int) =
        words.getOrNull(index + 1) ?: ""

    private fun replacePreviouslySubmittedWordWithEditedInput(
        editedWordInput: String,
        index: Int,
        state: ConfirmPhraseWordsState
    ): ConfirmPhraseWordsState? {
        val words = state.words.toMutableList()

        if (words.getOrNull(index) == null) {
            return null
        }

        //Replace the old word with the edited word
        words.set(index = index, element = editedWordInput)

        val wordInput = getNextWordInput(words, index)

        return state.copy(
            phraseWordToVerifyIndex = index + 1,//Increment by one
            wordInput = wordInput,
            errorEnabled = false,
            words = words
        )
    }

    fun addWordToPhraseWords(
        word: IndexedPhraseWord,
        phraseWords: MutableList<String>,
        state: ConfirmPhraseWordsState
    ): ConfirmPhraseWordsState {

        phraseWords.add(element = word.wordValue)

        return state.copy(
            phraseWordToVerifyIndex = word.wordIndex + 1,//Increment by one
            wordInput = "",
            errorEnabled = false,
            words = phraseWords
        )
    }

    fun handlePreviouslySubmittedWord(
        word: IndexedPhraseWord,
        submittedWords: List<String>,
        state: KeyManagementState
    ): KeyManagementState? {
        val previousSubmittedWord = submittedWords.getOrNull(word.wordIndex)
        if (previousSubmittedWord != null) {
            if (previousSubmittedWord == word.wordValue) {
                //Word has not been changed, get the next word to display
                return state.copy(
                    confirmPhraseWordsState = getNextWordToDisplay(
                        index = word.wordIndex,
                        state = state.confirmPhraseWordsState
                    )
                )
            } else {
                //Word has been edited, replace the previously submitted word
                val updatedConfirmPhraseWordsState = replacePreviouslySubmittedWordWithEditedInput(
                    editedWordInput = word.wordValue,
                    index = word.wordIndex,
                    state = state.confirmPhraseWordsState
                )
                //Return null or the updated state
                return updatedConfirmPhraseWordsState?.let { safeState ->
                    state.copy(
                        confirmPhraseWordsState = safeState
                    )
                }
            }
        } else {
            return null
        }
    }
    //endregion
}