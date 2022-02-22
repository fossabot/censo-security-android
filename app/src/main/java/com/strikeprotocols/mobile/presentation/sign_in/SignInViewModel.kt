package com.strikeprotocols.mobile.presentation.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.NoInternetException
import com.strikeprotocols.mobile.data.NoInternetException.Companion.NO_INTERNET_ERROR
import com.strikeprotocols.mobile.domain.use_case.SignInUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    var state by mutableStateOf(SignInState())
        private set

    fun updateEmail(updatedEmail: String) {
        state = state.copy(email = updatedEmail, emailErrorEnabled = false)
    }

    fun updatePassword(updatedPassword: String) {
        state = state.copy(password = updatedPassword, passwordErrorEnabled = false)
    }

    fun attemptLogin() {
        if (state.signInButtonEnabled) {

            val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
                state = state.copy(
                    loginResult = Resource.Error(
                        NoInternetException().message ?: NO_INTERNET_ERROR
                    )
                )
            }

            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                signInUseCase.execute(state.email, state.password).onEach { result ->
                    state = when (result) {
                        is Resource.Success, is Resource.Error -> {
                            state.copy(loginResult = result)
                        }
                        else -> {
                            state.copy(loginResult = Resource.Loading())
                        }
                    }
                }.launchIn(this)
            }

        } else {
            state = state.copy(
                emailErrorEnabled = !state.emailValid(),
                passwordErrorEnabled = !state.passwordValid()
            )
        }
    }

    fun resetLoginCall() {
        state = state.copy(loginResult = Resource.Uninitialized)
    }

}