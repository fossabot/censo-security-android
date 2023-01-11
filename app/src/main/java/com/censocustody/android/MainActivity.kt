package com.censocustody.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raygun.raygun4android.RaygunClient
import com.censocustody.android.common.BiometricUtil
import com.censocustody.android.common.CrashReportingUtil
import com.censocustody.android.common.Resource
import com.censocustody.android.common.*
import com.censocustody.android.common.BioCryptoUtil.NO_CIPHER_CODE
import com.censocustody.android.data.*
import com.censocustody.android.data.models.approval.ApprovalRequest
import com.censocustody.android.presentation.Screen
import com.censocustody.android.presentation.account.AccountScreen
import com.censocustody.android.presentation.semantic_version_check.EnforceUpdateScreen
import com.censocustody.android.presentation.semantic_version_check.MainViewModel
import com.censocustody.android.presentation.approval_detail.ApprovalDetailsScreen
import com.censocustody.android.presentation.approvals.ApprovalsListScreen
import com.censocustody.android.presentation.approvals.ApprovalsViewModel
import com.censocustody.android.presentation.components.OnLifecycleEvent
import com.censocustody.android.presentation.contact_censo.ContactCensoScreen
import com.censocustody.android.presentation.device_registration.DeviceRegistrationScreen
import com.censocustody.android.presentation.entrance.EntranceScreen
import com.censocustody.android.presentation.key_creation.KeyCreationScreen
import com.censocustody.android.presentation.key_management.KeyManagementInitialData
import com.censocustody.android.presentation.key_management.KeyManagementScreen
import com.censocustody.android.presentation.migration.VerifyUserInitialData
import com.censocustody.android.presentation.migration.MigrationScreen
import com.censocustody.android.presentation.regeneration.RegenerationScreen
import com.censocustody.android.presentation.reset_password.ResetPasswordScreen
import com.censocustody.android.presentation.sign_in.SignInScreen
import com.censocustody.android.service.MessagingService.Companion.NOTIFICATION_DISPLAYED_KEY
import com.censocustody.android.ui.theme.BackgroundBlack
import com.censocustody.android.ui.theme.CensoMobileTheme
import com.censocustody.android.presentation.semantic_version_check.BlockingUI
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var authProvider: AuthProvider

    val approvalsViewModel: ApprovalsViewModel by viewModels()

    internal val mainViewModel: MainViewModel by viewModels()

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

            userStateListener = setupUserStateListener(navController, context)
            userStateListener?.let { authProvider.addUserStateListener(it) }

            val mainState = mainViewModel.state

            LaunchedEffect(key1 = mainState) {

                if (mainState.bioPromptTrigger is Resource.Success) {
                    val promptInfo = BioCryptoUtil.createPromptInfo(context = context)

                    val bioPrompt = BioCryptoUtil.createBioPrompt(
                        fragmentActivity = this@MainActivity,
                        onSuccess = {
                            val cipher = it?.cipher
                            if (cipher != null) {
                                mainViewModel.biometryApproved(cipher)
                            } else {
                                BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = NO_CIPHER_CODE) {
                                    mainViewModel.biometryFailed(errorCode = NO_CIPHER_CODE)
                                }
                            }
                        },
                        onFail = {
                            BioCryptoUtil.handleBioPromptOnFail(context = context, errorCode = it) {
                                mainViewModel.biometryFailed(errorCode = it)
                            }
                        }
                    )

                    mainState.bioPromptTrigger.data?.let {
                        bioPrompt.authenticate(
                            promptInfo,
                            BiometricPrompt.CryptoObject(mainState.bioPromptTrigger.data)
                        )
                        mainViewModel.setPromptTriggerToLoading()
                    }
                }
            }

            CensoMobileTheme {
                Surface(color = BackgroundBlack) {
                    //NavHost
                    CensoNavHost(navController = navController)

                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        mainViewModel.updateCurrentScreen(
                            destination.route
                        )
                    }

                    OnLifecycleEvent { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START
                            -> {
                                mainViewModel.onForeground(
                                    BiometricUtil.checkForBiometricFeaturesOnDevice(context),
                                )
                            }
                            else -> Unit
                        }
                    }
                    //endregion

                    if (mainState.sendUserToEntrance) {
                        navController.navigate(Screen.EntranceRoute.route) {
                            launchSingleTop = true
                            popUpToTop()
                        }
                        mainViewModel.resetSendUserToEntrance()
                    }

                    val blockAppUI = mainViewModel.blockUIStatus()

                    BlockingUI(
                        blockAppUI = blockAppUI,
                        bioPromptTrigger = mainState.bioPromptTrigger,
                        biometryUnavailable = mainState.biometryTooManyAttempts,
                        biometryStatus = mainState.biometryStatus,
                        retry = mainViewModel::retryBiometricGate
                    )
                }
            }
        }
    }

    @Composable
    private fun CensoNavHost(navController: NavHostController) {

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
                ApprovalDetailsScreen(navController = navController, approval = ApprovalRequest.fromJson(approvalArg))
            }
            composable(
                route = Screen.ContactCensoRoute.route
            ) {
                ContactCensoScreen()
            }
            composable(
                route = Screen.AccountRoute.route
            ) {
                AccountScreen(
                    navController = navController
                )
            }
            composable(
                route = Screen.KeyCreationRoute.route
            ) {
                KeyCreationScreen(navController = navController)
            }
            composable(
                route = "${Screen.KeyManagementRoute.route}/{${Screen.KeyManagementRoute.KEY_MGMT_ARG}}",
                arguments = listOf(navArgument(Screen.KeyManagementRoute.KEY_MGMT_ARG) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val keyInitialDataArg = backStackEntry.arguments?.get(Screen.KeyManagementRoute.KEY_MGMT_ARG) as String
                KeyManagementScreen(navController = navController, initialData = KeyManagementInitialData.fromJson(keyInitialDataArg))
            }
            composable(
                route = "${Screen.MigrationRoute.route}/{${Screen.MigrationRoute.MIGRATION_ARG}}",
                arguments = listOf(navArgument(Screen.MigrationRoute.MIGRATION_ARG) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val migrationDataInitialArg = backStackEntry.arguments?.get(Screen.MigrationRoute.MIGRATION_ARG) as String
                MigrationScreen(navController = navController, initialData = VerifyUserInitialData.fromJson(migrationDataInitialArg))
            }
            composable(
                route = Screen.RegenerationRoute.route,
            ) {
                RegenerationScreen(navController = navController)
            }
            composable(
                route = Screen.EnforceUpdateRoute.route
            ) {
              EnforceUpdateScreen()
            }
            composable(
                route = Screen.ResetPasswordRoute.route
            ) {
                ResetPasswordScreen(navController = navController)
            }
            composable(
                route = Screen.DeviceRegistrationRoute.route
            ) {
                DeviceRegistrationScreen(navController = navController)
            }
        }
    }

    private fun setupUserStateListener(navController: NavController, context: Context) =
        object : UserStateListener {
            override fun onUserStateChanged(userState: UserState) {
                runOnUiThread {
                    if (userState == UserState.INVALIDATED_KEY) {
                        Toast.makeText(
                            context,
                            getString(R.string.biometry_changed_key_invalidated),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    if (userState == UserState.INVALID_SENTINEL_DATA) {
                        mainViewModel.resetBiometry()

                        Toast.makeText(
                            context,
                            getString(R.string.sentinel_data_invalidated),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    if (userState == UserState.REFRESH_TOKEN_EXPIRED
                        || userState == UserState.INVALIDATED_KEY || userState == UserState.INVALID_SENTINEL_DATA) {
                        navController.navigate(Screen.EntranceRoute.route) {
                            launchSingleTop = true
                            popUpToTop()
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