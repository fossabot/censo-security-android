package com.censocustody.android.presentation.org_key_recovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.censocustody.android.common.Resource
import com.censocustody.android.common.censoLog
import com.censocustody.android.common.util.PhraseEntryUtil
import com.censocustody.android.data.models.IndexedPhraseWord
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.validator.PhraseValidator
import com.censocustody.android.presentation.key_management.ConfirmPhraseWordsState
import com.censocustody.android.presentation.key_management.KeyManagementFlow
import com.censocustody.android.presentation.key_management.KeyManagementState
import com.censocustody.android.presentation.key_management.PhraseInputMethod
import com.censocustody.android.presentation.key_management.flows.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrgKeyRecoveryViewModel @Inject constructor(
    private val keyRepository: KeyRepository,
    private val phraseValidator: PhraseValidator
) : ViewModel() {

    var state by mutableStateOf(OrgKeyRecoveryState())
        private set

    fun onStart() {
        state = state.copy(
            keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP)
        )
    }

    //VM Key Recovery Logic
    fun keyRecoveryNavigateForward() {
        if (state.keyRecoveryFlowStep !is KeyManagementFlowStep.RecoveryFlow) {
            return
        }

        val recoveryFlowStep: KeyRecoveryFlowStep =
            (state.keyRecoveryFlowStep as KeyManagementFlowStep.RecoveryFlow).step

        val nextStep = moveUserToNextRecoveryScreen(recoveryFlowStep)

        if (nextStep == KeyRecoveryFlowStep.VERIFY_WORDS_STEP) {
            setupConfirmPhraseWordsStateForRecoveryFlow()
        } else {
            state = state.copy(keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(nextStep))
        }
    }

    fun keyRecoveryNavigateBackward() {
        if (state.keyRecoveryFlowStep !is KeyManagementFlowStep.RecoveryFlow) {
            return
        }

        val recoveryFlowStep: KeyRecoveryFlowStep =
            (state.keyRecoveryFlowStep as KeyManagementFlowStep.RecoveryFlow).step

        val nextStep = moveUserToPreviousRecoveryScreen(recoveryFlowStep)

        state = state.copy(keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(nextStep))
    }

    private fun setupConfirmPhraseWordsStateForRecoveryFlow() {
        val confirmPhraseWordsState =
            PhraseEntryUtil.generateConfirmPhraseWordsState(
                state = KeyManagementState(
                    keyManagementFlow = KeyManagementFlow.KEY_RECOVERY
                )
            )

        if (confirmPhraseWordsState != null) {
            state = state.copy(
                keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.VERIFY_WORDS_STEP),
                confirmPhraseWordsState = confirmPhraseWordsState
            )
        }
    }

    fun phraseFlowAction(phraseFlowAction: PhraseFlowAction) {
        when (phraseFlowAction) {
            is PhraseFlowAction.WordIndexChanged -> {
                val updatedWordIndex = PhraseEntryUtil.handleWordIndexChanged(
                    increasing = phraseFlowAction.increasing,
                    currentWordIndex = state.wordIndexForDisplay
                )
                state = state.copy(wordIndexForDisplay = updatedWordIndex)
            }
            is PhraseFlowAction.ChangeRecoveryFlowStep -> {
                state =
                    state.copy(
                        keyRecoveryFlowStep =
                        KeyManagementFlowStep.RecoveryFlow(phraseFlowAction.phraseGenerationFlowStep)
                    )
            }
            else -> {
                return
            }
        }
    }

    //region USER PHRASE ENTRY ACTIONS
    fun phraseEntryAction(phraseEntryAction: PhraseEntryAction) {
        when (phraseEntryAction) {
            is PhraseEntryAction.NavigateNextWord -> {
                navigateNextWordDuringKeyRecovery()
            }
            is PhraseEntryAction.NavigatePreviousWord -> {
                navigatePreviousWordDuringKeyRecovery()
            }
            is PhraseEntryAction.PastePhrase -> {
                handlePastedPhrase(pastedPhrase = phraseEntryAction.phrase)
            }
            is PhraseEntryAction.SubmitWordInput -> {
                submitWordInput(errorMessage = phraseEntryAction.errorMessage)
            }
            is PhraseEntryAction.UpdateWordInput -> {
                updateWordInput(input = phraseEntryAction.wordInput)
            }
        }
    }

    fun updateWordInput(input: String) {
        state = state.copy(
            confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                wordInput = input.lowercase().trim()
            )
        )
    }

    private fun submitWordInput(errorMessage: String) {
        handleWordSubmitted(errorMessage = errorMessage)
    }

    private fun navigateNextWordDuringKeyRecovery() {
        if (state.confirmPhraseWordsState.wordInput.isEmpty() || state.confirmPhraseWordsState.wordInput.isBlank()) {
            return
        }

        //Navigate to next word should also act as a submitWord
        addWordInputToPhraseWordsDuringKeyRecovery(
            word = state.confirmPhraseWordsState.wordInput,
            currentWordIndex = state.confirmPhraseWordsState.phraseWordToVerifyIndex
        )
    }

    private fun navigatePreviousWordDuringKeyRecovery() {
        if (state.confirmPhraseWordsState.phraseWordToVerifyIndex == 0) {
            return
        }

        handlePreviousWordNavigationDuringKeyRecovery()
    }

    private fun handlePreviousWordNavigationDuringKeyRecovery() {
        val decrementedWordIndex = state.confirmPhraseWordsState.phraseWordToVerifyIndex - 1
        val wordInputToDisplay = state.confirmPhraseWordsState.words[decrementedWordIndex]

        state = state.copy(
            confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                phraseWordToVerifyIndex = decrementedWordIndex,
                wordInput = wordInputToDisplay
            )
        )
    }

    fun handlePastedPhrase(pastedPhrase: String) {
        handlePhrasePastedDuringKeyRecovery(pastedPhrase = pastedPhrase)
    }

    private fun handlePhrasePastedDuringKeyRecovery(pastedPhrase: String) {
        userEnteredFullPhraseDuringKeyRecovery(
            phrase = pastedPhrase,
            inputMethod = PhraseInputMethod.PASTED
        )
    }
    //endregion

    //region MANUAL ENTRY PHRASE WORD SUBMISSION LOGIC
    private fun handleWordSubmitted(errorMessage: String) {
        val word = state.confirmPhraseWordsState.wordInput.trim()
        val index = state.confirmPhraseWordsState.phraseWordToVerifyIndex

        if (word.isEmpty()) {
            state = state.copy(showToast = Resource.Success(errorMessage))
            return
        } else {
            addWordInputToPhraseWordsDuringKeyRecovery(word = word, currentWordIndex = index)
        }
    }

    private fun addWordInputToPhraseWordsDuringKeyRecovery(word: String, currentWordIndex: Int) {
        val words = state.confirmPhraseWordsState.words.toMutableList()

        val indexedPhraseWord = IndexedPhraseWord(
            wordIndex = currentWordIndex,
            wordValue = word
        )

        //Check to see if we are dealing with a previously submitted word
        if (words.size > currentWordIndex) {
            val updatedState = PhraseEntryUtil.handlePreviouslySubmittedWordDuringOrgRecovery(
                word = indexedPhraseWord,
                submittedWords = words,
                state = state
            )

            if (updatedState != null) {
                state = updatedState
            } else {
                state = state.copy(inputMethod = PhraseInputMethod.MANUAL)
                recoverKeyFailure()
            }
            return
        }

        //If we are not dealing with a previously submitted word,
        // then accept the new word and move forward
        state = state.copy(
            confirmPhraseWordsState = PhraseEntryUtil.addWordToPhraseWords(
                word = indexedPhraseWord,
                phraseWords = words,
                state = state.confirmPhraseWordsState
            )
        )

        if (state.confirmPhraseWordsState.allWordsEntered) {
            handleAllWordsEnteredDuringKeyRecovery()
        }
    }

    private fun handleAllWordsEnteredDuringKeyRecovery() {
        val phrase =
            PhraseEntryUtil.assemblePhraseFromWords(words = state.confirmPhraseWordsState.words)

        userEnteredFullPhraseDuringKeyRecovery(
            phrase = phrase,
            inputMethod = PhraseInputMethod.MANUAL
        )
    }

    private fun userEnteredFullPhraseDuringKeyRecovery(
        phrase: String,
        inputMethod: PhraseInputMethod
    ) {
        state = state.copy(inputMethod = inputMethod)
        if (!phraseValidator.isPhraseValid(phrase)) {
            recoverKeyFailure()
            return
        }

        state = state.copy(
            keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(
                KeyRecoveryFlowStep.ALL_SET_STEP
            ),
            finalizeKeyFlow = Resource.Success(Unit),
            userInputtedPhrase = phrase
        )
    }
    //endregion

    fun recoverKeyFailure() {
        state = state.copy(
            userInputtedPhrase = "",
            keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            keyRecoveryManualEntryError = Resource.Error(),
            confirmPhraseWordsState = ConfirmPhraseWordsState()
        )

    }


    fun exitPhraseFlow() {
        state = state.copy(onExit = Resource.Success(Unit))
    }

    fun retryKeyRecoveryFromPhrase() {
        state = state.copy(
            keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ALL_SET_STEP),
            finalizeKeyFlow = Resource.Loading()
        )
    }

    //Reset
    fun resetOnExit() {
        state = state.copy(onExit = Resource.Uninitialized)
    }

    fun resetShowToast() {
        state = state.copy(showToast = Resource.Uninitialized)
    }
}