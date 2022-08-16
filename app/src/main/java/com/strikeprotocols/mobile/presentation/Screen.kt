package com.strikeprotocols.mobile.presentation

sealed class Screen(val route: String) {
    object SignInRoute : Screen("sign_in_screen")
    object ApprovalListRoute : Screen("approvals_list_screen")
    object ApprovalDetailRoute : Screen("approval_detail_screen") {
        const val APPROVAL_ARG = "approval"
    }
    object ContactStrikeRoute : Screen("contact_strike_screen")
    object BiometryDisabledRoute : Screen("biometry_disabled_screen") {
        const val MESSAGE_ARG = "message"
        const val BIOMETRY_AVAILABLE_ARG = "biometry available"
    }
    object AccountRoute : Screen("account_screen")
    object ResetPasswordRoute : Screen("reset_password_screen")
    object EnforceUpdateRoute : Screen("enforce_update_screen")
    object EntranceRoute : Screen("entrance_screen")
    object KeyManagementRoute : Screen("key_management_screen") {
        const val KEY_MGMT_ARG = "key_mgmt_arg"
    }

    fun buildScreenDeepLinkUri() = "$STRIKE_PROTOCOLS_URI${this.route}"

    companion object {
        //Used for setting up deep linking options for composable screens
        const val STRIKE_PROTOCOLS_URI = "data://strikeprotocols/"

        //This val is used to check if the current destination route matches up with the biometry disabled screen.
        //If the biometry disabled screen route changes, then update this key to match it.
        val BIOMETRY_DISABLED_ROUTE_KEY = "${BiometryDisabledRoute.route}/{${BiometryDisabledRoute.MESSAGE_ARG}}/{${BiometryDisabledRoute.BIOMETRY_AVAILABLE_ARG}}"
    }
}