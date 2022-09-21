package com.strikeprotocols.mobile.presentation.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.raygun.raygun4android.RaygunClient
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.strikeprotocols.mobile.data.NoInternetException.Companion.NO_INTERNET_ERROR
import com.strikeprotocols.mobile.data.models.LoginResponse
import com.strikeprotocols.mobile.data.models.PushBody
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementInitialData
import kotlinx.coroutines.*
import javax.crypto.Cipher

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
    private val keyRepository: KeyRepository,
    private val strikeUserData: StrikeUserData
) : ViewModel() {

    var state by mutableStateOf(SignInState())
        private set

    init {
        getCachedEmail()
    }


    fun onStart() {
        viewModelScope.launch {
            val userLoggedIn = try {
                userRepository.userLoggedIn()
            } catch (e: Exception) {
                false
            }

            val haveSentinelData = keyRepository.haveSentinelData()

            if (userLoggedIn && !haveSentinelData) {
                val email = userRepository.retrieveCachedUserEmail()
                val cipher = keyRepository.getCipherForEncryption(SENTINEL_KEY_NAME)
                state = state.copy(
                    loginStep = LoginStep.PASSWORD_ENTRY,
                    email = email,
                    triggerBioPrompt = Resource.Success(cipher),
                    bioPromptReason = BioPromptReason.SAVE_SENTINEL
                )
            }
        }
    }

    private fun getCachedEmail() {
        viewModelScope.launch {
            val email = userRepository.retrieveCachedUserEmail()
            state = state.copy(email = email)
        }
    }

    //region Handle User Input
    fun updateEmail(updatedEmail: String) {
        val sanitizedEmail = updatedEmail.lowercase().trim()
        state = state.copy(email = sanitizedEmail, emailErrorEnabled = false)
        viewModelScope.launch { userRepository.saveUserEmail(sanitizedEmail) }
    }

    fun updatePassword(updatedPassword: String) {
        state = state.copy(password = updatedPassword, passwordErrorEnabled = false)
    }

    fun moveBackToEmailScreen() {
        state = state.copy(loginStep = LoginStep.EMAIL_ENTRY)
    }

    fun signInActionCompleted() {
        if (state.loginStep == LoginStep.EMAIL_ENTRY) {
            checkEmail()
        } else {
            checkPassword()
        }
    }

    private fun checkEmail() {
        if (state.email.isEmpty()) {
            state = state.copy(emailErrorEnabled = true)
        } else {
            kickOffBiometryLoginOrMoveToPasswordEntry()
        }
    }

    fun kickOffBiometryLoginOrMoveToPasswordEntry() {
        viewModelScope.launch {
            if (keyRepository.havePrivateKey()) {
                val cipher = keyRepository.getCipherForPrivateKeyDecryption()
                if (cipher != null) {
                    state = state.copy(
                        triggerBioPrompt = Resource.Success(cipher),
                        bioPromptReason = BioPromptReason.RETURN_LOGIN,
                        loginResult = Resource.Uninitialized
                    )
                }
            } else {
                state = state.copy(loginStep = LoginStep.PASSWORD_ENTRY)
            }
        }
    }

    fun biometryApproved(cipher: Cipher) {
        when(state.bioPromptReason) {
            BioPromptReason.SAVE_SENTINEL -> saveSentinelData(cipher)
            BioPromptReason.RETURN_LOGIN -> handleBiometryReturnLogin(cipher)
            else -> {}
        }
    }

    fun biometryFailed() {
        state = state.copy(loginResult = Resource.Error())
    }

    private fun saveSentinelData(cipher: Cipher) {
        viewModelScope.launch {
            keyRepository.saveSentinelData(cipher)
            state = state.copy(exitLoginFlow = Resource.Success(Unit))
        }
    }

    private fun handleBiometryReturnLogin(cipher: Cipher) {
        viewModelScope.launch {
            try {
                val timestamp = keyRepository.generateTimestamp()
                val signedTimestamp = keyRepository.signTimestamp(
                    timestamp = timestamp,
                    cipher = cipher
                )

                loginWithBiometry(timestamp = timestamp, signedTimestamp = signedTimestamp)
            } catch (e: Exception) {
                biometryFailed()
            }
        }
    }

    private fun checkPassword() {
        if (state.password.isEmpty()) {
            state = state.copy(passwordErrorEnabled = true)
        } else {
            attemptLogin()
        }
    }
    //endregion

    //region Login + API Calls
    private fun loginWithPassword() {
        state = state.copy(loginResult = Resource.Loading())
        viewModelScope.launch {
            try {
                val loginResource = userRepository.loginWithPassword(
                    email = state.email, password = state.password
                )
                when (loginResource) {
                    is Resource.Success -> {
                        val token = loginResource.data?.token
                        if (token != null) {
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
                        handleSuccessfulBiometryLogin()
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
            loginWithPassword()
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
        userRepository.saveToken(token)
        submitNotificationTokenForRegistration()
        state = state.copy(loginResult = Resource.Success(LoginResponse(token)))
    }

    private suspend fun saveSentinelDataToDevice() {
        val cipher = keyRepository.getCipherForEncryption(SENTINEL_KEY_NAME)
        state = state.copy(
            triggerBioPrompt = Resource.Success(cipher),
            bioPromptReason = BioPromptReason.SAVE_SENTINEL
        )
    }

    private suspend fun handleSuccessfulBiometryLogin() {
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

    fun resetExitLoginFlow() {
        state = state.copy(exitLoginFlow = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
}