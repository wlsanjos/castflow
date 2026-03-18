package com.wlsanjos.castflow.samsung.models

import android.net.Uri

/**
 * Represents a discovered Samsung TV device on the local network.
 * Keep fields minimal so it can be populated by different discovery mechanisms.
 */
data class SamsungTvDevice(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 0,
    val model: String? = null,
    val descriptionUri: Uri? = null,
    val mac: String? = null,
    val token: String? = null
)
