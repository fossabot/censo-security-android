package com.strikeprotocols.mobile.presentation.reset_password

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    var state by mutableStateOf(ResetPasswordState())
        private set

    fun updateEmail(updatedEmail: String) {
        state = state.copy(email = updatedEmail, emailErrorEnabled = false)
    }

    fun submitResetPassword() {
        if (state.resetPasswordResult is Resource.Loading) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            state = state.copy(resetPasswordResult = Resource.Loading())
            state = try {
                userRepository.resetPassword(state.email)
                state.copy(
                    resetPasswordResult = Resource.Success("")
                )
            } catch (e: Exception) {
                state.copy(
                    resetPasswordResult = Resource.Error(e.message ?: ""),
                    emailErrorEnabled = true
                )
            }
        }
    }
}