package com.strikeprotocols.mobile.presentation.device_registration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.CryptographyManager
import com.strikeprotocols.mobile.data.SharedPrefsHelper
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.presentation.entrance.EntranceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DeviceRegistrationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cryptographyManager: CryptographyManager
) : ViewModel() {

    var state by mutableStateOf(EntranceState())
        private set


    fun onStart() {
        viewModelScope.launch {
            val isUserLoggedIn = userRepository.userLoggedIn()

            if (isUserLoggedIn) {
                checkUserHasDeviceKey()
            }
        }
    }

    private suspend fun checkUserHasDeviceKey() {
        val userEmail = userRepository.retrieveUserEmail()
        val deviceId = SharedPrefsHelper.getDeviceId(userEmail)

        if (deviceId.isNotEmpty()) {
            checkUserInformationOnBackend(deviceId)
        }
    }

    private suspend fun checkUserInformationOnBackend(deviceId: String) {
        val verifyUserCall = userRepository.verifyUser()

        if (verifyUserCall is Resource.Success) {
            val user = verifyUserCall.data
            val localDevicePublicKey =
                cryptographyManager.getPublicKeyFromKeystore(deviceId)

            if (BaseWrapper.encode(localDevicePublicKey) == user?.deviceKey) {
                strikeLog(message = "User has registered key and have current key set.")
            } else {
                strikeLog(message = "User has not registered key")
            }
        }
    }
}