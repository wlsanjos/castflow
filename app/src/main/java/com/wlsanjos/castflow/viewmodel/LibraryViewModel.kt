package com.wlsanjos.castflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlsanjos.castflow.data.LocalMediaService
import com.wlsanjos.castflow.model.Album
import com.wlsanjos.castflow.model.MediaItem
import com.wlsanjos.castflow.model.MediaSelectionStore
import com.wlsanjos.castflow.model.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab {
    PHOTOS,
    VIDEOS,
    ALBUMS
}

data class LibraryUiState(
    val photos: List<MediaItem> = emptyList(),
    val videos: List<MediaItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.PHOTOS,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasPermission: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaService: LocalMediaService,
    private val selectionStore: MediaSelectionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun onMediaClick(media: MediaItem) {
        selectionStore.setSelected(media)
    }

    fun onTabSelected(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        loadIfNeeded(tab)
    }

    private fun loadIfNeeded(tab: LibraryTab) {
        val state = _uiState.value
        when (tab) {
            LibraryTab.PHOTOS -> if (state.photos.isEmpty()) loadPhotos()
            LibraryTab.VIDEOS -> if (state.videos.isEmpty()) loadVideos()
            LibraryTab.ALBUMS -> if (state.albums.isEmpty()) loadAlbums()
        }
    }

    fun loadAll() {
        loadPhotos()
        loadVideos()
        loadAlbums()
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val photos = mediaService.getPhotos()
                _uiState.update { it.copy(photos = photos, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun loadVideos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val videos = mediaService.getVideos()
                _uiState.update { it.copy(videos = videos, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val albums = mediaService.getAlbums()
                _uiState.update { it.copy(albums = albums, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun updatePermissionStatus(hasPermission: Boolean) {
        _uiState.update { it.copy(hasPermission = hasPermission) }
        if (hasPermission) {
            loadAll()
        }
    }
}
