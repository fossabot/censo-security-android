package com.censocustody.android.presentation.pending_approval

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.CensoCountDownTimer
import com.censocustody.android.common.CensoCountDownTimerImpl
import com.censocustody.android.common.CensoError
import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import com.censocustody.android.data.repository.BaseRepository.Companion.CONFLICT_CODE
import com.censocustody.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingApprovalViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val timer: CensoCountDownTimer
) : ViewModel() {

    var state by mutableStateOf(PendingApprovalState())
        private set

    fun onStart() {
        timer.startCountDownTimer(CensoCountDownTimerImpl.Companion.POLL_USER_COUNTDOWN) {
            retrieveUserVerifyDetails()
        }
    }

    fun onStop() {
        timer.stopCountDownTimer()
    }

    fun retrieveUserVerifyDetails() {

        if (state.verifyUserResult is Resource.Uninitialized) {

            viewModelScope.launch {

                val verifyUserDataResource = userRepository.verifyUser()

                if (verifyUserDataResource is Resource.Success) {
                    val verifyUser = verifyUserDataResource.data

                    state = if (verifyUser?.canAddSigners == true) {
                        state.copy(
                            verifyUserResult = verifyUserDataResource,
                            sendUserToEntrance = Resource.Success(true)
                        )
                    } else {
                        state.copy(verifyUserResult = verifyUserDataResource)
                    }

                } else if (verifyUserDataResource is Resource.Error) {
                    state = if (verifyUserDataResource.censoError is CensoError.DefaultApiError
                        && verifyUserDataResource.censoError.statusCode == CONFLICT_CODE
                    ) {
                        state.copy(sendUserToEntrance = Resource.Success(true))
                    } else {
                        state.copy(verifyUserResult = verifyUserDataResource)
                    }
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