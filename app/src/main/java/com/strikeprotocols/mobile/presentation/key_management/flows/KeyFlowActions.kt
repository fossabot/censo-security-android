package com.strikeprotocols.mobile.presentation.key_management.flows

sealed class KeyManagementFlowStep() {

    abstract fun isStepFinished() : Boolean

    data class CreationFlow(val step: KeyCreationFlowStep) : KeyManagementFlowStep() {
        override fun isStepFinished() = step == KeyCreationFlowStep.FINISHED
    }

    data class RecoveryFlow(val step: KeyRecoveryFlowStep) : KeyManagementFlowStep() {
        override fun isStepFinished() = step == KeyRecoveryFlowStep.FINISHED
    }

    data class RegenerationFlow(val step: KeyRegenerationFlowStep) : KeyManagementFlowStep() {
        override fun isStepFinished() = step == KeyRegenerationFlowStep.FINISHED
    }

    data class MigrationFlow(val step: KeyMigrationFlowStep) : KeyManagementFlowStep() {
        override fun isStepFinished() = step == KeyMigrationFlowStep.FINISHED
    }
}

enum class KeyCreationFlowStep {
    ENTRY_STEP, COPY_PHRASE_STEP, PHRASE_COPIED_STEP, PHRASE_SAVED_STEP, WRITE_WORD_STEP, VERIFY_WORDS_STEP,
    CONFIRM_KEY_ENTRY_STEP, CONFIRM_KEY_ERROR_STEP, ALL_SET_STEP, UNINITIALIZED, FINISHED
}

enum class KeyRecoveryFlowStep {
    ENTRY_STEP, VERIFY_WORDS_STEP, CONFIRM_KEY_ENTRY_STEP, CONFIRM_KEY_ERROR_STEP, ALL_SET_STEP, UNINITIALIZED, FINISHED
}

enum class KeyRegenerationFlowStep {
    ALL_SET_STEP, UNINITIALIZED, FINISHED
}

enum class KeyMigrationFlowStep {
    ALL_SET_STEP, UNINITIALIZED, FINISHED
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

sealed class PhraseEntryAction {
    //Navigation actions are only used during Key Recovery Flow
    object NavigatePreviousWord : PhraseEntryAction()
    object NavigateNextWord : PhraseEntryAction()
    data class PastePhrase(val phrase: String) : PhraseEntryAction()
    data class SubmitWordInput(val errorMessage: String) : PhraseEntryAction()
    data class UpdateWordInput(val wordInput: String) : PhraseEntryAction()
}