package com.strikeprotocols.mobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raygun.raygun4android.RaygunClient
import com.strikeprotocols.mobile.common.BiometricUtil
import com.strikeprotocols.mobile.common.CrashReportingUtil
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.AuthProvider
import com.strikeprotocols.mobile.data.UserState
import com.strikeprotocols.mobile.data.UserStateListener
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.Screen
import com.strikeprotocols.mobile.presentation.account.AccountScreen
import com.strikeprotocols.mobile.presentation.semantic_version_check.EnforceUpdateScreen
import com.strikeprotocols.mobile.presentation.semantic_version_check.SemVerViewModel
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetailsScreen
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsListScreen
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsViewModel
import com.strikeprotocols.mobile.presentation.biometry_disabled.BiometryDisabledScreen
import com.strikeprotocols.mobile.presentation.components.OnLifecycleEvent
import com.strikeprotocols.mobile.presentation.contact_strike.ContactStrikeScreen
import com.strikeprotocols.mobile.presentation.entrance.EntranceScreen
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementInitialData
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementScreen
import com.strikeprotocols.mobile.presentation.reset_password.ResetPasswordScreen
import com.strikeprotocols.mobile.presentation.sign_in.SignInScreen
import com.strikeprotocols.mobile.service.MessagingService.Companion.NOTIFICATION_DISPLAYED_KEY
import com.strikeprotocols.mobile.ui.theme.BackgroundBlack
import com.strikeprotocols.mobile.ui.theme.StrikeMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var authProvider: AuthProvider

    val approvalsViewModel: ApprovalsViewModel by viewModels()

    private val semVerViewModel: SemVerViewModel by viewModels()

    private var userStateListener: UserStateListener? = null

    private val notificationDisplayedBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            intent?.let { safeIntent ->
                if (safeIntent.hasExtra(NOTIFICATION_DISPLAYED_KEY)
                    && safeIntent.getBooleanExtra(NOTIFICATION_DISPLAYED_KEY, false)
                ) {
                    approvalsViewModel.refreshData()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupRayGunCrashReporting()
        setupPushChannel()

        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()

            userStateListener = setupUserStateListener(navController)
            userStateListener?.let { authProvider.addUserStateListener(it) }

            val semVerState = semVerViewModel.state

            StrikeMobileTheme {
                Surface(color = BackgroundBlack) {
                    //NavHost
                    StrikeNavHost(navController = navController)

                    //region Biometric Check
                    val biometryDisabledMessage = stringResource(R.string.biometry_disabled_message)
                    val biometryUnavailableMessage = stringResource(R.string.biometry_unavailable_message)

                    OnLifecycleEvent { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START
                            -> {
                                semVerViewModel.checkMinimumVersion()

                                when (BiometricUtil.checkForBiometricFeaturesOnDevice(context)) {
                                    BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_ENABLED -> {
                                        //Do nothing, and continue with normal app flow
                                        if (navController.currentDestination?.route == Screen.BIOMETRY_DISABLED_ROUTE_KEY) {
                                            navController.backQueue.clear()
                                            navController.navigate(Screen.EntranceRoute.route)
                                        }
                                    }
                                    BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_DISABLED -> {
                                        //Display a screen that this app is unusable without biometrics
                                        // and deeplink user to the settings so they can turn it on
                                        navController.navigate("${Screen.BiometryDisabledRoute.route}/${biometryDisabledMessage}/${true}") {
                                            launchSingleTop = true
                                            navController.backQueue.clear()
                                        }
                                    }
                                    BiometricUtil.Companion.BiometricsStatus.BIOMETRICS_NOT_AVAILABLE -> {
                                        //Display a screen that this app is unusable on this device
                                        navController.navigate("${Screen.BiometryDisabledRoute.route}/${biometryUnavailableMessage}/${false}") {
                                            launchSingleTop = true
                                            navController.backQueue.clear()
                                        }
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                    //endregion

                    if (semVerState.shouldEnforceAppUpdate is Resource.Success
                        && semVerState.shouldEnforceAppUpdate.data == true) {
                        navController.navigate(Screen.EnforceUpdateRoute.route) {
                            launchSingleTop = true
                            navController.backQueue.clear()
                            popUpTo(Screen.EntranceRoute.route) { inclusive = true }
                        }
                        semVerViewModel.resetShouldEnforceAppUpdate()
                    }
                }
            }
        }
    }

    @Composable
    private fun StrikeNavHost(navController: NavHostController) {

        NavHost(
            navController = navController,
            startDestination = Screen.EntranceRoute.route,
        ) {
            composable(
                route = Screen.EntranceRoute.route
            ) {
                EntranceScreen(navController = navController)
            }
            composable(
                route = Screen.SignInRoute.route
            ) {
                SignInScreen(navController = navController)
            }
            composable(
                route = Screen.ApprovalListRoute.route,
                deepLinks = listOf(navDeepLink { uriPattern = Screen.ApprovalListRoute.buildScreenDeepLinkUri() })
            ) {
                ApprovalsListScreen(navController = navController, approvalsViewModel = approvalsViewModel)
            }
            composable(
                route = "${Screen.ApprovalDetailRoute.route}/{${Screen.ApprovalDetailRoute.APPROVAL_ARG}}",
                arguments = listOf(navArgument(Screen.ApprovalDetailRoute.APPROVAL_ARG) { type = NavType.StringType })
            ) { backStackEntry ->
                val approvalArg = backStackEntry.arguments?.get(Screen.ApprovalDetailRoute.APPROVAL_ARG) as String
                ApprovalDetailsScreen(navController = navController, approval = WalletApproval.fromJson(approvalArg))
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
            composable(
                route = Screen.AccountRoute.route
            ) {
                AccountScreen(
                    navController = navController
                )
            }
            composable(
                route = "${Screen.KeyManagementRoute.route}/{${Screen.KeyManagementRoute.KEY_MGMT_ARG}}",
                arguments = listOf(navArgument(Screen.KeyManagementRoute.KEY_MGMT_ARG) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val keyInitialDataArg = backStackEntry.arguments?.get(Screen.KeyManagementRoute.KEY_MGMT_ARG) as String
                strikeLog(message = "Initial data as string: $keyInitialDataArg")
                strikeLog(message = "Parse data: ${KeyManagementInitialData.fromJson(keyInitialDataArg)}")
                KeyManagementScreen(navController = navController, initialData = KeyManagementInitialData.fromJson(keyInitialDataArg))
            }
            composable(
                route = Screen.ResetPasswordRoute.route
            ) {
                ResetPasswordScreen(navController = navController)
            }
            composable(
                route = Screen.EnforceUpdateRoute.route
            ) {
              EnforceUpdateScreen()
            }
        }
    }

    private fun setupUserStateListener(navController: NavController) =
        object : UserStateListener {
            override fun onUserStateChanged(userState: UserState) {
                runOnUiThread {
                    if (userState == UserState.REFRESH_TOKEN_EXPIRED) {
                        navController.navigate(Screen.EntranceRoute.route) {
                            popUpTo(0)
                        }
                    }
                }
            }
        }


    override fun onResume() {
        super.onResume()
        try {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BuildConfig.APPLICATION_ID)
            registerReceiver(notificationDisplayedBroadcastReceiver, intentFilter)
        } catch (e: Exception) {
            RaygunClient.send(
                e, listOf(
                    CrashReportingUtil.BROADCAST_RECEIVER_TAG,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG,
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(notificationDisplayedBroadcastReceiver)
        } catch (e: Exception) {
            RaygunClient.send(
                e, listOf(
                    CrashReportingUtil.BROADCAST_RECEIVER_TAG,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG,
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authProvider.clearAllListeners()
    }

    private fun setupPushChannel() {
        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(
            NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun setupRayGunCrashReporting() {
        RaygunClient.init(application);
        RaygunClient.enableCrashReporting();
    }
}