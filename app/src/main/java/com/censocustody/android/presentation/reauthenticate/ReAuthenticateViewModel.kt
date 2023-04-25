package com.censocustody.android.presentation.reauthenticate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.NoInternetException
import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.LoginResponse
import com.censocustody.android.data.repository.KeyRepository
import com.censocustody.android.data.repository.UserRepository
import com.censocustody.android.data.storage.CensoUserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReAuthenticateViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val censoUserData: CensoUserData
) : ViewModel() {

    var state by mutableStateOf(ReAuthenticateState())
        private set

    fun onStart() {
        triggerBioPrompt()
    }

    fun biometryApproved() {
        handleBiometryReturnLogin()
    }

    fun biometryFailed() {
        state = state.copy(loginResult = Resource.Error(exception = Exception("Failed Biometry")))
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

    private suspend fun loginWithBiometry(timestamp: String, signedTimestamp: String) {
        state = state.copy(loginResult = Resource.Loading())
        try {
            val email = userRepository.retrieveUserEmail()

            val loginResource = userRepository.loginWithTimestamp(
                email = email, timestamp = timestamp, signedTimestamp = signedTimestamp
            )

            when (loginResource) {
                is Resource.Success -> {
                    val token = loginResource.data?.token
                    if (token != null) {
                        userSuccessfullyLoggedIn(email = email, token = token)
                        state = state.copy(
                            loginResult = Resource.Success(
                                LoginResponse(token)
                            )
                        )
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

    private suspend fun userSuccessfullyLoggedIn(email: String, token: String) {
        userRepository.saveUserEmail(email)
        censoUserData.setEmail(email)
        userRepository.setUserLoggedIn()
        userRepository.saveToken(token)
    }

    private fun userFailedLogin(resource: Resource<LoginResponse>? = null, e: Exception? = null) {
        state = if (resource != null) {
            state.copy(loginResult = resource)
        } else {
            state.copy(
                loginResult = Resource.Error(
                    exception = Exception(e?.message ?: NoInternetException.NO_INTERNET_ERROR)
                )
            )
        }
    }

    fun retry() {
        resetLoginResult()
        triggerBioPrompt()
    }

    fun triggerBioPrompt() {
        state = state.copy(triggerBioPrompt = Resource.Success(Unit))
    }

    fun resetLoginResult() {
        state = state.copy(loginResult = Resource.Uninitialized)
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
}