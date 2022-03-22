package com.strikeprotocols.mobile.presentation

sealed class Screen(val route: String) {
    object SignInRoute : Screen("sign_in_screen")
    object ApprovalListRoute : Screen("approvals_list_screen")
    object ApprovalDetailRoute : Screen("approval_detail_screen")
    object AuthRoute : Screen("auth_screen")
    object ContactStrikeRoute : Screen("contact_strike_screen")
}