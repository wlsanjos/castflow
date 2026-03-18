package com.wlsanjos.castflow.samsung.impl

import android.util.Base64
import android.util.Log
import com.wlsanjos.castflow.samsung.api.SamsungConnectionService
import com.wlsanjos.castflow.samsung.models.ConnectionState
import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.*
import org.json.JSONObject
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class SamsungConnectionServiceImpl(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : SamsungConnectionService {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    private var webSocket: WebSocket? = null
    
    // Client for Port 8001 (Unsecure)
    private val client8001 = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    // Client for Port 8002 (Secure - trusts all certs)
    private val client8002: OkHttpClient by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    override fun connectionState(): Flow<ConnectionState> = _connectionState

    override fun connect(device: SamsungTvDevice) {
        scope.launch {
            if (_connectionState.value is ConnectionState.Connecting || 
                _connectionState.value is ConnectionState.Connected) {
                return@launch
            }

            _connectionState.emit(ConnectionState.Connecting)

            // Try 8002 (WSS) first for newer models
            val success = tryConnect(device, 8002, "wss")
            if (!success) {
                Log.d("SamsungConnection", "Port 8002 failed, falling back to 8001")
                tryConnect(device, 8001, "ws")
            }
        }
    }

    private suspend fun tryConnect(device: SamsungTvDevice, port: Int, protocol: String): Boolean {
        val deviceNameBase64 = Base64.encodeToString("CastFlow".toByteArray(), Base64.NO_WRAP)
        val url = "$protocol://${device.host}:$port/api/v2/channels/samsung.remote.control?name=$deviceNameBase64"
        
        val deferred = CompletableDeferred<Boolean>()
        val currentClient = if (port == 8002) client8002 else client8001
        
        val request = Request.Builder().url(url).build()
        
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                this@SamsungConnectionServiceImpl.webSocket = webSocket
                deferred.complete(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val event = json.optString("event")
                    Log.d("SamsungConnection", "Message: $text")
                    
                    when (event) {
                        "ms.channel.connect" -> {
                            scope.launch { _connectionState.emit(ConnectionState.WaitingForApproval) }
                        }
                        "ms.channel.ready" -> {
                            val data = json.optJSONObject("data")
                            val token = data?.optString("token")
                            scope.launch {
                                _connectionState.emit(ConnectionState.Connected(device.copy(token = token)))
                            }
                        }
                        "ms.channel.unauthorized" -> {
                            scope.launch { _connectionState.emit(ConnectionState.Failed("Denied by user")) }
                            webSocket.close(1000, "Unauthorized")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SamsungConnection", "Parsing error", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("SamsungConnection", "WS Failure on port $port: ${t.message}")
                if (!deferred.isCompleted) {
                    deferred.complete(false)
                } else {
                    scope.launch {
                        _connectionState.emit(ConnectionState.Failed(t.localizedMessage ?: "Connection failed"))
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                scope.launch { _connectionState.emit(ConnectionState.Disconnected) }
            }
        }

        currentClient.newWebSocket(request, listener)
        
        return withTimeoutOrNull(6000) { deferred.await() } ?: false
    }

    override fun disconnect() {
        webSocket?.close(1000, "User requested")
        webSocket = null
        scope.launch { _connectionState.emit(ConnectionState.Disconnected) }
    }
}
