package com.strikeprotocols.mobile.data

class PhraseException : Exception(NULL_PHRASE_IN_STATE) {
    companion object {
        const val NULL_PHRASE_IN_STATE = "NULL_PHRASE_IN_STATE"
    }
}

class RegenerateKeyPhraseException : Exception(DEFAULT_KEY_REGENERATION_ERROR) {
    companion object {
        const val DEFAULT_KEY_REGENERATION_ERROR = "DEFAULT_KEY_REGENERATION_ERROR"
    }
}

class InvalidKeyPhraseException : Exception(INVALID_KEY_PHRASE_ERROR) {
    companion object {
        const val INVALID_KEY_PHRASE_ERROR = "INVALID_KEY_PHRASE_ERROR"
    }
}