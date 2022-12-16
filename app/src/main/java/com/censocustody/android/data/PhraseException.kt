package com.censocustody.android.data

class PhraseException : Exception(NULL_PHRASE_IN_STATE) {
    companion object {
        const val NULL_PHRASE_IN_STATE = "NULL_PHRASE_IN_STATE"
        const val INVALID_PHRASE_IN_STATE = "INVALID_PHRASE_IN_STATE"
        const val DEFAULT_ERROR = "DEFAULT_ERROR"
        const val FAILED_TO_VERIFY_PHRASE = "FAILED_TO_VERIFY_PHRASE"
    }
}

class RecoverKeyException(message: String) : Exception(message) {
    companion object {
        const val DEFAULT_KEY_RECOVERY_ERROR = "DEFAULT_KEY_RECOVERY_ERROR"
        const val MANUALLY_TYPED_PHRASE_IS_INVALID = "Manually typed phrase is invalid"
    }
}

class InvalidKeyPhraseException : Exception(INVALID_KEY_PHRASE_ERROR) {
    companion object {
        const val INVALID_KEY_PHRASE_ERROR = "INVALID_KEY_PHRASE_ERROR"
    }
}