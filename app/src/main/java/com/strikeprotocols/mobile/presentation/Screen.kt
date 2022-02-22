package com.strikeprotocols.mobile.presentation

sealed class Screen(val route: String) {
    object SignInRoute : Screen("sign_in_screen")
    object ApprovalListRoute : Screen("approvals_list_screen")
    object AuthRoute : Screen("auth_screen")
}