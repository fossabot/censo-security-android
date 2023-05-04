package com.censocustody.android.presentation

sealed class RecoveryAppScreen(val route: String) {
    object OrgKeyRecoveryRoute : RecoveryAppScreen("key_recovery_screen")
}