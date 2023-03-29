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
    object KeyCreationRoute : Screen("key_creation_screen")  {
        const val KEY_CREATION_ARG = "key_creation_arg"
    }
    object ResetPasswordRoute : Screen("reset_password_screen")
    object UploadKeysRoute : Screen("upload_keys_screen")
    object DeviceRegistrationRoute : Screen("device_registration_screen") {
        const val DEVICE_REG_ARG = "device_registration_arg"
    }
    object PendingApprovalRoute : Screen("pending_approval_screen") {
        const val PENDING_APPROVAL_ARG = "pending_approval_arg"
    }
    object KeyRecoveryRoute : Screen("key_recovery_route") {
        const val KEY_RECOVERY_ARG = "key_recovery_arg"
    }

    fun buildScreenDeepLinkUri() = "$CENSO_CUSTODY_URI${this.route}"

    companion object {
        //Used for clearing the backstack to the first destination
        const val START_DESTINATION_ID = 0

        //Used for setting up deep linking options for composable screens
        const val CENSO_CUSTODY_URI = "data://censocustody/"
    }
}