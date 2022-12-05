package com.censocustody.mobile.presentation.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.PushRepository
import com.censocustody.mobile.data.StrikeUserData
import com.censocustody.mobile.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
    private val strikeUserData: StrikeUserData
) : ViewModel() {

    var state by mutableStateOf(AccountState())
        private set

    fun onStart() {
        setUserInfo()
    }

    private fun setUserInfo() {
        state = state.copy(
            email = strikeUserData.getEmail(),
            name = strikeUserData.getStrikeUser()?.fullName ?: ""
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