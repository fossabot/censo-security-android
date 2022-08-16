package com.strikeprotocols.mobile.presentation.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.raygun.raygun4android.RaygunClient
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.NoInternetException.Companion.NO_INTERNET_ERROR
import com.strikeprotocols.mobile.data.models.PushBody
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
    private val strikeUserData: StrikeUserData
) : ViewModel() {

    var state by mutableStateOf(SignInState())
        private set

    //region Handle User Input
    fun updateEmail(updatedEmail: String) {
        state = state.copy(email = updatedEmail.lowercase().trim(), emailErrorEnabled = false)
    }

    fun updatePassword(updatedPassword: String) {
        state = state.copy(password = updatedPassword, passwordErrorEnabled = false)
    }

    //region Login + API Calls
    fun attemptLogin() {
        if (state.signInButtonEnabled) {
            state = state.copy(loginResult = Resource.Loading())
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val sessionToken =
                        userRepository.retrieveSessionToken(state.email, state.password)
                    val token = userRepository.authenticate(sessionToken)
                    userSuccessfullyLoggedIn(token)
                } catch (e: Exception) {
                    state = state.copy(loginResult = Resource.Error(e.message ?: NO_INTERNET_ERROR))
                }
            }
        } else {
            state = state.copy(
                emailErrorEnabled = !state.emailValid(),
                passwordErrorEnabled = !state.passwordValid()
            )
        }
    }

    private suspend fun userSuccessfullyLoggedIn(token: String) {
        userRepository.saveUserEmail(state.email)
        strikeUserData.setEmail(state.email)
        userRepository.setUserLoggedIn()
        submitNotificationTokenForRegistration()
        state = state.copy(loginResult = Resource.Success(token))
    }

    private suspend fun submitNotificationTokenForRegistration() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()

            val deviceId = pushRepository.getDeviceId()

            if (token.isNotEmpty() && deviceId.isNotEmpty()) {
                val pushBody = PushBody(
                    deviceId = deviceId,
                    token = token
                )
                val pushResource = pushRepository.addPushNotification(pushBody = pushBody)

                if(pushResource is Resource.Error) {
                    throw Exception("Push registration failed with code: ${pushResource.strikeError}")
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

    fun resetLoginCall() {
        state = state.copy(loginResult = Resource.Uninitialized)
    }
}