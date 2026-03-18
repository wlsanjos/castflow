package com.wlsanjos.castflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlsanjos.castflow.model.MediaItem
import com.wlsanjos.castflow.model.MediaSelectionStore
import com.wlsanjos.castflow.samsung.state.ConnectedDeviceStore
import com.wlsanjos.castflow.samsung.api.SamsungCastService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val media: MediaItem? = null,
    val deviceName: String? = null
)

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val selectionStore: MediaSelectionStore,
    private val deviceStore: ConnectedDeviceStore,
    private val castService: SamsungCastService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            selectionStore.selectedMedia.collect { media ->
                _uiState.update { it.copy(
                    media = media,
                    durationMs = media?.duration ?: 0L
                ) }
            }
        }
        viewModelScope.launch {
            deviceStore.device.collect { device ->
                _uiState.update { it.copy(deviceName = device?.name) }
            }
        }
    }

    fun play() { 
        _uiState.update { it.copy(isPlaying = true) }
        // TODO: implement real play command to TV
    }
    
    fun pause() { 
        _uiState.update { it.copy(isPlaying = false) }
        // TODO: implement real pause command to TV
    }
    
    fun stopCasting() {
        castService.stopCasting()
    }
}
