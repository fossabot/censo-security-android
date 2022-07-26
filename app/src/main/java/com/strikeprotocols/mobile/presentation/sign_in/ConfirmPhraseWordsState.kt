package com.strikeprotocols.mobile.presentation.sign_in

data class ConfirmPhraseWordsState(
    val phrase: String = "",
    val phraseWordToVerify: String = "",
    val wordIndex: Int = DEFAULT_WORD_INDEX,
    val wordInput: String = "",
    val errorEnabled: Boolean = false,
    val wordsVerified: Int = DEFAULT_WORDS_VERIFIED,
    val words: List<String> = emptyList(),
    val isCreationKeyFlow: Boolean = false
) {
    val allWordsEntered: Boolean
        get() = words.isNotEmpty() && words.size == PHRASE_WORD_COUNT

    companion object {
        const val DEFAULT_WORD_INDEX = -1
        const val DEFAULT_WORDS_VERIFIED = 0

        const val PHRASE_WORD_FIRST_INDEX = 0
        const val PHRASE_WORD_SECOND_TO_LAST_INDEX = 23
        const val PHRASE_WORD_COUNT = 24
    }
}
