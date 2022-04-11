package com.strikeprotocols.mobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.strikeprotocols.mobile.common.BiometricUtil
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.AuthProvider
import com.strikeprotocols.mobile.data.UserState
import com.strikeprotocols.mobile.data.UserStateListener
import com.strikeprotocols.mobile.data.models.WalletApproval
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetailsScreen
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsListScreen
import com.strikeprotocols.mobile.presentation.auth.AuthScreen
import com.strikeprotocols.mobile.presentation.biometry_disabled.BiometryDisabledScreen
import com.strikeprotocols.mobile.presentation.components.OnLifecycleEvent
import com.strikeprotocols.mobile.presentation.contact_strike.ContactStrikeScreen
import com.strikeprotocols.mobile.presentation.sign_in.SignInScreen
import com.strikeprotocols.mobile.presentation.splash.SplashScreen
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


        // Create channel to show notifications.
        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(
            NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_LOW)
        )

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()

            userStateListener = setupUserStateListener(navController)
            userStateListener?.let { authProvider.addUserStateListener(it) }

            StrikeMobileTheme {
                Surface(color = BackgroundBlack) {
                    //NavHost
                    StrikeNavHost(navController = navController)

                    //Biometric Check
                    val biometryDisabledMessage = stringResource(R.string.biometry_disabled_message)
                    val biometryUnavailableMessage = stringResource(R.string.biometry_unavailable_message)

                    OnLifecycleEvent { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START
                            -> {
                                when (BiometricUtil.checkForBiometricFeaturesOnDevice(context)) {
                                    BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED -> {
                                        //Do nothing, and continue with normal app flow
                                        if (navController.currentDestination?.route == Screen.BIOMETRY_DISABLED_ROUTE_KEY) {
                                            navController.popBackStack()
                                            navController.navigate(Screen.SignInRoute.route)
                                        }
                                    }
                                    BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_DISABLED -> {
                                        //Display a screen that this app is unusable without biometrics
                                        // and deeplink user to the settings so they can turn it on
                                        navController.navigate("${Screen.BiometryDisabledRoute.route}/${biometryDisabledMessage}/${true}", ) {
                                            launchSingleTop = true
                                            popUpTo(Screen.ApprovalListRoute.route) { inclusive = true }
                                        }
                                    }
                                    BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_NOT_AVAILABLE -> {
                                        //Display a screen that this app is unusable on this device
                                        navController.navigate("${Screen.BiometryDisabledRoute.route}/${biometryUnavailableMessage}/${false}", ) {
                                            launchSingleTop = true
                                            popUpTo(Screen.ApprovalListRoute.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun StrikeNavHost(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = Screen.ApprovalListRoute.route
        ) {
            composable(
                route = Screen.SplashRoute.route
            ) {
                SplashScreen(navController = navController)
            }
            composable(
                route = Screen.SignInRoute.route
            ) {
                SignInScreen(navController = navController)
            }
            composable(
                route = Screen.ApprovalListRoute.route
            ) {
                ApprovalsListScreen(navController = navController)
            }
            composable(
                route = "${Screen.ApprovalDetailRoute.route}/{${Screen.ApprovalDetailRoute.APPROVAL_ARG}}",
                arguments = listOf(navArgument(Screen.ApprovalDetailRoute.APPROVAL_ARG) { type = NavType.StringType })
            ) { backStackEntry ->
                val approvalArg = backStackEntry.arguments?.get(Screen.ApprovalDetailRoute.APPROVAL_ARG) as String
                ApprovalDetailsScreen(navController = navController, approval = WalletApproval.fromJson(approvalArg))
            }
            composable(
                route = Screen.AuthRoute.route
            ) {
                AuthScreen(navController = navController)
            }
            composable(
                route = Screen.ContactStrikeRoute.route
            ) {
                ContactStrikeScreen()
            }
            composable(
                route = Screen.BIOMETRY_DISABLED_ROUTE_KEY,
                arguments = listOf(navArgument(Screen.BiometryDisabledRoute.MESSAGE_ARG) { type = NavType.StringType },
                    navArgument(Screen.BiometryDisabledRoute.BIOMETRY_AVAILABLE_ARG) { type = NavType.BoolType })
            ) { backStackEntry ->
                val messageArg = backStackEntry.arguments?.get(Screen.BiometryDisabledRoute.MESSAGE_ARG) as String
                val biometryAvailableArg = backStackEntry.arguments?.get(Screen.BiometryDisabledRoute.BIOMETRY_AVAILABLE_ARG) as Boolean
                BiometryDisabledScreen(
                    message = messageArg,
                    biometryAvailable = biometryAvailableArg)
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