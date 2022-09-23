package com.strikeprotocols.mobile.presentation.key_management

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.BIO_KEY_NAME
import com.strikeprotocols.mobile.data.models.IndexedPhraseWord
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.NO_PHRASE_ERROR
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.FIRST_WORD_INDEX
import com.strikeprotocols.mobile.presentation.key_management.flows.*
import kotlinx.coroutines.*
import javax.crypto.Cipher

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
                KeyManagementFlow.KEY_REGENERATION -> {
                    state = state.copy(
                        initialData = keyManagementInitialData,
                        keyManagementFlowStep =
                        KeyManagementFlowStep.RegenerationFlow(KeyRegenerationFlowStep.ALL_SET_STEP),
                        keyManagementFlow = keyManagementInitialData.flow
                    )
                    regenerateData()
                }
                KeyManagementFlow.KEY_MIGRATION -> {
                    state = state.copy(
                        initialData = keyManagementInitialData,
                        keyManagementFlowStep =
                        KeyManagementFlowStep.MigrationFlow(KeyMigrationFlowStep.ALL_SET_STEP),
                        keyManagementFlow = keyManagementInitialData.flow,
                        finalizeKeyFlow = Resource.Loading(),
                    )
                    triggerBioPrompt()
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
    private suspend fun attemptAddWalletSigner(walletSignerBody: WalletSigner): Resource<WalletSigner> {
        val walletSignerResource =
            userRepository.addWalletSigner(walletSignerBody)

        state = state.copy(finalizeKeyFlow = walletSignerResource)
        return walletSignerResource
    }

    private fun regenerateData() {
        state = state.copy(finalizeKeyFlow = Resource.Loading())
        viewModelScope.launch {
            val walletSignerResource = keyRepository.regenerateDataAndUploadToBackend()
            state = state.copy(finalizeKeyFlow = walletSignerResource)
        }
    }
    //endregion

    //region RETRY
    fun retryRegenerateData() {
        //We use resetAddWalletSignersCall() to reset finalizeKeyFlow
        // and prevent the UI from constantly displaying an error dialog
        resetAddWalletSignersCall()
        regenerateData()
    }

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

    fun retryKeyMigration() {
        state = state.copy(
            keyManagementFlowStep =
            KeyManagementFlowStep.MigrationFlow(KeyMigrationFlowStep.ALL_SET_STEP),
            finalizeKeyFlow = Resource.Loading(),
        )
        triggerBioPrompt()
    }
    //endregion

    //region CORE ACTIONS
    fun triggerBioPrompt(inputMethod: PhraseInputMethod? = null) {
        viewModelScope.launch {
            val cipher = keyRepository.getCipherForEncryption(BIO_KEY_NAME)
            if (cipher != null) {
                state = when (state.keyManagementFlow) {
                    KeyManagementFlow.KEY_CREATION -> {
                        state.copy(
                            triggerBioPrompt = Resource.Success(cipher),
                            bioPromptReason = BioPromptReason.CREATE_KEY
                        )
                    }
                    KeyManagementFlow.KEY_RECOVERY -> {
                        state.copy(
                            triggerBioPrompt = Resource.Success(cipher),
                            bioPromptReason = BioPromptReason.RECOVER_KEY,
                            inputMethod = inputMethod ?: PhraseInputMethod.MANUAL
                        )
                    }
                    KeyManagementFlow.KEY_MIGRATION -> {
                        state.copy(
                            triggerBioPrompt = Resource.Success(cipher),
                            bioPromptReason = BioPromptReason.MIGRATE_BIOMETRIC_KEY
                        )
                    }
                    KeyManagementFlow.KEY_REGENERATION,
                    KeyManagementFlow.UNINITIALIZED -> {
                        return@launch
                    }
                }
            }
        }
    }

    fun biometryApproved(cipher: Cipher) {
        when (state.bioPromptReason) {
            BioPromptReason.CREATE_KEY -> createAndSaveKey(cipher)
            BioPromptReason.RECOVER_KEY -> recoverKey(cipher)
            BioPromptReason.MIGRATE_BIOMETRIC_KEY -> migrateKeyData(cipher)
            else -> {}
        }
    }

    fun biometryFailed() {
        state = state.copy(finalizeKeyFlow = Resource.Error())
    }

    fun createAndSaveKey(cipher: Cipher) {
        val phrase = state.keyGeneratedPhrase

        if (phrase.isNullOrEmpty()) {
            state = retrieveInitialKeyCreationState()
            return
        }

        viewModelScope.launch {
            try {
                val walletSigner = keyRepository.generateInitialAuthDataAndSaveKeyToUser(
                    Mnemonics.MnemonicCode(phrase = phrase),
                    cipher
                )
                state = state.copy(walletSignerToAdd = walletSigner)

                val addWalletSignerResource =
                    attemptAddWalletSigner(walletSigner)

                if (addWalletSignerResource is Resource.Success) {
                    state =
                        state.copy(
                            keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(
                                KeyCreationFlowStep.ALL_SET_STEP
                            ),
                            finalizeKeyFlow = Resource.Success(addWalletSignerResource.data),
                            keyGeneratedPhrase = null
                        )
                } else if (addWalletSignerResource is Resource.Error) {
                    state = state.copy(
                        finalizeKeyFlow = addWalletSignerResource
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    finalizeKeyFlow = Resource.Error(exception = e)
                )
            }
        }
    }

    fun recoverKey(cipher: Cipher) {
        viewModelScope.launch {
            val verifyUser = state.initialData?.verifyUserDetails
            val publicKey = verifyUser?.firstPublicKey()

            if (verifyUser != null && publicKey != null) {
                try {
                    keyRepository.regenerateAuthDataAndSaveKeyToUser(
                        phrase = state.userInputtedPhrase,
                        backendPublicKey = publicKey,
                        cipher = cipher
                    )
                    state = state.copy(finalizeKeyFlow = Resource.Success(null))
                } catch (e: Exception) {
                    recoverKeyFailure()
                }
            } else {
                recoverKeyFailure()
            }
        }
    }

    private fun migrateKeyData(cipher: Cipher) {
        viewModelScope.launch {
            state = try {
                keyRepository.migrateOldDataToBiometryProtectedStorage(cipher)
                state.copy(finalizeKeyFlow = Resource.Success(null))
            } catch (e: Exception) {
                state.copy(finalizeKeyFlow = Resource.Error(exception = e))
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

    fun keyRegenerationNavigateForward() {
        if (state.keyManagementFlowStep !is KeyManagementFlowStep.RegenerationFlow) {
            return
        }

        val regenerationFlowStep: KeyRegenerationFlowStep =
            (state.keyManagementFlowStep as KeyManagementFlowStep.RegenerationFlow).step

        val nextStep = moveToNextRegenerationScreen(regenerationFlowStep)

        state = state.copy(keyManagementFlowStep = KeyManagementFlowStep.RegenerationFlow(nextStep))
    }

    fun keyMigrationNavigateForward() {
        if (state.keyManagementFlowStep !is KeyManagementFlowStep.MigrationFlow) {
            return
        }

        val regenerationFlowStep: KeyMigrationFlowStep =
            (state.keyManagementFlowStep as KeyManagementFlowStep.MigrationFlow).step

        val nextStep = moveToNextMigrationScreen(regenerationFlowStep)

        state = state.copy(keyManagementFlowStep = KeyManagementFlowStep.MigrationFlow(nextStep))
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
                //TODO: Test the state resetting and flow handling when this occurs,
                // we might need to reset more state here
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