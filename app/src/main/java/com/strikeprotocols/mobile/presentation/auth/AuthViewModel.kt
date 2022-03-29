package com.strikeprotocols.mobile.presentation.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.data.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(AuthState())
        private set

    fun goBackToSetup() {
        state = state.copy(authStep = AuthStep.SETUP)
    }

    fun updateStep() {
        viewModelScope.launch {
            when (state.authStep) {
                AuthStep.SETUP -> state =
                    state.copy(authStep = AuthStep.BIOMETRIC, triggerBioPrompt = true)
                AuthStep.BIOMETRIC -> {
                    state = state.copy(authStep = AuthStep.PROCESSING)
                    delay(3000)
                    state = state.copy(authStep = AuthStep.FINISHED)
                }
                AuthStep.FINISHED -> state = state.copy(authStep = AuthStep.LEAVE_SCREEN)
                AuthStep.PROCESSING, AuthStep.LEAVE_SCREEN -> {}
            }
        }
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = false)
    }
}