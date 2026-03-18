package com.wlsanjos.castflow.samsung.state

import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectedDeviceStore @Inject constructor() {
    private val _device = MutableStateFlow<SamsungTvDevice?>(null)
    val device: StateFlow<SamsungTvDevice?> = _device

    fun set(device: SamsungTvDevice) {
        _device.value = device
    }

    fun clear() {
        _device.value = null
    }
}
