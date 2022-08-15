package com.strikeprotocols.mobile.presentation.semantic_version_check

import com.strikeprotocols.mobile.common.Resource

data class SemVerState(
    val shouldEnforceAppUpdate: Resource<Boolean> = Resource.Uninitialized
)