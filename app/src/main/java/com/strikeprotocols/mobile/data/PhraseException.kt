package com.strikeprotocols.mobile.data

class PhraseException : Exception(NULL_PHRASE_IN_STATE) {
    companion object {
        const val NULL_PHRASE_IN_STATE = "NULL_PHRASE_IN_STATE"
    }
}