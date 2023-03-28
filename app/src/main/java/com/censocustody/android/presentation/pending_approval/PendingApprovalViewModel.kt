package com.censocustody.android.presentation.pending_approval

import com.censocustody.android.presentation.key_creation.KeyCreationState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.censocustody.android.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PendingApprovalViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
) : ViewModel() {

    var state by mutableStateOf(KeyCreationState())
        private set

    //region VM SETUP
    fun onStart(initialData: PendingApprovalInitialData) {
        state = state.copy(
            verifyUserDetails = initialData.verifyUserDetails,
        )
    }

    fun cleanUp() {

    }
}