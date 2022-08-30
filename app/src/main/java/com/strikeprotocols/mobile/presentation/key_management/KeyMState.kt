package com.strikeprotocols.mobile.presentation.key_management

import com.strikeprotocols.mobile.common.BioPromptReason
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletSigner
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
    val phrase: String? = null,
    val pastedPhrase: String = "",
    val wordIndex: Int = 0,
    val confirmPhraseWordsState: ConfirmPhraseWordsState = ConfirmPhraseWordsState(),

    //API calls
    val finalizeKeyFlow: Resource<WalletSigner> = Resource.Uninitialized,
    val walletSignerToAdd: WalletSigner? = null,

    //Utility state
    val triggerBioPrompt: Resource<Cipher> = Resource.Uninitialized,
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
    val showToast: Resource<String> = Resource.Uninitialized,
    val goToAccount: Resource<Boolean> = Resource.Uninitialized,
    val keyRecoveryManualEntryError: Resource<Boolean> = Resource.Uninitialized,
    val biometryFailedError: Resource<Boolean> = Resource.Uninitialized
) {
    companion object {
        const val NO_PHRASE_ERROR = "no_phrase_error"
    }
}

enum class PhraseInputMethod {
    PASTED, MANUAL
}