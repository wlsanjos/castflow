package com.wlsanjos.castflow.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.wlsanjos.castflow.model.MediaItem

data class PreviewUiState(
    val media: MediaItem? = null,
    val isReadyToCast: Boolean = false
)

@HiltViewModel
class PreviewViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState

    fun prepareCast(item: MediaItem) {
        _uiState.value = _uiState.value.copy(media = item, isReadyToCast = true)
        // TODO: prepare the media for casting (transcoding/URL provisioning)
    }
}
