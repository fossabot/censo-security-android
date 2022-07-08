package com.strikeprotocols.mobile.presentation.sign_in

enum class KeyCreationFlowStep {
    ENTRY_STEP, COPY_PHRASE_STEP, PHRASE_COPIED_STEP, PHRASE_SAVED_STEP, WRITE_WORD_STEP, VERIFY_WORDS_STEP,
    CONFIRM_KEY_ENTRY_STEP, CONFIRM_KEY_ERROR_STEP, ALL_SET_STEP, UNINITIALIZED, FINISHED
}

enum class KeyRecoveryFlowStep {
    ENTRY_STEP, VERIFY_WORDS_STEP, CONFIRM_KEY_ENTRY_STEP, CONFIRM_KEY_ERROR_STEP, ALL_SET_STEP, UNINITIALIZED, FINISHED
}

sealed class PhraseFlowAction {
    data class WordIndexChanged(val increasing: Boolean) : PhraseFlowAction()
    object LaunchManualKeyCreation : PhraseFlowAction()
    data class ChangeRecoveryFlowStep(
        val phraseGenerationFlowStep: KeyRecoveryFlowStep
    ) : PhraseFlowAction()

    data class ChangeCreationFlowStep(
        val phraseVerificationFlowStep: KeyCreationFlowStep
    ) : PhraseFlowAction()
}