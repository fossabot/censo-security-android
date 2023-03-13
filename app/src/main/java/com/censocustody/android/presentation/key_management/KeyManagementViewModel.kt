package com.censocustody.android.presentation.key_management

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.censocustody.android.common.*
import com.censocustody.android.data.*
import com.censocustody.android.data.models.IndexedPhraseWord
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.presentation.key_management.KeyManagementState.Companion.NO_PHRASE_ERROR
import com.censocustody.android.presentation.key_management.KeyManagementState.Companion.FIRST_WORD_INDEX
import com.censocustody.android.presentation.key_management.flows.*
import kotlinx.coroutines.*

@HiltViewModel
class KeyManagementViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val phraseValidator: PhraseValidator
) : ViewModel() {

    companion object {
        const val CLIPBOARD_LABEL_PHRASE = "Phrase"
    }

    var state by mutableStateOf(KeyManagementState())
        private set

    //region VM SETUP
    fun onStart(keyManagementInitialData: KeyManagementInitialData) {
        if (state.initialData == null) {
            setArgsToState(keyManagementInitialData)
        }
    }

    private fun setArgsToState(keyManagementInitialData: KeyManagementInitialData) {
        viewModelScope.launch {
            when (keyManagementInitialData.flow) {
                KeyManagementFlow.KEY_CREATION, KeyManagementFlow.UNINITIALIZED -> {
                    val phrase = keyRepository.generatePhrase()
                    state = state.copy(
                        keyGeneratedPhrase = phrase
                    )
                    state = state.copy(
                        initialData = keyManagementInitialData,
                        keyManagementFlowStep =
                        KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
                        keyManagementFlow = keyManagementInitialData.flow
                    )
                }
                KeyManagementFlow.KEY_RECOVERY -> {
                    state = state.copy(
                        initialData = keyManagementInitialData,
                        keyManagementFlowStep =
                        KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
                        keyManagementFlow = keyManagementInitialData.flow
                    )
                }
            }
        }
    }

    fun onResume() {
        val flowStep = state.keyManagementFlowStep
        val userIsOnPhraseCopiedCreationStep =
            flowStep is KeyManagementFlowStep.CreationFlow && flowStep.step == KeyCreationFlowStep.PHRASE_COPIED_STEP
        if (userIsOnPhraseCopiedCreationStep) {
            state =
                state.copy(
                    keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.PHRASE_SAVED_STEP)
                )
        }
    }
    //endregion

    //region API CALLS
    //todo: no longer doing key creation with a phrase
