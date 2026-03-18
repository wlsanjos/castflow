package com.wlsanjos.castflow.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaSelectionStore @Inject constructor() {
    private val _selectedMedia = MutableStateFlow<MediaItem?>(null)
    val selectedMedia = _selectedMedia.asStateFlow()

    fun setSelected(media: MediaItem) {
        _selectedMedia.value = media
    }

    fun clear() {
        _selectedMedia.value = null
    }
}
