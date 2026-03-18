package com.wlsanjos.castflow.ui.screens

import androidx.compose.runtime.Composable
import com.wlsanjos.castflow.model.MediaItem

@Composable
fun HomeScreen(
    onNavigateToPreview: (MediaItem) -> Unit = {},
    onNavigateToDiscover: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    // Redirecting Home to Library which is the main screen in the new design
    LibraryScreen(
        onNavigateToPreview = onNavigateToPreview,
        onNavigateToDiscover = onNavigateToDiscover,
        onNavigateToSettings = onNavigateToSettings
    )
}
