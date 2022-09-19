package com.strikeprotocols.mobile.presentation.semantic_version_check

import com.strikeprotocols.mobile.common.Resource
import javax.crypto.Cipher

data class SemVerState(
    val shouldEnforceAppUpdate: Resource<Boolean> = Resource.Uninitialized,
    val bioPromptTrigger: Resource<Cipher> = Resource.Uninitialized,
    val biometryUnavailable: Boolean = false
)