//    private suspend fun attemptAddWalletSigner(walletSignerBody: List<WalletSigner>): Resource<Signers> {
//        val walletSignerResource =
//            userRepository.addWalletSigner(walletSignerBody)
//
//        state = state.copy(finalizeKeyFlow = walletSignerResource)
//        return walletSignerResource
//    }
    //endregion

    //region RETRY
    fun retryKeyCreationFromPhrase() {
        viewModelScope.launch {
            userVerifiedFullPhraseDuringKeyCreation()
        }
    }

    fun retryKeyRecoveryFromPhrase() {
        state = state.copy(
            keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ALL_SET_STEP),
            finalizeKeyFlow = Resource.Loading()
        )

        triggerBioPrompt(inputMethod = PhraseInputMethod.PASTED)
    }
    //endregion

    //region CORE ACTIONS
    fun triggerBioPrompt(inputMethod: PhraseInputMethod? = null) {
        viewModelScope.launch {
            val bioPromptData = BioPromptData(BioPromptReason.SAVE_V3_ROOT_SEED)

            state = when (state.keyManagementFlow) {
                KeyManagementFlow.KEY_CREATION -> {
                    state.copy(
                        triggerBioPrompt = Resource.Success(Unit),
                        bioPromptData = bioPromptData
                    )
                }
                KeyManagementFlow.KEY_RECOVERY -> {
                    state.copy(
                        triggerBioPrompt = Resource.Success(Unit),
                        bioPromptData = bioPromptData,
                        inputMethod = inputMethod ?: PhraseInputMethod.MANUAL
                    )
                }
                KeyManagementFlow.UNINITIALIZED -> {
                    return@launch
                }
            }

        }
    }

    private fun triggerDeviceSignatureRetrieval() {
        viewModelScope.launch {
            state =
                state.copy(
                    triggerBioPrompt = Resource.Success(Unit),
                    bioPromptData = BioPromptData(BioPromptReason.RETRIEVE_DEVICE_SIGNATURE)
                )
        }
    }

    fun biometryApproved() {
        if (state.bioPromptData.bioPromptReason == BioPromptReason.SAVE_V3_ROOT_SEED) {
            saveRootSeed()
        }

        if (state.bioPromptData.bioPromptReason == BioPromptReason.RETRIEVE_DEVICE_SIGNATURE) {
            uploadKeys()
        }
    }

    fun biometryFailed() {
        state = state.copy(finalizeKeyFlow = Resource.Error())
    }

    fun saveRootSeed() {
        viewModelScope.launch {
            try {
                val phrase = when (state.keyManagementFlow) {
                    KeyManagementFlow.KEY_CREATION -> {
                        val safePhrase = state.keyGeneratedPhrase
                        if (safePhrase == null) {
                            state = retrieveInitialKeyCreationState()
                            return@launch
                        } else {
                            safePhrase
                        }
                    }
                    KeyManagementFlow.KEY_RECOVERY -> {
                        state.userInputtedPhrase
                    }
                    else -> {
                        state = retrieveInitialKeyCreationState()
                        return@launch
                    }
                }

                keyRepository.saveV3RootKey(
                    Mnemonics.MnemonicCode(phrase = phrase)
                )

                val walletSigners =
                    keyRepository.saveV3PublicKeys(
                        rootSeed = Mnemonics.MnemonicCode(phrase = phrase).toSeed()
                    )

                finalizeKeyCreationOrRecovery(walletSigners = walletSigners)
            } catch (e: Exception) {
                when (state.keyManagementFlow) {
                    KeyManagementFlow.KEY_CREATION -> {
                        state = state.copy(
                            finalizeKeyFlow = Resource.Error(exception = e)
                        )
                    }
                    KeyManagementFlow.KEY_RECOVERY -> {
                        recoverKeyFailure()
                    }
                    KeyManagementFlow.UNINITIALIZED -> {}
                }
            }
        }
    }

    private fun finalizeKeyCreationOrRecovery(walletSigners: List<WalletSigner>) {
        when (state.keyManagementFlow) {
            KeyManagementFlow.KEY_CREATION -> createAndSaveKey()
            KeyManagementFlow.KEY_RECOVERY -> recoverKey(walletSigners)
            KeyManagementFlow.UNINITIALIZED -> {}
        }
    }

    fun createAndSaveKey() {
        try {
            //todo: remove this flow now that key creation no longer works with a phrase
//            val addWalletSignerResource = attemptAddWalletSigner(localKeys)
//
//            if (addWalletSignerResource is Resource.Success) {
//                state =
//                    state.copy(
//                        keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(
//                            KeyCreationFlowStep.ALL_SET_STEP
//                        ),
//                        finalizeKeyFlow = Resource.Success(addWalletSignerResource.data),
//                        keyGeneratedPhrase = null
//                    )
//            } else if (addWalletSignerResource is Resource.Error) {
//                state = state.copy(
//                    finalizeKeyFlow = addWalletSignerResource
//                )
//            }
        } catch (e: Exception) {
            state = state.copy(
                finalizeKeyFlow = Resource.Error(exception = e)
            )
        }
    }

    fun recoverKey(localKeys: List<WalletSigner>) {
        try {
            //need to update wallet signer call
            val keysNotSavedOnBackend =
                state.initialData?.verifyUserDetails?.determineKeysUserNeedsToUpload(localKeys)

            if (keysNotSavedOnBackend?.isNotEmpty() == true) {
                state = state.copy(walletSignersToAdd = localKeys)
                triggerDeviceSignatureRetrieval()
            } else {
                //no API call needed, move on
                state = state.copy(finalizeKeyFlow = Resource.Success(null))
            }
        } catch (e: Exception) {
            recoverKeyFailure()
        }
    }

    private fun uploadKeys() {
        viewModelScope.launch {
            val walletSignerResource =
                userRepository.addWalletSigner(state.walletSignersToAdd)

            if (walletSignerResource is Resource.Success) {
                state =
                    state.copy(
                        finalizeKeyFlow = Resource.Success(walletSignerResource.data),
                    )
            } else if (walletSignerResource is Resource.Error) {
                state = state.copy(
                    finalizeKeyFlow = walletSignerResource
                )
            }
        }
    }
    //endregion

    //region PHRASE FLOW NAVIGATION
    fun exitPhraseFlow() {
        state = state.copy(goToAccount = Resource.Success(true))
    }

    fun retrieveInitialKeyCreationState(): KeyManagementState {
        return state.copy(
            keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            showToast = Resource.Success(NO_PHRASE_ERROR)
        )
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
            PhraseFlowAction.LaunchManualKeyCreation -> {
                state = state.copy(
                    keyManagementFlowStep =
                    KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.WRITE_WORD_STEP),
                    wordIndexForDisplay = FIRST_WORD_INDEX
                )
            }
            is PhraseFlowAction.ChangeCreationFlowStep -> {
                state =
                    state.copy(
                        keyManagementFlowStep =
                        KeyManagementFlowStep.CreationFlow(phraseFlowAction.phraseVerificationFlowStep)
                    )
            }
            is PhraseFlowAction.ChangeRecoveryFlowStep -> {
                state =
                    state.copy(
                        keyManagementFlowStep =
                        KeyManagementFlowStep.RecoveryFlow(phraseFlowAction.phraseGenerationFlowStep)
                    )
            }
        }
    }

    fun keyCreationNavigateForward() {
        if (state.keyManagementFlowStep !is KeyManagementFlowStep.CreationFlow) {
            return
        }

        val creationFlowStep: KeyCreationFlowStep =
            (state.keyManagementFlowStep as KeyManagementFlowStep.CreationFlow).step

        val nextStep = moveUserToNextCreationScreen(creationFlowStep)

        if (nextStep == KeyCreationFlowStep.VERIFY_WORDS_STEP) {
            setupConfirmPhraseWordsStateForCreationFlow()
        } else {
            state = state.copy(keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(nextStep))
        }
    }

    fun keyCreationNavigateBackward() {
        if (state.keyManagementFlowStep !is KeyManagementFlowStep.CreationFlow) {
            return
        }

        val creationFlowStep: KeyCreationFlowStep =
            (state.keyManagementFlowStep as KeyManagementFlowStep.CreationFlow).step

        val nextStep =
            moveUserToPreviousCreationScreen(creationFlowStep, state.finalizeKeyFlow)

        state = if (nextStep == KeyCreationFlowStep.VERIFY_WORDS_STEP) {
            state.copy(
                keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(nextStep),
                confirmPhraseWordsState = ConfirmPhraseWordsState()
            )
        } else {
            state.copy(keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(nextStep))
        }
    }

    fun keyRecoveryNavigateForward() {
        if (state.keyManagementFlowStep !is KeyManagementFlowStep.RecoveryFlow) {
            return
        }

        val recoveryFlowStep: KeyRecoveryFlowStep =
            (state.keyManagementFlowStep as KeyManagementFlowStep.RecoveryFlow).step

        val nextStep = moveUserToNextRecoveryScreen(recoveryFlowStep)

        if (nextStep == KeyRecoveryFlowStep.VERIFY_WORDS_STEP) {
            setupConfirmPhraseWordsStateForRecoveryFlow()
        } else {
            state = state.copy(keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(nextStep))
        }
    }

    fun keyRecoveryNavigateBackward() {
        if (state.keyManagementFlowStep !is KeyManagementFlowStep.RecoveryFlow) {
            return
        }

        val recoveryFlowStep: KeyRecoveryFlowStep =
            (state.keyManagementFlowStep as KeyManagementFlowStep.RecoveryFlow).step

        val nextStep = moveUserToPreviousRecoveryScreen(recoveryFlowStep)

        state = state.copy(keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(nextStep))
    }
    //endregion

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

        //TODO: Slot in the autocomplete logic here
        if (state.confirmPhraseWordsState.isCreationKeyFlow) {
            verifyWordInputAgainstPhraseWordKeyCreation()
        }
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
        if (state.keyManagementFlow == KeyManagementFlow.KEY_CREATION) {
            handlePhrasePastedDuringKeyCreation(pastedPhrase = pastedPhrase)
        } else if (state.keyManagementFlow == KeyManagementFlow.KEY_RECOVERY) {
            handlePhrasePastedDuringKeyRecovery(pastedPhrase = pastedPhrase)
        }
    }

    private fun handlePhrasePastedDuringKeyCreation(pastedPhrase: String) {
        if (pastedPhrase == state.confirmPhraseWordsState.pastedPhrase) {
            return
        }

        state = state.copy(
            inputMethod = PhraseInputMethod.PASTED,
            confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                pastedPhrase = pastedPhrase
            )
        )
        try {
            if (phraseValidator.isPhraseValid(pastedPhrase) && pastedPhrase == state.keyGeneratedPhrase) {
                userVerifiedFullPhraseDuringKeyCreation()
            } else {
                pastedPhraseVerificationFailureDuringKeyCreation()
            }
        } catch (e: Exception) {
            pastedPhraseVerificationFailureDuringKeyCreation()
        }
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

            if (state.confirmPhraseWordsState.isCreationKeyFlow) {
                verifyWordSubmittedAgainstPhraseWordKeyCreation()
            } else {
                addWordInputToPhraseWordsDuringKeyRecovery(word = word, currentWordIndex = index)
            }
        }
    }

    private fun verifyWordInputAgainstPhraseWordKeyCreation() {
        val phraseWord = state.confirmPhraseWordsState.phraseWordToVerify
        val wordInput = state.confirmPhraseWordsState.wordInput

        if (phraseWord == wordInput) {
            try {
                handleWordVerifiedDuringKeyCreation()
            } catch (e: Exception) {
                retrieveInitialKeyCreationState()
            }
        }
    }

    private fun verifyWordSubmittedAgainstPhraseWordKeyCreation() {
        val phraseWord = state.confirmPhraseWordsState.phraseWordToVerify
        val wordInput = state.confirmPhraseWordsState.wordInput

        if (phraseWord == wordInput) {
            try {
                handleWordVerifiedDuringKeyCreation()
            } catch (e: Exception) {
                retrieveInitialKeyCreationState()
            }
        } else {
            state = state.copy(
                confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                    errorEnabled = true
                )
            )
        }
    }

    private fun handleWordVerifiedDuringKeyCreation() {
        state = state.copy(
            confirmPhraseWordsState = PhraseEntryUtil.incrementWordsVerified(
                state = state.confirmPhraseWordsState
            )
        )
        val wordsVerified = state.confirmPhraseWordsState.wordsVerified
        val wordIndex = state.confirmPhraseWordsState.phraseWordToVerifyIndex

        val allWordsVerified = PhraseEntryUtil.checkIfAllPhraseWordsAreVerifiedDuringKeyCreation(
            wordsVerified = wordsVerified,
            wordIndex = wordIndex
        )

        if (allWordsVerified) {
            userVerifiedFullPhraseDuringKeyCreation()
        } else {
            //Update the state with the next word to verify
            val phrase =
                state.keyGeneratedPhrase ?: throw Exception(PhraseException.NULL_PHRASE_IN_STATE)
            val nextIndex = wordIndex + 1

            val nextWordToVerify =
                PhraseEntryUtil.getPhraseWordAtIndex(phrase = phrase, index = nextIndex)

            state = state.copy(
                confirmPhraseWordsState = PhraseEntryUtil.updateConfirmPhraseWordsStateWithNextWordToVerify(
                    nextWordToVerify = nextWordToVerify,
                    confirmPhraseWordsState = state.confirmPhraseWordsState
                )
            )
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
            val updatedState = PhraseEntryUtil.handlePreviouslySubmittedWord(
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
    //endregion

    //region MANUAL ENTRY ALL WORDS SUBMITTED LOGIC
    private fun userVerifiedFullPhraseDuringKeyCreation() {
        handleUserFinishedPhraseVerificationDuringKeyCreation()

        state = state.copy(
            keyManagementFlowStep =
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ALL_SET_STEP),
            finalizeKeyFlow = Resource.Loading(),
        )

        triggerBioPrompt()
    }

    private fun handleUserFinishedPhraseVerificationDuringKeyCreation() {
        when (state.inputMethod) {
            PhraseInputMethod.PASTED -> {}
            PhraseInputMethod.MANUAL -> {
                resetConfirmPhraseWordsState()
            }
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

        val keyMatchesBackendPublic =
            keyRepository.validateUserEnteredPhraseAgainstBackendKeys(phrase, state.initialData?.verifyUserDetails)

        if(!keyMatchesBackendPublic) {
            recoverKeyFailure()
            return
        }

        state = state.copy(
            keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(
                KeyRecoveryFlowStep.ALL_SET_STEP
            ),
            finalizeKeyFlow = Resource.Loading(),
            userInputtedPhrase = phrase
        )
        triggerBioPrompt(inputMethod = inputMethod)
    }
    //endregion

    //region MANUAL ENTRY STATE SETUP
    fun setupConfirmPhraseWordsStateForCreationFlow() {
        val confirmPhraseWordsState =
            PhraseEntryUtil.generateConfirmPhraseWordsState(state = state)
        if (confirmPhraseWordsState != null) {
            state = state.copy(
                keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.VERIFY_WORDS_STEP),
                confirmPhraseWordsState = confirmPhraseWordsState
            )
        } else {
            retrieveInitialKeyCreationState()
        }
    }

    fun setupConfirmPhraseWordsStateForRecoveryFlow() {
        val confirmPhraseWordsState =
            PhraseEntryUtil.generateConfirmPhraseWordsState(state = state)
        if (confirmPhraseWordsState != null) {
            state = state.copy(
                keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.VERIFY_WORDS_STEP),
                confirmPhraseWordsState = confirmPhraseWordsState
            )
        }
    }
    //endregion

    //region FAILURE HANDLING
    private fun pastedPhraseVerificationFailureDuringKeyCreation() {
        state = state.copy(
            keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP),
        )
    }

    fun recoverKeyFailure() {
        state = if (state.inputMethod == PhraseInputMethod.PASTED) {
            state.copy(
                keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP)
            )
        } else {
            state.copy(
                userInputtedPhrase = "",
                keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
                keyRecoveryManualEntryError = Resource.Error(),
                confirmPhraseWordsState = ConfirmPhraseWordsState()
            )
        }
    }
    //endregion

    //region RESET RESOURCES
    fun resetGoToAccount() {
        state = state.copy(goToAccount = Resource.Uninitialized)
    }

    fun resetAddWalletSignersCall() {
        state = state.copy(finalizeKeyFlow = Resource.Uninitialized)
    }

    private fun resetConfirmPhraseWordsState() {
        state = state.copy(confirmPhraseWordsState = ConfirmPhraseWordsState())
    }

    fun resetShowToast() {
        state = state.copy(showToast = Resource.Uninitialized)
    }

    fun resetRecoverManualEntryError() {
        state = state.copy(keyRecoveryManualEntryError = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
    //endregion
}