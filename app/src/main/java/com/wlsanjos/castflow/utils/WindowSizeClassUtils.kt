package com.wlsanjos.castflow.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

enum class WindowSize { Compact, Medium, Expanded }

@Composable
fun currentWindowSize(): WindowSize {
    val widthDp = LocalConfiguration.current.screenWidthDp.dp
    return when {
        widthDp < 600.dp -> WindowSize.Compact
        widthDp < 840.dp -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
}
