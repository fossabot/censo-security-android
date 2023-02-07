package com.censocustody.android.presentation

sealed class Screen(val route: String) {
    object SignInRoute : Screen("sign_in_screen")
    object ApprovalListRoute : Screen("approvals_list_screen")
    object ApprovalDetailRoute : Screen("approval_detail_screen") {
        const val APPROVAL_ARG = "approval"
    }
    object ContactCensoRoute : Screen("contact_censo_screen")
    object AccountRoute : Screen("account_screen")
    object EnforceUpdateRoute : Screen("enforce_update_screen")
    object EntranceRoute : Screen("entrance_screen")
    object KeyCreationRoute : Screen("key_creation_state")
    object KeyManagementRoute : Screen("key_management_screen") {
        const val KEY_MGMT_ARG = "key_mgmt_arg"
    }
    object ResetPasswordRoute : Screen("reset_password_screen")
    object KeyUploadRoute : Screen("migration_screen")
    object DeviceRegistrationRoute : Screen("device_registration")

    fun buildScreenDeepLinkUri() = "$CENSO_CUSTODY_URI${this.route}"

    companion object {
        //Used for clearing the backstack to the first destination
        const val START_DESTINATION_ID = 0

        //Used for setting up deep linking options for composable screens
        const val CENSO_CUSTODY_URI = "data://censocustody/"
    }
}