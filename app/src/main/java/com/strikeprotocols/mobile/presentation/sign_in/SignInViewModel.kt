package com.strikeprotocols.mobile.presentation.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.domain.use_case.SignInUseCase
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
            signInUseCase.execute().onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        state = state.copy(loginResult = result)
                    }
                    is Resource.Error -> {
                        throw NotImplementedError()
                    }
                    else -> {
                        state = state.copy(loginResult = Resource.Loading())
                    }
                }

            }.launchIn(viewModelScope)
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