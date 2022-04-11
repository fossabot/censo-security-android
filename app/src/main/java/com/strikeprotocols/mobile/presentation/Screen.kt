package com.strikeprotocols.mobile.presentation

sealed class Screen(val route: String) {
    object SplashRoute : Screen("splash_screen")
    object SignInRoute : Screen("sign_in_screen")
    object ApprovalListRoute : Screen("approvals_list_screen")
    object ApprovalDetailRoute : Screen("approval_detail_screen") {
        const val APPROVAL_ARG = "approval"
    }
    object AuthRoute : Screen("auth_screen")
    object ContactStrikeRoute : Screen("contact_strike_screen")
    object BiometryDisabledRoute : Screen("biometry_disabled_screen") {
        const val MESSAGE_ARG = "message"
        const val BIOMETRY_AVAILABLE_ARG = "biometry available"
    }

    companion object {
        //This val is used to check if the current destination route matches up with the biometry disabled screen.
        //If the biometry disabled screen route changes, then update this key to match it.
        val BIOMETRY_DISABLED_ROUTE_KEY = "${BiometryDisabledRoute.route}/{${BiometryDisabledRoute.MESSAGE_ARG}}/{${BiometryDisabledRoute.BIOMETRY_AVAILABLE_ARG}}"
    }
}