package com.wlsanjos.castflow.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class HomeUiState(
    val pairedTvName: String? = null,
    val isDiscovering: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun startDiscovery() {
        _uiState.value = _uiState.value.copy(isDiscovering = true)
        // TODO: kick off discovery flow (Samsung TV integration) in next iteration
    }
}
