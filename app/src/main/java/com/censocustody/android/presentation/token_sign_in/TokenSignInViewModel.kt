package com.censocustody.android.presentation.token_sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.raygun.raygun4android.RaygunClient
import com.censocustody.android.common.*
import com.censocustody.android.data.*
import com.censocustody.android.data.NoInternetException.Companion.NO_INTERNET_ERROR
import com.censocustody.android.data.models.LoginResponse
import com.censocustody.android.data.models.PushBody
import kotlinx.coroutines.*
import javax.crypto.Cipher

@HiltViewModel
class TokenSignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
    private val keyRepository: KeyRepository,
    private val censoUserData: CensoUserData
) : ViewModel() {

    var state by mutableStateOf(TokenSignInState())
        private set

    fun onStart(email: String?, token: String?) {
        state = state.copy(
            email = email ?: "",
            verificationToken = token ?: ""
        )

        logUserIn()
    }

    private fun logUserIn() {
        viewModelScope.launch {
            if (userRepository.userLoggedIn()) {
                state = state.copy(exitLoginFlow = Resource.Success(Unit))
                return@launch
            }

            if (!state.emailValid() || !state.verificationTokenValid()) {
                state = state.copy(exitLoginFlow = Resource.Success(Unit))
                return@launch
            }

            loginWithToken()
        }
    }

    private suspend fun loginWithToken() {
        state = state.copy(loginResult = Resource.Loading())
        try {
            val loginResource = userRepository.loginWithVerificationToken(
                email = state.email, token = state.verificationToken
            )
            when (loginResource) {
                is Resource.Success -> {
                    val token = loginResource.data?.token
                    if (token != null) {
                        userSuccessfullyLoggedIn(token)
                        showBiometryForSentinelData()
                    } else {
                        userFailedLogin(e = Exception("NO TOKEN"))
                    }
                }
                is Resource.Error -> {
                    userFailedLogin(resource = loginResource)
                }
                else -> {
                    state = state.copy(loginResult = Resource.Loading())
                }
            }
        } catch (e: Exception) {
            userFailedLogin(e = e)
        }
    }

    fun biometryApproved(cipher: Cipher?) {
        if (cipher != null) {
            saveSentinelData(cipher)
        }
    }

    fun biometryFailed() {
        state = state.copy(triggerBioPrompt = Resource.Error())
    }

    private suspend fun showBiometryForSentinelData() {
        val cipher = keyRepository.getInitializedCipherForSentinelEncryption()

        if (cipher != null) {
            state = state.copy(
                triggerBioPrompt = Resource.Success(cipher),
            )
        }
    }

    fun retryBiometry() {
        viewModelScope.launch {
            showBiometryForSentinelData()
        }
    }

    private fun saveSentinelData(cipher: Cipher) {
        viewModelScope.launch {
            state = try {
                keyRepository.saveSentinelData(cipher)
                state.copy(exitLoginFlow = Resource.Success(Unit))
            } catch (e: Exception) {
                keyRepository.handleKeyInvalidatedException(e)
                state.copy(exitLoginFlow = Resource.Success(Unit))
            }
        }
    }

    private suspend fun userSuccessfullyLoggedIn(token: String) {
        userRepository.saveUserEmail(state.email)
        censoUserData.setEmail(state.email)
        userRepository.setUserLoggedIn()
        userRepository.saveToken(token)
        submitNotificationTokenForRegistration()
        state = state.copy(loginResult = Resource.Success(LoginResponse(token)))
    }

    private fun userFailedLogin(resource: Resource<LoginResponse>? = null, e: Exception? = null) {
        state =
            state.copy(
                exitLoginFlow = Resource.Success(Unit),
                loginResult = resource ?: Resource.Error(
                    exception = Exception(e?.message ?: NO_INTERNET_ERROR)
                )
            )
    }


    private suspend fun submitNotificationTokenForRegistration() {
        try {
            val token = pushRepository.retrievePushToken()

            val deviceId = pushRepository.getDeviceId()

            if (token.isNotEmpty() && deviceId.isNotEmpty()) {
                val pushBody = PushBody(
                    deviceId = deviceId,
                    token = token
                )
                val pushResource = pushRepository.addPushNotification(pushBody = pushBody)

                if (pushResource is Resource.Error) {
                    throw Exception("Push registration failed with code: ${pushResource.censoError}")
                }
            } else {
                if (token.isEmpty()) {
                    throw Exception("Firebase push token is empty")
                } else if (deviceId.isEmpty()) {
                    throw Exception("Device id is empty")
                } else {
                    throw Exception("Unable to create push body")
                }
            }
        } catch (e: Exception) {
            RaygunClient.send(
                e, listOf(
                    CrashReportingUtil.PUSH_NOTIFICATION_TAG,
                    CrashReportingUtil.MANUALLY_REPORTED_TAG,
                )
            )
        }
    }

    fun resetExitLoginFlow() {
        state = state.copy(exitLoginFlow = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
}