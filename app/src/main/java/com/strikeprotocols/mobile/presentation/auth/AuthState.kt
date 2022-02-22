package com.strikeprotocols.mobile.presentation.auth

data class AuthState(
    val authStep: AuthStep = AuthStep.SETUP,
    val triggerBioPrompt: Boolean = false
) {
    val letUserAdvance = authStep != AuthStep.PROCESSING && authStep != AuthStep.BIOMETRIC
}

enum class AuthStep {
    SETUP, BIOMETRIC, PROCESSING, FINISHED, LEAVE_SCREEN
}