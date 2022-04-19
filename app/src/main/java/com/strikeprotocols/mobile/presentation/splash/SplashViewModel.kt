package com.strikeprotocols.mobile.presentation.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.PushRepository
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.PushBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(SplashState())
        private set

    fun onStart() {
        viewModelScope.launch {
            delay(500)
            val userLoggedIn = try {
                userRepository.userLoggedIn()
            } catch (e: Exception) {
                false
            }
            state = state.copy(userLoggedInResult = Resource.Success(userLoggedIn))
        }
    }

    fun resetLoggedInResource() {
        state = state.copy(userLoggedInResult = Resource.Uninitialized)
    }
}