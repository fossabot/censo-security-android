package com.censocustody.android.presentation.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.Resource
import com.censocustody.android.data.repository.PushRepository
import com.censocustody.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
) : ViewModel() {

    var state by mutableStateOf(AccountState())
        private set

    fun onStart() {
        viewModelScope.launch {
            setUserInfo()
        }
    }

    private suspend fun setUserInfo() {
        state = state.copy(
            email = userRepository.retrieveUserEmail(),
            name = userRepository.getCachedUser()?.fullName ?: ""
        )
    }

    fun logout() {
        viewModelScope.launch {
            state = state.copy(logoutResult = Resource.Loading())
            try {
                pushRepository.removePushNotification()
            } catch (e: Exception) {
                //continue logging out
            }
            state = try {
                val loggedOut = userRepository.logOut()
                state.copy(logoutResult = Resource.Success(loggedOut))
            } catch (e: Exception) {
                state.copy(logoutResult = Resource.Success(false))
            }
        }
    }

    fun resetLogoutResource() {
        state = state.copy(logoutResult = Resource.Uninitialized)
    }
}