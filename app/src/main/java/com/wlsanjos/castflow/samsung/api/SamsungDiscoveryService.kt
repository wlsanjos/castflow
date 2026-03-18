package com.wlsanjos.castflow.samsung.api

import com.wlsanjos.castflow.samsung.models.ConnectionState
import com.wlsanjos.castflow.samsung.models.DiscoveryState
import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import kotlinx.coroutines.flow.Flow

/**
 * High-level abstraction for discovery and connection state streams.
 * Implementations should be lifecycle-aware and cancellable.
 */
interface SamsungDiscoveryService {
    /**
     * Observable DiscoveryState stream.
     */
    fun discoveryState(): Flow<DiscoveryState>

    /**
     * Start discovery of Samsung TVs on the network.
     */
    fun startDiscovery()

    /**
     * Stop discovery and release any network resources.
     */
    fun stopDiscovery()

    /**
     * Attempt to connect to a device (establish protocol-level session later).
     * For now this only updates ConnectionState; actual media send is out-of-scope.
     */
    fun connectTo(device: SamsungTvDevice)

    /**
     * Terminate connection if active.
     */
    fun disconnect()

    /**
     * Observable ConnectionState stream.
     */
    fun connectionState(): Flow<ConnectionState>
}
