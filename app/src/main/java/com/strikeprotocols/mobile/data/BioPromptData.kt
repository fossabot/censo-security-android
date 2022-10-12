package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.common.BioPromptReason

data class BioPromptData(val bioPromptReason: BioPromptReason, val immediate: Boolean = false)