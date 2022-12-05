package com.censocustody.mobile.presentation

sealed class Screen(val route: String) {
    object SignInRoute : Screen("sign_in_screen")
    object ApprovalListRoute : Screen("approvals_list_screen")
    object ApprovalDetailRoute : Screen("approval_detail_screen") {
        const val APPROVAL_ARG = "approval"
    }
    object ContactStrikeRoute : Screen("contact_strike_screen")
    object AccountRoute : Screen("account_screen")
    object EnforceUpdateRoute : Screen("enforce_update_screen")
    object EntranceRoute : Screen("entrance_screen")
    object KeyManagementRoute : Screen("key_management_screen") {
        const val KEY_MGMT_ARG = "key_mgmt_arg"
    }
    object ResetPasswordRoute : Screen("reset_password_screen")
    object MigrationRoute : Screen("migration_screen") {
        const val MIGRATION_ARG = "migration_arg"
    }
    object RegenerationRoute : Screen("regeneration_screen")

    fun buildScreenDeepLinkUri() = "$STRIKE_PROTOCOLS_URI${this.route}"

    companion object {
        //Used for clearing the backstack to the first destination
        const val START_DESTINATION_ID = 0

        //Used for setting up deep linking options for composable screens
        const val STRIKE_PROTOCOLS_URI = "data://strikeprotocols/"
    }
}