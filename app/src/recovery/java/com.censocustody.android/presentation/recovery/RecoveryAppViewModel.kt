package com.censocustody.android.presentation.recovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.data.repository.KeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
data class RecoveryAppViewModel @Inject constructor(
    private val keyRepository: KeyRepository
) : ViewModel() {

    var state by mutableStateOf(RecoveryAppState())
        private set

    fun createPhrase() {
        viewModelScope.launch {
            val generatedPhrase = keyRepository.generatePhrase()
            state = state.copy(generatedPhrase = generatedPhrase)
        }
    }

}