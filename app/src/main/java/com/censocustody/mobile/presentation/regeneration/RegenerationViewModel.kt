package com.strikeprotocols.mobile.presentation.regeneration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.MigrationRepository
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.mapToPublicKeysList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegenerationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val migrationRepository: MigrationRepository
) : ViewModel() {

    var state by mutableStateOf(RegenerationState())
        private set

    //region VM SETUP
    fun onStart() {
        viewModelScope.launch {
            handleKeyReUploadKeys()
        }
    }

    private suspend fun handleKeyReUploadKeys() {
        val publicKeys = migrationRepository.retrieveV3PublicKeys()

        val walletSignerResource =
            userRepository.addWalletSigner(publicKeys.mapToPublicKeysList())

        if (walletSignerResource is Resource.Success) {
            state = state.copy(finishedRegeneration = true)
        } else if (walletSignerResource is Resource.Error) {
            state =
                state.copy(regenerationError = Resource.Success(walletSignerResource.strikeError))
        }
    }

    fun retryRegeneration() {
        viewModelScope.launch {
            handleKeyReUploadKeys()
        }
    }

    fun resetAddWalletSignerCall() {
        state = state.copy(
            addWalletSigner = Resource.Uninitialized,
            finishedRegeneration = false
        )
    }
}