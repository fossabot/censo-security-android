package com.strikeprotocols.mobile.presentation.approvals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.data.ApprovalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovalsViewModel @Inject constructor(
    private val approvalsRepository: ApprovalsRepository
) : ViewModel() {

    fun checkToken() {
        viewModelScope.launch {
            approvalsRepository.getApprovals()
        }
    }
}