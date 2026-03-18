package com.wlsanjos.castflow.model

import android.net.Uri

/**
 * Simple UI model representing a media item from the gallery.
 * Prepared for extension with metadata used to drive casting logic.
 */
data class MediaItem(
    val id: String,
    val title: String,
    val uri: Uri,
    val isVideo: Boolean = false,
)
