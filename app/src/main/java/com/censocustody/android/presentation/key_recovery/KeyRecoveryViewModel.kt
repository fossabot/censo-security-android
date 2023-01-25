package com.censocustody.android.presentation.key_recovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.censocustody.android.data.KeyRepository
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.CipherRepository
import com.censocustody.android.presentation.key_creation.KeyCreationState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class KeyRecoveryViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val cipherRepository: CipherRepository
) : ViewModel() {

    var state by mutableStateOf(KeyRecoveryState())
        private set

    //region VM SETUP
    fun onStart() {
    }

    fun cleanUp() {
    }
}