package com.wlsanjos.castflow.samsung.api

import com.wlsanjos.castflow.samsung.models.ConnectionState
import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import kotlinx.coroutines.flow.Flow

/**
 * Service to manage the real-time communication channel with a Samsung TV.
 * Handles pairing handshakes and keeps track of the connection session.
 */
interface SamsungConnectionService {
    /**
     * Observable stream of the current connection status.
     */
    fun connectionState(): Flow<ConnectionState>

    /**
     * Start the connection/pairing process with the specified device.
     * @param channelId The WebSocket channel to connect to. 
     * Defaults to "samsung.remote.control" for remote keys.
     * Use "com.samsung.multiscreen.cast" for media transmission.
     */
    fun connect(device: SamsungTvDevice, channelId: String = "samsung.remote.control")

    /**
     * Terminate the connection and cleanup resources.
     */
    fun disconnect()

    /**
     * Send a raw WebSocket message to the TV.
     */
    fun sendMessage(text: String): Boolean
}
