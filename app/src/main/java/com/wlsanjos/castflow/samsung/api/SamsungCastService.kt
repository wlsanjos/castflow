package com.wlsanjos.castflow.samsung.api

import com.wlsanjos.castflow.model.MediaItem
import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import kotlinx.coroutines.flow.Flow

sealed class CastState {
    object Idle : CastState()
    object Casting : CastState()
    data class Success(val media: MediaItem) : CastState()
    data class Error(val message: String) : CastState()
}

interface SamsungCastService {
    fun castState(): Flow<CastState>
    fun castMedia(media: MediaItem, device: SamsungTvDevice)
    fun stopCasting()
}
