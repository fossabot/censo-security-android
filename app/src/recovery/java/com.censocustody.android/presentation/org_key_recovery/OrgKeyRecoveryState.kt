package com.censocustody.android.presentation.org_key_recovery

import com.censocustody.android.common.Resource
import com.censocustody.android.presentation.key_management.ConfirmPhraseWordsState
import com.censocustody.android.presentation.key_management.KeyManagementState.Companion.FIRST_WORD_INDEX
import com.censocustody.android.presentation.key_management.PhraseInputMethod
import com.censocustody.android.presentation.key_management.flows.KeyManagementFlowStep
import com.censocustody.android.presentation.key_management.flows.KeyRecoveryFlowStep

data class OrgKeyRecoveryState(
    //Key Recovery flow data
    val keyRecoveryFlowStep: KeyManagementFlowStep = KeyManagementFlowStep.RecoveryFlow(
        KeyRecoveryFlowStep.UNINITIALIZED
    ),
    val inputMethod: PhraseInputMethod = PhraseInputMethod.MANUAL,

    //Phrase
    val userInputtedPhrase: String = "",
    val wordIndexForDisplay: Int = FIRST_WORD_INDEX,
    val confirmPhraseWordsState: ConfirmPhraseWordsState = ConfirmPhraseWordsState(),

    //SideEffect state
    val finalizeKeyFlow: Resource<Unit> = Resource.Uninitialized,

    //Utility State
    val showToast: Resource<String> = Resource.Uninitialized,
    val keyRecoveryManualEntryError: Resource<Boolean> = Resource.Uninitialized,
    val onExit: Resource<Unit> = Resource.Uninitialized
)
