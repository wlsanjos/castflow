package com.wlsanjos.castflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlsanjos.castflow.model.MediaItem
import com.wlsanjos.castflow.model.MediaSelectionStore
import com.wlsanjos.castflow.samsung.api.CastState
import com.wlsanjos.castflow.samsung.api.SamsungCastService
import com.wlsanjos.castflow.samsung.state.ConnectedDeviceStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreviewUiState(
    val media: MediaItem? = null,
    val isReadyToCast: Boolean = false,
    val connectedDeviceName: String? = null,
    val castState: CastState = CastState.Idle
)

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val castService: SamsungCastService,
    private val deviceStore: ConnectedDeviceStore,
    private val selectionStore: MediaSelectionStore
) : ViewModel() {

    sealed class UiEvent {
        object NavigateToPlayback : UiEvent()
    }

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<UiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            selectionStore.selectedMedia.collect { media ->
                _uiState.update { it.copy(media = media, isReadyToCast = media != null) }
            }
        }
        viewModelScope.launch {
            deviceStore.device.collect { device ->
                _uiState.update { it.copy(connectedDeviceName = device?.name) }
            }
        }

        viewModelScope.launch {
            castService.castState().collect { state ->
                _uiState.update { it.copy(castState = state) }
                if (state is CastState.Success) {
                    _uiEvents.send(UiEvent.NavigateToPlayback)
                }
            }
        }
    }

    fun castMedia() {
        val media = _uiState.value.media ?: return
        val device = deviceStore.device.value ?: return
        castService.castMedia(media, device)
    }
}
