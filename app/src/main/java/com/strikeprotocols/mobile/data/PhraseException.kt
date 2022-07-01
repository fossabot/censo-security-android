package com.strikeprotocols.mobile.data

class PhraseException : Exception(NULL_PHRASE_IN_STATE) {
    companion object {
        const val NULL_PHRASE_IN_STATE = "NULL_PHRASE_IN_STATE"
    }
}

class RecoverKeyException : Exception(DEFAULT_KEY_RECOVERY_ERROR) {
    companion object {
        const val DEFAULT_KEY_RECOVERY_ERROR = "DEFAULT_KEY_RECOVERY_ERROR"
    }
}

class InvalidKeyPhraseException : Exception(INVALID_KEY_PHRASE_ERROR) {
    companion object {
        const val INVALID_KEY_PHRASE_ERROR = "INVALID_KEY_PHRASE_ERROR"
    }
}