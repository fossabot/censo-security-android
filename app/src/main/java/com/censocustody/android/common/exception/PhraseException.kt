package com.censocustody.android.common

class PhraseException : Exception(NULL_PHRASE_IN_STATE) {
    companion object {
        const val NULL_PHRASE_IN_STATE = "NULL_PHRASE_IN_STATE"
        const val INVALID_PHRASE_IN_STATE = "INVALID_PHRASE_IN_STATE"
        const val DEFAULT_ERROR = "DEFAULT_ERROR"
        const val FAILED_TO_VERIFY_PHRASE = "FAILED_TO_VERIFY_PHRASE"
    }
}

class InvalidKeyPhraseException : Exception(INVALID_KEY_PHRASE_ERROR) {
    companion object {
        const val INVALID_KEY_PHRASE_ERROR = "INVALID_KEY_PHRASE_ERROR"
    }
}