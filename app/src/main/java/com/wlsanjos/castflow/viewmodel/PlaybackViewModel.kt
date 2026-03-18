package com.wlsanjos.castflow.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L
)

@HiltViewModel
class PlaybackViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState

    fun play() { _uiState.value = _uiState.value.copy(isPlaying = true) }
    fun pause() { _uiState.value = _uiState.value.copy(isPlaying = false) }
}
