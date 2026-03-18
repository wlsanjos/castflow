package com.wlsanjos.castflow.samsung.models

sealed interface DiscoveryState {
    object Idle : DiscoveryState
    object Searching : DiscoveryState
    data class Found(val devices: List<SamsungTvDevice>) : DiscoveryState
    data class Error(val message: String) : DiscoveryState
}
