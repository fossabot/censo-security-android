package com.censocustody.android.presentation.org_key_recovery

import android.graphics.Bitmap
import com.censocustody.android.common.Resource
import com.censocustody.android.presentation.key_management.ConfirmPhraseWordsState
import com.censocustody.android.presentation.key_management.KeyManagementState.Companion.FIRST_WORD_INDEX
import com.censocustody.android.presentation.key_management.PhraseInputMethod
import com.censocustody.android.presentation.key_management.flows.KeyManagementFlowStep
import com.censocustody.android.presentation.key_management.flows.KeyRecoveryFlowStep

data class OrgKeyRecoveryState(
    //Recovery State
    val recoveryStep: RecoveryStep = RecoveryStep.RECOVERY_START,
    val recoveryProcess: RecoveryProcessState = RecoveryProcessState(),
    val recoveredKey: HashMap<String, String> = hashMapOf(),
    val signedRecoveryData: String = "",

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

    //QRCode
    val qrCodeBitmap: Bitmap? = null,
    val scanQRCodeResult: Resource<String> = Resource.Uninitialized,
    val scannedData: String = "",

    //Utility State
    val showToast: Resource<String> = Resource.Uninitialized,
    val onExit: Resource<Unit> = Resource.Uninitialized
)

data class RecoveryProcessState(
    val qrCodeScanned: Boolean = false,
    val keyRecovered: Boolean = false
)

enum class RecoveryStep {
    RECOVERY_START, PHRASE_ENTRY, SCAN_QR, DISPLAY_QR, RECOVERY_ERROR
}
