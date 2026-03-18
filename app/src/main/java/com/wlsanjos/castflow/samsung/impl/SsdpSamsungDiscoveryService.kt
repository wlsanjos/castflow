package com.wlsanjos.castflow.samsung.impl

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.wlsanjos.castflow.samsung.api.SamsungConnectionService
import com.wlsanjos.castflow.samsung.api.SamsungDiscoveryService
import com.wlsanjos.castflow.samsung.models.ConnectionState
import com.wlsanjos.castflow.samsung.models.DiscoveryState
import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.*
import java.nio.charset.Charset
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Ultra-robust discovery service combining SSDP (with XML inspection) and mDNS (NSD)
 * with multiple service types for maximum reliability on all network conditions.
 */
class SsdpSamsungDiscoveryService(
    private val context: Context,
    private val connectionService: SamsungConnectionService,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : SamsungDiscoveryService {

    private val _discovery = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    private var multicastLock: WifiManager.MulticastLock? = null
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val foundDevices = mutableMapOf<String, SamsungTvDevice>()
    private val nsdListeners = mutableListOf<NsdManager.DiscoveryListener>()
    private var scanJob: Job? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    override fun discoveryState(): Flow<DiscoveryState> = _discovery
    override fun connectionState(): Flow<ConnectionState> = connectionService.connectionState()

    override fun startDiscovery() {
        Log.i("SsdpSamsung", "startDiscovery() called")

        // Cancel any previous job and ensure we are starting fresh
        scanJob?.cancel()

        scanJob = scope.launch {
            Log.i("SsdpSamsung", "Discovery Job started")

            // Ensure any previous discovery is completely stopped
            stopNsd()

            _discovery.emit(DiscoveryState.Searching)
            synchronized(foundDevices) { foundDevices.clear() }

            // 1. mDNS Discovery (Multiple Types)
            startNsd("_samsung-screens._tcp.")
            startNsd("_airplay._tcp.")
            startNsd("_dial._tcp.")
            startNsd("_spotify-connect._tcp.")
            startNsd("_mediarenderer._tcp.")

            // 2. Multicast Lock
            acquireMulticastLock()

            // 3. SSDP Search
            searchSsdp()
        }
    }

    private fun startNsd(serviceType: String) {
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.i("SsdpSamsung", "NSD Discovery Started: $regType")
            }
            override fun onServiceFound(service: NsdServiceInfo) {
                Log.i("SsdpSamsung", "NSD Found: ${service.serviceName} (${service.serviceType})")
                nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e("SsdpSamsung", "NSD Resolve Failed: $errorCode for ${serviceInfo.serviceName}")
                    }
                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        val host = serviceInfo.host.hostAddress ?: return
                        Log.i("SsdpSamsung", "NSD Resolved: ${serviceInfo.serviceName} at $host")
                        val name = serviceInfo.serviceName ?: "Smart TV"

                        // Heuristic: If it's definitely a Samsung or a generic TV name, add it
                        if (name.contains("Samsung", true) ||
                            name.contains("TV", true) ||
                            serviceInfo.serviceType.contains("samsung", true)) {

                            val device = SamsungTvDevice(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                host = host,
                                port = serviceInfo.port
                            )
                            addDevice(device)
                        }
                    }
                })
            }
            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d("SsdpSamsung", "NSD Service Lost: ${service.serviceName}")
            }
            override fun onDiscoveryStopped(serviceType: String) {
                Log.d("SsdpSamsung", "NSD Discovery Stopped: $serviceType")
            }
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("SsdpSamsung", "NSD Start Failed for $serviceType: $errorCode")
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("SsdpSamsung", "NSD Stop Failed for $serviceType: $errorCode")
            }
        }

        synchronized(nsdListeners) { nsdListeners.add(listener) }
        try {
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            Log.e("SsdpSamsung", "Failed to start NSD for $serviceType", e)
        }
    }

    private fun stopNsd() {
        synchronized(nsdListeners) {
            Log.d("SsdpSamsung", "Stopping ${nsdListeners.size} NSD listeners")
            nsdListeners.forEach {
                try {
                    nsdManager.stopServiceDiscovery(it)
                } catch (e: Exception) {}
            }
            nsdListeners.clear()
        }
    }

    private fun getLocalIpAddress(): InetAddress? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue

                // Prioritize WiFi and Ethernet, but allow others if they have an IPv4
                val isWifi = iface.name.contains("wlan", true) || iface.name.contains("p2p", true)
                val isEth = iface.name.contains("eth", true)
                
                val addrs = iface.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        // If it's wifi or eth, return immediately. Otherwise keep looking.
                        if (isWifi || isEth) return addr
                    }
                }
            }

            // Fallback for some devices
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            if (ipAddress != 0) {
                return InetAddress.getByAddress(
                    byteArrayOf(
                        (ipAddress and 0xff).toByte(),
                        (ipAddress shr 8 and 0xff).toByte(),
                        (ipAddress shr 16 and 0xff).toByte(),
                        (ipAddress shr 24 and 0xff).toByte()
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("SsdpSamsung", "Failed to get Local IP", e)
        }
        return null
    }

    private fun addDevice(device: SamsungTvDevice) {
        synchronized(foundDevices) {
            if (!foundDevices.containsKey(device.host)) {
                foundDevices[device.host] = device
                _discovery.update { DiscoveryState.Found(foundDevices.values.toList()) }
            }
        }
    }

    private fun acquireMulticastLock() {
        try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (multicastLock == null) {
                multicastLock = wifi.createMulticastLock("CastFlowSSDP")
                multicastLock?.setReferenceCounted(false)
            }
            multicastLock?.acquire()
        } catch (e: Exception) {
            Log.e("SsdpSamsung", "Lock failure", e)
        }
    }

    private suspend fun searchSsdp() = withContext(Dispatchers.IO) {
        var socket: DatagramSocket? = null
        try {
            val localIp = getLocalIpAddress()
            Log.i("SsdpSamsung", "Detected Local IP: $localIp")
            
            socket = DatagramSocket(0)
            socket.soTimeout = 3000
            socket.reuseAddress = true
            
            val ssdpAddr = InetAddress.getByName("239.255.255.250")
            val ssdpPort = 1900

            Log.i("SsdpSamsung", "SSDP Search starting on wildcard port ${socket.localPort}")

            val searchTargets = listOf(
                "ssdp:all",
                "upnp:rootdevice",
                "urn:samsung.com:device:RemoteControlReceiver:1",
                "urn:dial-multiscreen-org:service:dial:1",
                "urn:schemas-upnp-org:device:MediaRenderer:1"
            )

            // Multiple bursts for reliability
            repeat(3) { burst ->
                Log.d("SsdpSamsung", "Sending SSDP burst #$burst")
                searchTargets.forEach { st ->
                    val query = (
                        "M-SEARCH * HTTP/1.1\r\n" +
                        "HOST: 239.255.255.250:1900\r\n" +
                        "MAN: \"ssdp:discover\"\r\n" +
                        "MX: 3\r\n" +
                        "ST: $st\r\n" +
                        "USER-AGENT: Android/10.0 UPnP/1.1 CastFlow/1.0\r\n" +
                        "\r\n"
                    ).toByteArray(Charset.forName("UTF-8"))
                    socket.send(DatagramPacket(query, query.size, InetSocketAddress(ssdpAddr, ssdpPort)))
                }
                delay(300)
            }

            val buffer = ByteArray(8192)
            val startTime = System.currentTimeMillis()
            var packetsReceived = 0
            while (System.currentTimeMillis() - startTime < 10000) { // 10 seconds total
                try {
                    val resp = DatagramPacket(buffer, buffer.size)
                    socket.receive(resp)
                    packetsReceived++
                    val text = String(resp.data, 0, resp.length, Charset.forName("UTF-8"))
                    val host = resp.address.hostAddress ?: continue
                    
                    Log.i("SsdpSamsung", "Packet #$packetsReceived from $host (Length: ${resp.length})")
                    Log.v("SsdpSamsung", "Content: ${text.take(200)}")

                    val isDefiniteSamsung = text.contains("Samsung", true) ||
                        text.contains("Tizen", true) ||
                        text.contains("DTV", true) ||
                        text.contains("CU7700", true) ||
                        text.contains("RemoteControlReceiver", true)

                    val isCandidate = text.contains("dial", true) ||
                        text.contains("MediaRenderer", true) ||
                        text.contains("multiscreen", true)

                    if (isDefiniteSamsung || isCandidate) {
                        launch {
                            inspectSsdpCandidate(host, text, isDefiniteSamsung)
                        }
                    } else {
                        Log.d("SsdpSamsung", "Ignoring non-target response from $host")
                    }
                } catch (e: SocketTimeoutException) {
                    Log.v("SsdpSamsung", "Still waiting for packets... (Total received: $packetsReceived)")
                } catch (e: Exception) {
                    Log.e("SsdpSamsung", "Receive error: ${e.message}")
                }
            }
            Log.i("SsdpSamsung", "Search loop ended. Total packets seen: $packetsReceived")
        } catch (e: Exception) {
            Log.e("SsdpSamsung", "SSDP Error", e)
        } finally {
            socket?.close()
            Log.i("SsdpSamsung", "SSDP Search finished. Final count: ${foundDevices.size}")
            _discovery.update { current ->
                if (current is DiscoveryState.Searching) {
                    DiscoveryState.Found(foundDevices.values.toList())
                } else current
            }
        }
    }

    private suspend fun inspectSsdpCandidate(host: String, rawResponse: String, isDefinite: Boolean) {
        val locationLine = rawResponse.split("\r\n").firstOrNull { it.startsWith("LOCATION:", true) }
        val locationUrl = locationLine?.substringAfter(':')?.trim()

        if (isDefinite) {
            val device = SamsungTvDevice(
                id = UUID.randomUUID().toString(),
                name = "Samsung TV ($host)",
                host = host,
                port = 8001,
                descriptionUri = locationUrl?.let { android.net.Uri.parse(it) }
            )
            addDevice(device)
            return
        }

        if (locationUrl != null) {
            try {
                val request = Request.Builder().url(locationUrl).build()
                val response = withContext(Dispatchers.IO) { httpClient.newCall(request).execute() }
                val body = response.body?.string() ?: ""

                if (body.contains("Samsung", true) || body.contains("Tizen", true)) {
                    val device = SamsungTvDevice(
                        id = UUID.randomUUID().toString(),
                        name = "Samsung TV ($host)",
                        host = host,
                        port = 8001,
                        descriptionUri = android.net.Uri.parse(locationUrl)
                    )
                    addDevice(device)
                }
            } catch (e: Exception) {
                Log.w("SsdpSamsung", "Failed to fetch XML from $locationUrl: ${e.message}")
            }
        }
    }

    override fun stopDiscovery() {
        Log.i("SsdpSamsung", "stopDiscovery() called")
        scanJob?.cancel()
        scope.launch {
            _discovery.emit(DiscoveryState.Idle)
            stopNsd()
            try {
                if (multicastLock?.isHeld == true) {
                    multicastLock?.release()
                }
            } catch (e: Exception) {}
        }
    }

    override fun connectTo(device: SamsungTvDevice) {
        connectionService.connect(device)
    }

    override fun disconnect() {
        connectionService.disconnect()
    }
}
