package com.wlsanjos.castflow

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache

@HiltAndroidApp
class CastFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configure a single ImageLoader with conservative memory/disk cache settings
        // to improve thumbnail performance and reduce OOM risk.
        val imageLoader = ImageLoader.Builder(this)
            .crossfade(true)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    // Use a modest fraction of app memory for image cache.
                    .maxSizePercent(0.2)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    // 64 MB disk cache for thumbnails
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()

        Coil.setImageLoader(imageLoader)
    }
}
