package com.censocustody.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.censocustody.android.presentation.RecoveryAppScreen
import com.censocustody.android.presentation.org_key_recovery.OrgKeyRecoveryScreen
import com.censocustody.android.ui.theme.BackgroundWhite
import com.censocustody.android.ui.theme.CensoMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            CensoMobileTheme {
                Surface(color = BackgroundWhite) {
                    //NavHost
                    CensoNavHost(navController = navController)
                }
            }
        }
    }

    @Composable
    private fun CensoNavHost(navController: NavHostController) {

        NavHost(
            navController = navController,
            startDestination = RecoveryAppScreen.OrgKeyRecoveryRoute.route,
        ) {
            composable(
                route = RecoveryAppScreen.OrgKeyRecoveryRoute.route
            ) {
                OrgKeyRecoveryScreen()
            }
        }
    }
}