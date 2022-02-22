package com.strikeprotocols.mobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.navigation.NavController
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.strikeprotocols.mobile.data.AuthProvider
import com.strikeprotocols.mobile.data.UserState
import com.strikeprotocols.mobile.data.UserStateListener
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsListScreen
import com.strikeprotocols.mobile.presentation.auth.AuthScreen
import com.strikeprotocols.mobile.presentation.sign_in.SignInScreen
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.StrikeMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var authProvider: AuthProvider

    private var userStateListener: UserStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            userStateListener = setupUserStateListener(navController)
            userStateListener?.let { authProvider.addUserStateListener(it) }

            StrikeMobileTheme {
                Surface(color = BackgroundBlack) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.SignInRoute.route
                    ) {
                        composable(
                            route = Screen.SignInRoute.route
                        ) {
                            SignInScreen(navController)
                        }
                        composable(
                            route = Screen.ApprovalListRoute.route
                        ) {
                            ApprovalsListScreen()
                        }
                        composable(
                            route = Screen.AuthRoute.route
                        ) {
                            AuthScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }

    private fun setupUserStateListener(navController: NavController) =
        object : UserStateListener {
            override fun onUserStateChanged(userState: UserState) {
                runOnUiThread {
                    if (userState == UserState.REFRESH_TOKEN_EXPIRED) {
                        navController.navigate(Screen.SignInRoute.route) {
                            popUpTo(0)
                        }
                    }
                }
            }
        }


    override fun onDestroy() {
        super.onDestroy()
        authProvider.clearAllListeners()
    }
}