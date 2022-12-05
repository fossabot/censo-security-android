package com.censocustody.mobile.presentation.semantic_version_check

import com.censocustody.mobile.common.BioPromptReason
import com.censocustody.mobile.common.BiometricUtil
import com.censocustody.mobile.common.Resource
import javax.crypto.Cipher

data class MainState(
    val sendUserToEntrance: Boolean = false,
    val bioPromptTrigger: Resource<Cipher> = Resource.Uninitialized,
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
    val biometryTooManyAttempts: Boolean = false,
    val biometryStatus: BiometricUtil.Companion.BiometricsStatus? = null,
    val currentDestination: String? = null,
    val blockAppUI: BlockAppUI = BlockAppUI.NONE
)

enum class BlockAppUI {
    BIOMETRY_DISABLED, FOREGROUND_BIOMETRY, NONE
}