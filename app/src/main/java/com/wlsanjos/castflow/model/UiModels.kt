package com.wlsanjos.castflow.model

import android.net.Uri

/**
 * Type of media supported by the application.
 */
enum class MediaType {
    PHOTO,
    VIDEO
}

/**
 * Simple UI model representing a media item from the gallery.
 */
data class MediaItem(
    val id: String,
    val title: String,
    val uri: Uri,
    val type: MediaType,
    val mimeType: String? = null,
    val duration: Long? = null, // In milliseconds, for videos
    val size: Long = 0,
    val dateModified: Long = 0,
    val albumName: String? = null
)

/**
 * UI model representing a group of media items.
 */
data class Album(
    val id: String,
    val name: String,
    val thumbnailUri: Uri?,
    val mediaCount: Int,
    val isVideoAlbum: Boolean = false
)
