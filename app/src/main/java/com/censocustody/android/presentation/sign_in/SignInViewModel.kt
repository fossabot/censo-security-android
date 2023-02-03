package com.censocustody.android.presentation.sign_in

import androidx.biometric.BiometricPrompt.CryptoObject
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
import com.censocustody.android.data.EncryptionManagerImpl.Companion.SENTINEL_KEY_NAME
import com.censocustody.android.data.NoInternetException.Companion.NO_INTERNET_ERROR
import com.censocustody.android.data.models.CipherRepository
import com.censocustody.android.data.models.LoginResponse
import com.censocustody.android.data.models.PushBody
import kotlinx.coroutines.*
import java.security.Signature
import javax.crypto.Cipher
import kotlin.random.Random

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
    private val cipherRepository: CipherRepository,
    private val keyRepository: KeyRepository,
    private val censoUserData: CensoUserData
) : ViewModel() {

    var state by mutableStateOf(SignInState())
        private set

    init {
        getCachedEmail()
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
        viewModelScope.launch {
            val userLoggedIn = userRepository.userLoggedIn()

            if (userLoggedIn) {
                handleReturnLoggedInUser()
                return@launch
            }

            if (state.loginStep == LoginStep.EMAIL_ENTRY) {
                checkEmail()
            } else {
                checkPassword()
            }
        }
    }

    private fun checkEmail() {
        if (state.email.isEmpty()) {
            state = state.copy(emailErrorEnabled = true)
        } else {
            kickOffBiometryLoginOrMoveToPasswordEntry()
        }
    }

    fun skipToPasswordEntry() {
        state = state.copy(loginStep = LoginStep.PASSWORD_ENTRY)
    }

    fun kickOffBiometryLoginOrMoveToPasswordEntry() {
        viewModelScope.launch {
            if (keyRepository.hasV3RootSeedStored()) {
                val email = userRepository.retrieveUserEmail()
                val deviceId = userRepository.retrieveUserDeviceId(email)
                val signature = cipherRepository.getSignatureForDeviceSigning(deviceId)
                if (signature != null) {
                    state = state.copy(
                        triggerBioPrompt = Resource.Success(CryptoObject(signature)),
                        bioPromptReason = BioPromptReason.RETURN_LOGIN,
                        loginResult = Resource.Uninitialized
                    )
                }
            } else {
                state = state.copy(loginStep = LoginStep.PASSWORD_ENTRY)
            }
        }
    }

    fun biometryApproved(cryptoObject: CryptoObject) {
        if (state.bioPromptReason == BioPromptReason.SAVE_SENTINEL && cryptoObject.cipher != null) {
            saveSentinelData(cryptoObject.cipher!!)
        }

        if (state.bioPromptReason == BioPromptReason.RETURN_LOGIN && cryptoObject.signature != null) {
            handleBiometryReturnLogin(cryptoObject.signature!!)
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
                keyRepository.removeSentinelDataAndKickUserToAppEntrance()
                SignInState()
            }
        }
    }

    private fun handleBiometryReturnLogin(signature: Signature) {
        viewModelScope.launch {
            try {
                val timestamp = keyRepository.generateTimestamp()
                val signedTimestamp = keyRepository.signTimestamp(
                    timestamp = timestamp,
                    signature = signature
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
                        handleReturnLoggedInUser()
                    } else {
                        userFailedLogin(e = Exception("NO TOKEN"))
                    }
                }
                is Resource.Error -> {
                    state = state.copy(biometricLoginPreviousFailure = true)
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
        censoUserData.setEmail(state.email)
        userRepository.setUserLoggedIn()
        userRepository.saveToken(token)
        submitNotificationTokenForRegistration()
        state = state.copy(loginResult = Resource.Success(LoginResponse(token)))
    }

    private suspend fun saveSentinelDataToDevice() {
        val cipher = cipherRepository.getCipherForEncryption(SENTINEL_KEY_NAME)
        if (cipher != null) {
            state = state.copy(
                triggerBioPrompt = Resource.Success(CryptoObject(cipher)),
                bioPromptReason = BioPromptReason.SAVE_SENTINEL
            )
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