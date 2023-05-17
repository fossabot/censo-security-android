package com.censocustody.android.presentation.maintenance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.Resource
import com.censocustody.android.common.wrapper.CensoCountDownTimer
import com.censocustody.android.common.wrapper.CensoCountDownTimerImpl
import com.censocustody.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val timer: CensoCountDownTimer
) : ViewModel() {

    var state by mutableStateOf(MaintenanceState())
        private set

    fun onStart() {
        timer.startCountDownTimer(CensoCountDownTimerImpl.Companion.POLL_USER_COUNTDOWN) {
            retrieveUserVerifyDetails()
        }

        viewModelScope.launch {
            val userLoggedIn = userRepository.userLoggedIn()
            state = state.copy(userLoggedIn = userLoggedIn)
        }
    }

    fun onStop() {
        timer.stopCountDownTimer()
    }

    private fun retrieveUserVerifyDetails() {

        if (state.verifyUserResult is Resource.Uninitialized) {

            viewModelScope.launch {
                val verifyUserDataResource = userRepository.verifyUser()

                state = if (verifyUserDataResource is Resource.Success) {
                    state.copy(
                        verifyUserResult = verifyUserDataResource,
                        sendUserToEntrance = Resource.Success(true)
                    )
                } else {
                    state.copy(verifyUserResult = verifyUserDataResource)
                }
            }
        }
    }

    fun resetSendUserToEntrance() {
        state = state.copy(sendUserToEntrance = Resource.Uninitialized)
    }

    fun resetVerifyUserResult() {
        state = state.copy(verifyUserResult = Resource.Uninitialized)
    }

}