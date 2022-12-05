package com.censocustody.mobile.presentation.key_management

import com.censocustody.mobile.presentation.key_management.KeyManagementState.Companion.DEFAULT_WORDS_VERIFIED
import com.censocustody.mobile.presentation.key_management.KeyManagementState.Companion.FIRST_WORD_INDEX
import com.censocustody.mobile.presentation.key_management.KeyManagementState.Companion.PHRASE_WORD_COUNT

data class ConfirmPhraseWordsState(
    val phraseWordToVerify: String = "",
    val phraseWordToVerifyIndex: Int = FIRST_WORD_INDEX,
    val pastedPhrase: String = "",
    val wordInput: String = "",
    val errorEnabled: Boolean = false,
    val wordsVerified: Int = DEFAULT_WORDS_VERIFIED,
    val words: List<String> = emptyList(),
    val isCreationKeyFlow: Boolean = false
) {
    val allWordsEntered: Boolean
        get() = words.isNotEmpty() && words.size == PHRASE_WORD_COUNT
}
