package com.strikeprotocols.mobile.presentation.key_management

import com.strikeprotocols.mobile.common.BioPromptReason
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.BioPromptData
import com.strikeprotocols.mobile.data.models.Signers
import com.strikeprotocols.mobile.presentation.key_management.flows.KeyCreationFlowStep
import com.strikeprotocols.mobile.presentation.key_management.flows.KeyManagementFlowStep
import javax.crypto.Cipher

data class KeyManagementState(
    //initial data + key management flow data
    val initialData: KeyManagementInitialData? = null,
    val keyManagementFlow: KeyManagementFlow = KeyManagementFlow.UNINITIALIZED,
    val keyManagementFlowStep: KeyManagementFlowStep = KeyManagementFlowStep.CreationFlow(
        KeyCreationFlowStep.UNINITIALIZED
    ),
    val inputMethod: PhraseInputMethod = PhraseInputMethod.MANUAL,

    //Phrase
    val keyGeneratedPhrase: String? = null,
    val userInputtedPhrase: String = "",
    val wordIndexForDisplay: Int = FIRST_WORD_INDEX,
    val confirmPhraseWordsState: ConfirmPhraseWordsState = ConfirmPhraseWordsState(),

    //API calls
    val finalizeKeyFlow: Resource<Signers> = Resource.Uninitialized,

    //Utility state
    val triggerBioPrompt: Resource<Cipher> = Resource.Uninitialized,
    val bioPromptData: BioPromptData = BioPromptData(BioPromptReason.UNINITIALIZED, false),
    val showToast: Resource<String> = Resource.Uninitialized,
    val goToAccount: Resource<Boolean> = Resource.Uninitialized,
    val keyRecoveryManualEntryError: Resource<Boolean> = Resource.Uninitialized,
    val biometryFailedError: Resource<Boolean> = Resource.Uninitialized
) {
    companion object {
        const val NO_PHRASE_ERROR = "no_phrase_error"
        const val INVALID_PHRASE_ERROR = "invalid_phrase_error"

        const val DEFAULT_WORDS_VERIFIED = 0

        const val PHRASE_WORD_COUNT = 24

        const val FIRST_WORD_INDEX = 0
        const val LAST_WORD_INDEX = PHRASE_WORD_COUNT - 1

        const val CHANGE_AMOUNT = 4
        const val LAST_SET_START_INDEX = PHRASE_WORD_COUNT - CHANGE_AMOUNT
    }
}

enum class PhraseInputMethod {
    PASTED, MANUAL
}