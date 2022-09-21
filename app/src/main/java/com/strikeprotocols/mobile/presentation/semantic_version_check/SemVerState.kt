package com.strikeprotocols.mobile.presentation.semantic_version_check

import com.strikeprotocols.mobile.common.BioPromptReason
import com.strikeprotocols.mobile.common.Resource
import javax.crypto.Cipher

data class SemVerState(
    val shouldEnforceAppUpdate: Resource<Boolean> = Resource.Uninitialized,
    val bioPromptTrigger: Resource<Cipher> = Resource.Uninitialized,
    val bioPromptReason: BioPromptReason = BioPromptReason.UNINITIALIZED,
    val biometryUnavailable: Boolean = false
)