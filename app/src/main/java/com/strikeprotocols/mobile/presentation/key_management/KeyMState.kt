package com.strikeprotocols.mobile.presentation.key_management

import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.key_management.flows.KeyCreationFlowStep
import com.strikeprotocols.mobile.presentation.key_management.flows.KeyManagementFlowStep

data class KeyManagementState(
    //initial data + key management flow data
    val initialData: KeyManagementInitialData? = null,
    val keyManagementFlow: KeyManagementFlow = KeyManagementFlow.UNINITIALIZED,
    val keyManagementFlowStep: KeyManagementFlowStep = KeyManagementFlowStep.CreationFlow(
        KeyCreationFlowStep.UNINITIALIZED
    ),

    //Phrase
    val phrase: String? = null,
    val pastedPhrase: String = "",
    val wordIndex: Int = 0,
    val confirmPhraseWordsState: ConfirmPhraseWordsState = ConfirmPhraseWordsState(),

    //API calls
    val addWalletSignerResult: Resource<WalletSigner> = Resource.Uninitialized,
    val walletSignerToAdd: WalletSigner? = null,

    //Utility state
    val showToast: Resource<String> = Resource.Uninitialized,
    val goToAccount: Resource<Boolean> = Resource.Uninitialized,
    val keyRecoveryManualEntryError: Resource<Boolean> = Resource.Uninitialized
) {
    companion object {
        const val NO_PHRASE_ERROR = "no_phrase_error"
    }
}