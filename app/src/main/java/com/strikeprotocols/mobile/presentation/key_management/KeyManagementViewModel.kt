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

        const val FIRST_WORD_INDEX = 0
        const val LAST_WORD_INDEX = 23

        const val LAST_WORD_RANGE_SET_INDEX = 20
        const val CHANGE_AMOUNT = 4
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
                        phrase = phrase
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
                    triggerBioPromptForMigration()
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
            verifiedPhraseSuccess()
        }
    }

    fun retryKeyRecoveryFromPhrase() {
        state = state.copy(
            keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ALL_SET_STEP),
            finalizeKeyFlow = Resource.Loading()
        )

        triggerBioPromptForRecover(inputMethod = PhraseInputMethod.PASTED)
    }

    fun retryKeyMigration() {
        state = state.copy(
            keyManagementFlowStep =
            KeyManagementFlowStep.MigrationFlow(KeyMigrationFlowStep.ALL_SET_STEP),
            finalizeKeyFlow = Resource.Loading(),
        )
        triggerBioPromptForMigration()
    }
    //endregion

    //region PHRASE NAVIGATION
    fun exitPhraseFlow() {
        state = state.copy(goToAccount = Resource.Success(true))
    }

    fun retrieveInitialKeyCreationState() : KeyManagementState {
        return state.copy(
            keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            showToast = Resource.Success(NO_PHRASE_ERROR)
        )
    }

    fun phraseFlowAction(phraseFlowAction: PhraseFlowAction) {
        when (phraseFlowAction) {
            is PhraseFlowAction.WordIndexChanged -> {
                handleWordIndexChanged(increasing = phraseFlowAction.increasing)
            }
            PhraseFlowAction.LaunchManualKeyCreation -> {
                state = state.copy(
                    keyManagementFlowStep =
                    KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.WRITE_WORD_STEP),
                    wordIndex = 0
                )
            }
            is PhraseFlowAction.ChangeCreationFlowStep -> {
                state =
                    state.copy(keyManagementFlowStep =
                    KeyManagementFlowStep.CreationFlow(phraseFlowAction.phraseVerificationFlowStep))
            }
            is PhraseFlowAction.ChangeRecoveryFlowStep -> {
                state =
                    state.copy(keyManagementFlowStep =
                    KeyManagementFlowStep.RecoveryFlow(phraseFlowAction.phraseGenerationFlowStep))
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
            try {
                val confirmPhraseWordsState = generateConfirmPhraseWordsStateForCreationFlow()
                state = state.copy(
                    keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.VERIFY_WORDS_STEP),
                    confirmPhraseWordsState = confirmPhraseWordsState
                )
            } catch (e: Exception) {
                retrieveInitialKeyCreationState()
            }
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
        if(state.keyManagementFlowStep !is KeyManagementFlowStep.RecoveryFlow) {
            return
        }

        val recoveryFlowStep : KeyRecoveryFlowStep =
            (state.keyManagementFlowStep as KeyManagementFlowStep.RecoveryFlow).step

        val nextStep = moveUserToNextRecoveryScreen(recoveryFlowStep)

        state = if(nextStep == KeyRecoveryFlowStep.VERIFY_WORDS_STEP) {
            val confirmPhraseWordsState = generateConfirmPhraseWordsStateForRecoveryFlow()
            state.copy(
                keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.VERIFY_WORDS_STEP),
                confirmPhraseWordsState = confirmPhraseWordsState
            )
        } else {
            state.copy(keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(nextStep))
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
        if(state.keyManagementFlowStep !is KeyManagementFlowStep.RegenerationFlow) {
            return
        }

        val regenerationFlowStep : KeyRegenerationFlowStep =
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

    //region PHRASE LOGIC
    fun updateWordInput(input: String) {
        state = state.copy(
            confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                wordInput = input.lowercase().trim()
            )
        )

        //TODO: Slot in the autocomplete logic here
        if (state.confirmPhraseWordsState.isCreationKeyFlow) {
            verifyWordInputAgainstPhraseWord()
        }
    }

    fun submitWordInput(errorMessage: String) {
        handleWordSubmitted(errorMessage = errorMessage)
    }

    fun navigatePreviousWord() {
        if (state.confirmPhraseWordsState.wordIndex == 0) {
            return
        }

        handlePreviousWordNavigationRecoveryFlow()
    }

    //Navigate to next word should also act as a submitWord
    fun navigateNextWord() {
        if (state.confirmPhraseWordsState.wordInput.isEmpty() || state.confirmPhraseWordsState.wordInput.isBlank()) {
            return
        }

        addWordInputToRecoveryPhraseWords(word = state.confirmPhraseWordsState.wordInput, index = state.confirmPhraseWordsState.wordIndex)
    }

    fun verifyPastedPhrase(pastedPhrase: String) {
        if (pastedPhrase == state.pastedPhrase) {
            return
        }

        viewModelScope.launch {
            state = state.copy(pastedPhrase = pastedPhrase)
            try {
                if (phraseValidator.isPhraseValid(pastedPhrase) && pastedPhrase == state.phrase) {
                    verifiedPhraseSuccess()
                } else {
                    verifiedPhraseFailure()
                }
            } catch (e: Exception) {
                verifiedPhraseFailure()
            }
        }
    }

    private fun verifiedPhraseSuccess() {
        viewModelScope.launch {
            state = state.copy(
                keyManagementFlowStep =
                KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ALL_SET_STEP),
                finalizeKeyFlow = Resource.Loading(),
            )

            triggerBioPromptForCreate()
        }
    }

    suspend fun triggerBioPromptForCreate() {
        val cipher = keyRepository.getCipherForEncryption(BIO_KEY_NAME)
        if(cipher != null) {
            state = state.copy(
                triggerBioPrompt = Resource.Success(cipher),
                bioPromptReason = BioPromptReason.CREATE
            )
        }
    }

    fun triggerBioPromptForRecover(inputMethod: PhraseInputMethod) {
        viewModelScope.launch {
            val cipher = keyRepository.getCipherForEncryption(BIO_KEY_NAME)
            if(cipher != null) {
                state = state.copy(
                    triggerBioPrompt = Resource.Success(cipher),
                    bioPromptReason = BioPromptReason.RECOVER,
                    inputMethod = inputMethod
                )
            }
        }
    }

    fun triggerBioPromptForMigration() {
        viewModelScope.launch {
            val cipher = keyRepository.getCipherForEncryption(BIO_KEY_NAME)
            state = state.copy(
                triggerBioPrompt = Resource.Success(cipher),
                bioPromptReason = BioPromptReason.MIGRATION
            )
        }
    }

    fun recoverKey(cipher: Cipher) {
        viewModelScope.launch {
            val verifyUser = state.initialData?.verifyUserDetails
            val publicKey = verifyUser?.firstPublicKey()

            if (verifyUser != null && publicKey != null) {
                try {
                    keyRepository.regenerateAuthDataAndSaveKeyToUser(
                        phrase = state.phrase ?: "",
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

    fun biometryApproved(cipher: Cipher) {
        when(state.bioPromptReason) {
            BioPromptReason.CREATE -> createAndSaveKey(cipher)
            BioPromptReason.RECOVER -> recoverKey(cipher)
            BioPromptReason.MIGRATION -> migrateKeyData(cipher)
            else -> {}
        }
    }

    fun biometryFailed() {
        state = state.copy(finalizeKeyFlow = Resource.Error())
    }

    fun createAndSaveKey(cipher: Cipher) {
        val phrase = state.phrase

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
                            phrase = null
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

    private fun verifiedPhraseFailure() {
        viewModelScope.launch {
            state = state.copy(
                keyManagementFlowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP),
            )
        }
    }

    //Only used in the recovery key setup
    fun verifyPhraseToRecoverKeyPair(pastedPhrase: String) {
        viewModelScope.launch {
            state = state.copy(
                keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ALL_SET_STEP),
                finalizeKeyFlow = Resource.Loading(),
                phrase = pastedPhrase
            )

            triggerBioPromptForRecover(inputMethod = PhraseInputMethod.PASTED)
        }
    }

    fun recoverKeyFailure() {
        state = if (state.inputMethod == PhraseInputMethod.PASTED) {
            state.copy(
                keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP)
            )
        } else {
            //This method call is not needed if we reset the same state object a few lines below
            resetConfirmPhraseWordsState()
            state.copy(
                phrase = "",
                keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
                keyRecoveryManualEntryError = Resource.Error(),
                confirmPhraseWordsState = ConfirmPhraseWordsState()
            )
        }
    }

    private fun handleWordIndexChanged(increasing: Boolean) {
        var wordIndex =
            if (increasing) {
                state.wordIndex + CHANGE_AMOUNT
            } else {
                state.wordIndex - CHANGE_AMOUNT
            }

        if (wordIndex > LAST_WORD_INDEX) {
            wordIndex = FIRST_WORD_INDEX
        } else if (wordIndex < FIRST_WORD_INDEX) {
            wordIndex = LAST_WORD_RANGE_SET_INDEX
        }

        state = state.copy(wordIndex = wordIndex)
    }

    fun generateConfirmPhraseWordsStateForCreationFlow(): ConfirmPhraseWordsState {
        val phrase = state.phrase ?: throw Exception(PhraseException.NULL_PHRASE_IN_STATE)

        val indexedPhraseWord = getPhraseWordAtIndex(phrase = phrase, index = FIRST_WORD_INDEX)//This does almost the same thing as getWordIndexFromWordSet method

        return ConfirmPhraseWordsState(
            phrase = phrase,
            phraseWordToVerify = indexedPhraseWord.wordValue,
            wordIndex = indexedPhraseWord.wordIndex,
            wordInput = "",
            errorEnabled = false,
            wordsVerified = 0,
            isCreationKeyFlow = true
        )
    }

    fun generateConfirmPhraseWordsStateForRecoveryFlow(): ConfirmPhraseWordsState {
        //The user will just have to enter 24 words and we just need to keep them and build them into a phrase
        return ConfirmPhraseWordsState(
            phrase = "",
            phraseWordToVerify = "",
            wordIndex = FIRST_WORD_INDEX,
            wordInput = "",
            errorEnabled = false,
            wordsVerified = 0,
            isCreationKeyFlow = false
        )
    }

    fun getPhraseWordAtIndex(phrase: String, index: Int): IndexedPhraseWord {
        if (!phrase.contains(" ") || phrase.split(" ").size < ConfirmPhraseWordsState.PHRASE_WORD_COUNT) {
            //Does not contain space
            //Does not equal 24 word phrase
            throw Exception(PhraseException.INVALID_PHRASE_IN_STATE)
        }

        val phraseWords = phrase.split(" ")
        val phraseWord = phraseWords[index]

        return IndexedPhraseWord(wordIndex = index, wordValue = phraseWord)
    }

    private fun handleWordSubmitted(errorMessage: String) {
        val word = state.confirmPhraseWordsState.wordInput.trim()
        val index = state.confirmPhraseWordsState.wordIndex

        if (word.isEmpty()) {
            state = state.copy(showToast = Resource.Success(errorMessage))
            return
        } else {

            if (state.confirmPhraseWordsState.isCreationKeyFlow) {
                verifyWordSubmittedAgainstPhraseWord()
            } else {
                addWordInputToRecoveryPhraseWords(word = word, index = index)
            }
        }
    }

    private fun verifyWordInputAgainstPhraseWord() {
        val phraseWord = state.confirmPhraseWordsState.phraseWordToVerify
        val wordInput = state.confirmPhraseWordsState.wordInput

        if (phraseWord == wordInput) {
            handleWordVerified()
        }
    }

    private fun verifyWordSubmittedAgainstPhraseWord() {
        val phraseWord = state.confirmPhraseWordsState.phraseWordToVerify
        val wordInput = state.confirmPhraseWordsState.wordInput

        if (phraseWord == wordInput) {
            handleWordVerified()
        } else {
            state = state.copy(
                confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                    errorEnabled = true
                )
            )
        }
    }

    private fun addWordInputToRecoveryPhraseWords(word: String, index: Int) {
        val words = state.confirmPhraseWordsState.words.toMutableList()

        //Check to see if we are dealing with a previously submitted word
        if (words.size > index) {
            if (words[index] == word) {
                getNextWordToDisplay(index = index)
                return
            } else {
                replacePreviouslySubmittedWordWithEditedInput(editedWordInput = word, index = index)
                return
            }
        }

        //If we are not dealing with a previously submitted word,
        // then accept the new word and move forward
        words.add(index = index, element = word)

        state = state.copy(
            confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                wordIndex = index + 1,//Increment by one
                wordInput = "",
                errorEnabled = false,
                words = words
            )
        )

        if (state.confirmPhraseWordsState.allWordsEntered) {
            assemblePhraseFromWords()
        }
    }

    //region Word navigation recovery flow
    private fun getNextWordToDisplay(index: Int) {
        val words = state.confirmPhraseWordsState.words.toMutableList()

        val wordInput = getNextWordInput(words, index)

        state = state.copy(
            confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                wordIndex = index + 1,//Increment by one
                wordInput = wordInput,
                errorEnabled = false
            )
        )
    }

    private fun replacePreviouslySubmittedWordWithEditedInput(editedWordInput: String, index: Int) {
        val words = state.confirmPhraseWordsState.words.toMutableList()
        //Replace the old word with the edited word
        words.set(index = index, element = editedWordInput)

        val wordInput = getNextWordInput(words, index)

        state = state.copy(
            confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                wordIndex = index + 1,//Increment by one
                wordInput = wordInput,
                errorEnabled = false,
                words = words
            )
        )
    }

    private fun getNextWordInput(words: List<String>, index: Int) =
        try {
            words[index + 1]
        } catch (e: IndexOutOfBoundsException) {
            ""
        }
    //endregion

    private fun handleWordVerified() {
        //If 23 words are verified and we are the last word Index this method gets call, we have verified the last word
        if (state.confirmPhraseWordsState.wordsVerified == ConfirmPhraseWordsState.PHRASE_WORD_SECOND_TO_LAST_INDEX
            && state.confirmPhraseWordsState.wordIndex + 1 == ConfirmPhraseWordsState.PHRASE_WORD_COUNT
        ) {
            handleUserVerifiedAllPhraseWords()
            return
        }

        val wordIndex = state.confirmPhraseWordsState.wordIndex
        val wordsVerified = state.confirmPhraseWordsState.wordsVerified

        val phrase = state.confirmPhraseWordsState.phrase
        val nextIndex = wordIndex + 1

        val nextWordToVerify = getPhraseWordAtIndex(phrase = phrase, index = nextIndex)

        if (wordIndex < wordsVerified) {
            //Need to decide if the user has to provide input for the next word or if they already verified the word
            if (wordIndex == wordsVerified - 1) {
                //The user needs to input the next word
                state = state.copy(
                    confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                        phraseWordToVerify = nextWordToVerify.wordValue,
                        wordIndex = nextWordToVerify.wordIndex,
                        wordInput = "",
                        errorEnabled = false
                    )
                )
            } else {
                state = state.copy(
                    confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                        phraseWordToVerify = nextWordToVerify.wordValue,
                        wordIndex = nextWordToVerify.wordIndex,
                        wordInput = nextWordToVerify.wordValue,
                        errorEnabled = false
                    )
                )
            }
            return
        }

        state = state.copy(
            confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
                phraseWordToVerify = nextWordToVerify.wordValue,
                wordIndex = nextWordToVerify.wordIndex,
                wordInput = "",
                wordsVerified = state.confirmPhraseWordsState.wordsVerified + 1,
                errorEnabled = false
            )
        )
    }

    private fun assemblePhraseFromWords() {
        viewModelScope.launch {
            val words = state.confirmPhraseWordsState.words

            val phraseBuilder = StringBuilder()
            for (word in words) {
                phraseBuilder.append("$word ")
            }

            val phrase = phraseBuilder.toString().trim()

            state = state.copy(
                keyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ALL_SET_STEP),
                finalizeKeyFlow = Resource.Loading(),
                phrase = phrase
            )
            triggerBioPromptForRecover(inputMethod = PhraseInputMethod.MANUAL)
        }
    }

    private fun handleUserVerifiedAllPhraseWords() {
        viewModelScope.launch {
            verifiedPhraseSuccess()
        }
        resetConfirmPhraseWordsState()
    }

    private fun handlePreviousWordNavigationRecoveryFlow() {
        val decrementedWordIndex = state.confirmPhraseWordsState.wordIndex - 1
        val wordInputToDisplay = state.confirmPhraseWordsState.words[decrementedWordIndex]

        state = state.copy(confirmPhraseWordsState = state.confirmPhraseWordsState.copy(
            wordIndex = decrementedWordIndex,
            wordInput = wordInputToDisplay
        ))
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