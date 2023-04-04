package com.censocustody.android.presentation.key_recovery

import com.censocustody.android.presentation.key_creation.KeyCreationState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.censocustody.android.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class KeyRecoveryViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
) : ViewModel() {

    var state by mutableStateOf(KeyRecoveryState())
        private set

    //region VM SETUP
    fun onStart(initialData: KeyRecoveryInitialData) {
    }

    fun cleanUp() {
        state = KeyRecoveryState()
    }
}