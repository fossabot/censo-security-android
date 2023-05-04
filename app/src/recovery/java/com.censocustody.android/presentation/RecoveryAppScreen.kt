package com.censocustody.android.presentation

sealed class RecoveryAppScreen(val route: String) {
    object BaseRoute : RecoveryAppScreen("base_screen")
    object OrgKeyRecoveryRoute : RecoveryAppScreen("key_recovery_screen")
}