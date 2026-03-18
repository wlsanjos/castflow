package com.wlsanjos.castflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlsanjos.castflow.samsung.api.SamsungDiscoveryService
import com.wlsanjos.castflow.samsung.state.ConnectedDeviceStore
import com.wlsanjos.castflow.samsung.models.ConnectionState
import com.wlsanjos.castflow.samsung.models.DiscoveryState
import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val discoveredDevices: List<SamsungTvDevice> = emptyList(),
    val isScanning: Boolean = false,
    val connectionMessage: String? = null
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val discoveryService: SamsungDiscoveryService
    ,
    private val connectedDeviceStore: ConnectedDeviceStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    sealed class UiEvent {
        object NavigateToLibrary : UiEvent()
    }

    private val _uiEvent = kotlinx.coroutines.channels.Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            discoveryService.discoveryState().collect { ds ->
                when (ds) {
                    DiscoveryState.Idle -> _uiState.update { it.copy(isScanning = false, discoveredDevices = emptyList()) }
                    DiscoveryState.Searching -> _uiState.update { it.copy(isScanning = true) }
                    is DiscoveryState.Found -> _uiState.update { it.copy(isScanning = false, discoveredDevices = ds.devices) }
                    is DiscoveryState.Error -> _uiState.update { it.copy(isScanning = false) }
                }
            }
        }

        viewModelScope.launch {
            discoveryService.connectionState().collect { cs ->
                _connectionState.value = cs
                when (cs) {
                    ConnectionState.Connecting -> _uiState.update { it.copy(connectionMessage = null) }
                    ConnectionState.WaitingForApproval -> _uiState.update { it.copy(connectionMessage = null) }
                    is ConnectionState.Connected -> {
                        _uiState.update { it.copy(connectionMessage = null) }
                        connectedDeviceStore.set(cs.device)
                        _uiEvent.send(UiEvent.NavigateToLibrary)
                    }
                    is ConnectionState.Failed -> _uiState.update { it.copy(connectionMessage = null) }
                    ConnectionState.Disconnected -> _uiState.update { it.copy(connectionMessage = null) }
                }
            }
        }
    }

    fun startScan() = discoveryService.startDiscovery()
    fun stopScan() = discoveryService.stopDiscovery()

    fun refreshScan() {
        stopScan()
        _uiState.update { it.copy(discoveredDevices = emptyList()) }
        startScan()
    }

    fun connectTo(device: SamsungTvDevice) = discoveryService.connectTo(device)

    fun resetConnectionState() {
        // Since we don't have a direct reset on the service,
        // we can trigger a disconnect which should emit Disconnected state
        discoveryService.disconnect()
    }
}
