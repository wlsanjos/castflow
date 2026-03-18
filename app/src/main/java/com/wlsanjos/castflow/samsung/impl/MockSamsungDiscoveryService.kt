package com.wlsanjos.castflow.samsung.impl

import com.wlsanjos.castflow.samsung.api.SamsungDiscoveryService
import com.wlsanjos.castflow.samsung.models.ConnectionState
import com.wlsanjos.castflow.samsung.models.DiscoveryState
import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Simple mock discovery used for development and for environments where network
 * discovery is not desirable. Emits a searching state then a Found state with
 * a couple of fake devices.
 */
class MockSamsungDiscoveryService(private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) : SamsungDiscoveryService {

    private val _discovery = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    private val _connection = MutableStateFlow<com.wlsanjos.castflow.samsung.models.ConnectionState>(ConnectionState.Disconnected)

    override fun discoveryState(): Flow<DiscoveryState> = _discovery.asStateFlow()
    override fun connectionState(): Flow<com.wlsanjos.castflow.samsung.models.ConnectionState> = _connection.asStateFlow()

    override fun startDiscovery() {
        scope.launch {
            _discovery.emit(DiscoveryState.Searching)
            delay(1200)
            val dev1 = SamsungTvDevice(id = UUID.randomUUID().toString(), name = "Samsung-TV-LivingRoom", host = "192.168.1.47", port = 8001, model = "UE55")
            val dev2 = SamsungTvDevice(id = UUID.randomUUID().toString(), name = "Samsung-TV-Bedroom", host = "192.168.1.53", port = 8001, model = "Q90")
            _discovery.emit(DiscoveryState.Found(listOf(dev1, dev2)))
        }
    }

    override fun stopDiscovery() {
        scope.launch { _discovery.emit(DiscoveryState.Idle) }
    }

    override fun connectTo(device: SamsungTvDevice) {
        scope.launch {
            _connection.emit(ConnectionState.Connecting)
            delay(800)
            _connection.emit(ConnectionState.Connected(device))
        }
    }

    override fun disconnect() {
        scope.launch {
            _connection.emit(ConnectionState.Disconnected)
        }
    }
}
