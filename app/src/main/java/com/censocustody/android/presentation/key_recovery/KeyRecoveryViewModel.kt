package com.censocustody.android.presentation.key_recovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class KeyRecoveryViewModel @Inject constructor(
) : ViewModel() {

    var state by mutableStateOf(KeyRecoveryState())
        private set

    fun onStart(initialData: KeyRecoveryInitialData) {
        state = state.copy(verifyUserDetails = initialData.verifyUserDetails)
    }
}