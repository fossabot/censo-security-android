package com.censocustody.android.presentation.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.raygun.raygun4android.RaygunClient
import com.censocustody.android.common.*
import com.censocustody.android.common.exception.NoInternetException.Companion.NO_INTERNET_ERROR
import com.censocustody.android.common.util.CrashReportingUtil
import com.censocustody.android.common.util.sendError
import com.censocustody.android.data.models.LoginResponse
import com.censocustody.android.data.models.PushBody
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.repository.PushRepository
import com.censocustody.android.data.repository.UserRepository
import kotlinx.coroutines.*
import javax.crypto.Cipher

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
    private val keyRepository: KeyRepository
) : ViewModel() {

    var state by mutableStateOf(SignInState())
        private set

    init {
        getEmailIfSaved()
    }

    private fun getEmailIfSaved() {
        viewModelScope.launch {
            val email = userRepository.retrieveUserEmail()
            state = state.copy(email = email)
        }
    }

    //region Handle User Input
    fun updateEmail(updatedEmail: String) {
        val sanitizedEmail = updatedEmail.lowercase().trim()
        state = state.copy(email = sanitizedEmail, emailErrorEnabled = false)
        viewModelScope.launch { userRepository.saveUserEmail(sanitizedEmail) }
    }

    fun updateVerificationToken(updatedToken: String) {
        state = state.copy(verificationToken = updatedToken, verificationTokenErrorEnabled = false)
    }

    fun moveBackToEmailScreen() {
        state = state.copy(loginStep = LoginStep.EMAIL_ENTRY)
    }

    fun signInActionCompleted() {
        viewModelScope.launch {
            val userLoggedIn = userRepository.userLoggedIn()

            if (userLoggedIn) {
                handleReturnLoggedInUser()
                return@launch
            }

            if (state.loginStep == LoginStep.EMAIL_ENTRY) {
                checkEmail()
            } else {
                checkToken()
            }
        }
    }

    private fun checkEmail() {
        if (state.email.isEmpty()) {
            state = state.copy(emailErrorEnabled = true)
        } else {
            kickOffBiometryLoginOrSendVerificationEmail()
        }
    }

    fun skipToTokenEntry() {
        state = state.copy(
            loginStep = LoginStep.TOKEN_ENTRY,
            loginResult = Resource.Uninitialized
        )
    }

    fun kickOffBiometryLoginOrSendVerificationEmail() {
        viewModelScope.launch {
            if (userRepository.userHasDeviceIdSaved(state.email)) {
                state = state.copy(
                    triggerBioPrompt = Resource.Success(null),
                    bioPromptReason = BioPromptReason.RETURN_LOGIN,
                    loginResult = Resource.Uninitialized
                )
            } else {
                sendVerificationEmail()
            }
        }
    }

    fun biometryApproved(cipher: Cipher?) {
        if (state.bioPromptReason == BioPromptReason.SAVE_SENTINEL && cipher != null) {
            saveSentinelData(cipher)
        }

        if (state.bioPromptReason == BioPromptReason.RETURN_LOGIN) {
            handleBiometryReturnLogin()
        }
    }

    fun biometryFailed() {
        state = state.copy(loginResult = Resource.Error())
    }

    private fun saveSentinelData(cipher: Cipher) {
        viewModelScope.launch {
            state = try {
                keyRepository.saveSentinelData(cipher)
                state.copy(exitLoginFlow = Resource.Success(Unit))
            } catch (e: Exception) {
                keyRepository.handleKeyInvalidatedException(e)
                SignInState()
            }
        }
    }

    private fun handleBiometryReturnLogin() {
        viewModelScope.launch {
            try {
                val timestamp = keyRepository.generateTimestamp()
                val signedTimestamp = keyRepository.signTimestamp(
                    timestamp = timestamp
                )

                loginWithBiometry(timestamp = timestamp, signedTimestamp = signedTimestamp)
            } catch (e: Exception) {
                biometryFailed()
            }
        }
    }

    private fun checkToken() {
        if (state.verificationToken.isEmpty()) {
            state = state.copy(verificationTokenErrorEnabled = true)
        } else {
            attemptLogin()
        }
    }
    //endregion

    //region Login + API Calls

    fun sendVerificationEmail() {
        viewModelScope.launch {
            val verificationResource = userRepository.sendVerificationEmail(state.email)

            state = if (verificationResource is Resource.Success) {
                state.copy(
                    sendVerificationEmail = verificationResource,
                    loginStep = LoginStep.TOKEN_ENTRY
                )
            } else {
                state.copy(sendVerificationEmail = verificationResource)
            }
        }
    }
    private fun loginWithToken() {
        state = state.copy(loginResult = Resource.Loading())
        viewModelScope.launch {
            try {
                val loginResource = userRepository.loginWithVerificationToken(
                    email = state.email, token = state.verificationToken
                )
                when (loginResource) {
                    is Resource.Success -> {
                        val token = loginResource.data?.token
                        if (token != null && token.isNotEmpty()) {
                            userSuccessfullyLoggedIn(token)
                            saveSentinelDataToDevice()
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
    }

    private suspend fun loginWithBiometry(timestamp: String, signedTimestamp: String) {
        state = state.copy(loginResult = Resource.Loading())
        try {
            val loginResource = userRepository.loginWithTimestamp(
                email = state.email, timestamp = timestamp, signedTimestamp = signedTimestamp
            )
            when (loginResource) {
                is Resource.Success -> {
                    val token = loginResource.data?.token
                    if (token != null) {
                        userSuccessfullyLoggedIn(token)
                        handleReturnLoggedInUser()
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


    fun attemptLogin() {
        if (state.signInButtonEnabled) {
            loginWithToken()
        } else {
            state = state.copy(
                emailErrorEnabled = !state.emailValid(),
                verificationTokenErrorEnabled = !state.verificationTokenValid()
            )
        }
    }

    private suspend fun userSuccessfullyLoggedIn(token: String) {
        userRepository.saveUserEmail(state.email)
        userRepository.setUserLoggedIn()
        userRepository.saveToken(token)
        submitNotificationTokenForRegistration()
        state = state.copy(loginResult = Resource.Success(LoginResponse(token)))
    }

    private fun saveSentinelDataToDevice() {
        viewModelScope.launch {
            val cipher = keyRepository.getInitializedCipherForSentinelEncryption()

            if (cipher != null) {
                state = state.copy(
                    triggerBioPrompt = Resource.Success(cipher),
                    bioPromptReason = BioPromptReason.SAVE_SENTINEL
                )
            }
        }
    }

    private suspend fun handleReturnLoggedInUser() {
        if(keyRepository.haveSentinelData()) {
            state = state.copy(exitLoginFlow = Resource.Success(Unit))
        } else {
            saveSentinelDataToDevice()
        }
    }

    private fun userFailedLogin(resource: Resource<LoginResponse>? = null, e: Exception? = null) {
        state = if (resource != null) {
            state.copy(loginResult = resource)
        } else {
            state.copy(
                loginResult = Resource.Error(
                    exception = Exception(e?.message ?: NO_INTERNET_ERROR)
                )
            )
        }
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
            e.sendError(CrashReportingUtil.PUSH_NOTIFICATION_TAG)
        }
    }

    fun resetLoginCall() {
        state = state.copy(loginResult = Resource.Uninitialized)
    }

    fun resetSendVerificationEmail() {
        state = state.copy(sendVerificationEmail = Resource.Uninitialized)
    }

    fun resetExitLoginFlow() {
        state = state.copy(exitLoginFlow = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
}