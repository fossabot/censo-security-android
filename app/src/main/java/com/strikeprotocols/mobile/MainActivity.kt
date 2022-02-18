package com.strikeprotocols.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsListScreen
import com.strikeprotocols.mobile.presentation.sign_in.SignInScreen
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.StrikeMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StrikeMobileTheme {
                Surface(color = BackgroundBlack) {
                    val navController = rememberNavController()
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
                    }
                }
            }
        }
    }
}