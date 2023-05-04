package com.censocustody.android.presentation.org_key_recovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.censocustody.android.common.Resource
import com.censocustody.android.common.ui.generateQRCode
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

    //region Entry Step Actions
    fun setPhraseEntry() {
        state = state.copy(
            recoveryStep = RecoveryStep.PHRASE_ENTRY,
            keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP)
        )
    }

    fun setScanQRCode() {
        state =
            state.copy(recoveryStep = RecoveryStep.SCAN_QR, scanQRCodeResult = Resource.Loading())
    }
    //endregion

    //region Key Recovery Navigation + Phrase Flow Logic
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
    //endregion

    //region User Phrase Entry Actions
    fun phraseEntryAction(phraseEntryAction: PhraseEntryAction) {
        when (phraseEntryAction) {
            is PhraseEntryAction.NavigateNextWord -> {
                navigateNextWordDuringKeyRecovery()
            }
            is PhraseEntryAction.NavigatePreviousWord -> {
                navigatePreviousWordDuringKeyRecovery()
            }
            is PhraseEntryAction.PastePhrase -> {
                handlePhrasePastedDuringKeyRecovery(pastedPhrase = phraseEntryAction.phrase)
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

    private fun handlePhrasePastedDuringKeyRecovery(pastedPhrase: String) {
        userEnteredFullPhraseDuringKeyRecovery(
            phrase = pastedPhrase,
            inputMethod = PhraseInputMethod.PASTED
        )
    }
    //endregion

    //region Manual Entry Phrase Word Submission Logic
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

        //If phrase is valid
        recoverKeysFromPhrase()
    }
    //endregion

    //region Recovery Process major steps
    fun recoverKeysFromPhrase() {
        if (state.userInputtedPhrase.isNotEmpty() || state.userInputtedPhrase.isNotBlank()) {

            //TODO: Implement actual key recovery here
            val recoveredKey: HashMap<String, String> = hashMapOf(Pair("recoveryKey", "keyData"))

            state = state.copy(
                recoveredKey = recoveredKey,
                recoveryProcess = state.recoveryProcess.copy(keyRecovered = true)
            )

            checkRecoveryProgress()
        } else {
            return
        }
    }

    fun checkRecoveryProgress() {
        val keyRecovered = state.recoveryProcess.keyRecovered
        val qrCodeScanned = state.recoveryProcess.qrCodeScanned

        if (keyRecovered && qrCodeScanned) {
            mockSignDataForRecovery()
        }

        if (!qrCodeScanned && keyRecovered) {
            setScanQRCode()
        }

        if (!keyRecovered && qrCodeScanned) {
            setPhraseEntry()
        }

        if (!keyRecovered && !qrCodeScanned) {
            //phrase was not recovered and qr code was not scanned
            state = state.copy(recoveryStep = RecoveryStep.RECOVERY_ERROR)
        }
    }

    fun mockSignDataForRecovery() {
        val recoveredKeyData = state.recoveredKey["recoveryKey"]
        val dataToSign = state.scannedData
        val isDataToSignValid = dataToSign.isNotBlank() && dataToSign.isNotEmpty()
        if (recoveredKeyData != null &&
            recoveredKeyData.isNotBlank() &&
            recoveredKeyData.isNotEmpty() && isDataToSignValid
        ) {
            //Stubbed out signing
            //TODO: IMPLEMENT SIGNING HERE
            val signedData: String = recoveredKeyData + dataToSign

            //Create QR code with signed data
            createQRCodeForRecoveryScanning(signedData)
        } else {
            //recoveredKeyData was invalid when attempting to sign data
            state = state.copy(recoveryStep = RecoveryStep.RECOVERY_ERROR)
        }
    }

    fun createQRCodeForRecoveryScanning(signedData: String) {
        val qrCodeBitmap = signedData.generateQRCode()

        state = state.copy(qrCodeBitmap = qrCodeBitmap, recoveryStep = RecoveryStep.DISPLAY_QR)
    }


    //endregion

    //region QR Code Scanning
    fun receivedQRCodeData(uri: String?) {
        if (state.scanQRCodeResult is Resource.Loading && !uri.isNullOrEmpty()) {

            state = state.copy(
                scanQRCodeResult = Resource.Success(uri),
                scannedData = uri,
                recoveryProcess = state.recoveryProcess.copy(qrCodeScanned = true)
            )

            checkRecoveryProgress()
        }
    }

    fun failedToScan(exception: Exception) {
        state = state.copy(scanQRCodeResult = Resource.Error(exception = exception))
    }

    fun retryScan() {
        state = state.copy(
            scanQRCodeResult = Resource.Loading()
        )
    }
    //endregion

    fun recoverKeyFailure() {
        state = state.copy(
            userInputtedPhrase = "",
            keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            recoveryStep = RecoveryStep.RECOVERY_ERROR,
            confirmPhraseWordsState = ConfirmPhraseWordsState()
        )

    }


    fun exitPhraseEntryStep() {
        state = state.copy(recoveryStep = RecoveryStep.RECOVERY_START)
    }

    fun exitScanQRStep() {
        state = state.copy(
            recoveryStep = RecoveryStep.RECOVERY_START,
            scanQRCodeResult = Resource.Uninitialized
        )
    }

    fun retryKeyRecoveryFromPhrase() {
        state = state.copy(
            keyRecoveryFlowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ALL_SET_STEP),
            finalizeKeyFlow = Resource.Loading()
        )
    }

    //Reset state
    fun resetShowToast() {
        state = state.copy(showToast = Resource.Uninitialized)
    }

    fun restartRecoveryProcess() {
        //Reset to initial state
        state = OrgKeyRecoveryState()
    }
}