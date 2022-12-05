package com.censocustody.mobile.data

import com.censocustody.mobile.common.BioPromptReason

data class BioPromptData(val bioPromptReason: BioPromptReason, val immediate: Boolean = false)