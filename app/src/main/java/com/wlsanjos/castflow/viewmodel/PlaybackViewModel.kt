package com.wlsanjos.castflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlsanjos.castflow.model.MediaItem
import com.wlsanjos.castflow.model.MediaSelectionStore
import com.wlsanjos.castflow.samsung.state.ConnectedDeviceStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val media: MediaItem? = null,
    val deviceName: String? = null
)

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val selectionStore: MediaSelectionStore,
    private val deviceStore: ConnectedDeviceStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            selectionStore.selectedMedia.collect { media ->
                _uiState.update { it.copy(media = media) }
            }
        }
        viewModelScope.launch {
            deviceStore.device.collect { device ->
                _uiState.update { it.copy(deviceName = device?.name) }
            }
        }
    }

    fun play() { _uiState.update { it.copy(isPlaying = true) } }
    fun pause() { _uiState.update { it.copy(isPlaying = false) } }
    
    fun stopCasting() {
        // TODO: call castService.stopCasting()
    }
}
