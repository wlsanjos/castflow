package com.wlsanjos.castflow.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.request.ImageRequest
import coil.size.Size
import coil.size.Precision
import coil.size.Scale
import java.util.Locale

suspend fun getVideoDurationMillis(context: Context, uri: Uri): Long? = withContext(Dispatchers.IO) {
    try {
        val projection = arrayOf(MediaStore.Video.Media.DURATION)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                return@withContext cursor.getLong(idx)
            }
        }
    } catch (e: Exception) {
        // ignore and return null
    }
    return@withContext null
}

fun formatDuration(millis: Long?): String? {
    if (millis == null) return null
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

fun buildCoilImageRequest(
    context: Context,
    data: Any?,
    targetPx: Int,
    precision: coil.size.Precision = Precision.EXACT,
    allowHardware: Boolean = false
): ImageRequest {
    // Use exact precision for thumbnails to avoid decoding larger bitmaps than needed.
    // Disable hardware bitmaps for better transform and memory predictability on older devices.
    return ImageRequest.Builder(context)
        .data(data)
        .size(Size(targetPx, targetPx))
        .precision(precision)
        .scale(Scale.FILL)
        .allowHardware(allowHardware)
        .crossfade(true)
        .build()
}
