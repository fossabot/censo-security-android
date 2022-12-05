package com.censocustody.mobile.data

sealed class AuthDataException(val value: String) : Exception(value) {

    class InvalidPhraseException : AuthDataException(INVALID_PHRASE)
    class InvalidKeyPairException : AuthDataException(INVALID_KEYPAIR)
    class InvalidVerifyUserException : AuthDataException(INVALID_VERIFY_USER_DATA)
    class PhraseKeyDoesNotMatchBackendKeyException : AuthDataException(
        PHRASE_KEY_DOES_NOT_MATCH_BACKEND_KEY
    )

    companion object {
        const val INVALID_VERIFY_USER_DATA = "Verify User data is invalid"
        const val INVALID_PHRASE = "Invalid Phrase"
        const val INVALID_KEYPAIR = "Invalid KeyPair"
        const val PHRASE_KEY_DOES_NOT_MATCH_BACKEND_KEY = "Phrase Key Does Not Match Backend Key"
    }
}