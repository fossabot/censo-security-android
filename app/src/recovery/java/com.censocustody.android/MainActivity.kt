package com.censocustody.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.censocustody.android.common.censoLog
import com.censocustody.android.presentation.RecoveryAppScreen
import com.censocustody.android.presentation.org_key_recovery.OrgKeyRecoveryScreen
import com.censocustody.android.presentation.recovery.RecoveryAppState
import com.censocustody.android.presentation.recovery.RecoveryAppViewModel
import com.censocustody.android.ui.theme.BackgroundWhite
import com.censocustody.android.ui.theme.CensoMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    internal val recoveryAppViewModel: RecoveryAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()

            val recoveryAppState = recoveryAppViewModel.state

            CensoMobileTheme {
                Surface(color = BackgroundWhite) {
                    //NavHost
                    CensoNavHost(navController = navController, mainState = recoveryAppState)
                }
            }
        }
    }

    @Composable
    private fun CensoNavHost(navController: NavHostController, mainState: RecoveryAppState) {

        NavHost(
            navController = navController,
            startDestination = RecoveryAppScreen.BaseRoute.route,
        ) {
            composable(
                route = RecoveryAppScreen.BaseRoute.route
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Recovery App")
                    Spacer(modifier = Modifier.height(36.dp))
                    TextButton(onClick = {
                        censoLog(message = "Recover key triggered [IMPLEMENT]")
                        navController.navigate(RecoveryAppScreen.OrgKeyRecoveryRoute.route)
                    }) {
                        Text(text = "Recover Key")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = {
                        censoLog(message = "Create key triggered [Implement]")
                        recoveryAppViewModel.createPhrase()
                    }) {
                        Text(text = "Create Key")
                    }
                    if (mainState.generatedPhrase.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            modifier = Modifier.padding(16.dp),
                            softWrap = true,
                            text = mainState.generatedPhrase
                        )
                    }
                }
            }
            composable(
                route = RecoveryAppScreen.OrgKeyRecoveryRoute.route
            ) {
                OrgKeyRecoveryScreen(navController = navController)
            }
        }
    }
}