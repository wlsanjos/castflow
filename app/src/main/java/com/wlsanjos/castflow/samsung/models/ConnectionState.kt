package com.wlsanjos.castflow.samsung.models

sealed interface ConnectionState {
    object Disconnected : ConnectionState
    object Connecting : ConnectionState
    object WaitingForApproval : ConnectionState
    data class Connected(val device: SamsungTvDevice) : ConnectionState
    data class Failed(val reason: String) : ConnectionState
}
