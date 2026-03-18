package com.wlsanjos.castflow.samsung.impl

import android.content.Context
import android.util.Log
import com.wlsanjos.castflow.model.MediaItem
import com.wlsanjos.castflow.model.MediaType
import com.wlsanjos.castflow.samsung.api.CastState
import com.wlsanjos.castflow.samsung.api.SamsungCastService
import com.wlsanjos.castflow.samsung.api.SamsungConnectionService
import com.wlsanjos.castflow.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.wlsanjos.castflow.samsung.models.ConnectionState
import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import org.json.JSONObject
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SamsungCastServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectionService: SamsungConnectionService
) : SamsungCastService {

    private val _castState = MutableStateFlow<CastState>(CastState.Idle)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var server: NettyApplicationEngine? = null
    private var currentMedia: MediaItem? = null

    override fun castState() = _castState.asStateFlow()

    override fun castMedia(media: MediaItem, device: SamsungTvDevice) {
        scope.launch {
            _castState.value = CastState.Casting
            currentMedia = media

            val localIp = NetworkUtils.getLocalIpAddress()
            if (localIp == null) {
                _castState.value = CastState.Error("Could not determine local IP")
                return@launch
            }

            startServerIfNeeded(8080)

            // Ensure we are connected to the casting channel specifically
            connectionService.connect(device, "com.samsung.multiscreen.cast")

            // Wait for connection to be ready (up to 15 seconds for user approval)
            val isConnected = withTimeoutOrNull(15000) {
                connectionService.connectionState().first { it is ConnectionState.Connected }
            }

            if (isConnected == null) {
                Log.e("SamsungCast", "Timed out waiting for connection to casting channel")
                _castState.value = CastState.Error("Connection timeout. Please approve on TV.")
                return@launch
            }

            val mediaUrl = "http://$localIp:8080/media?id=${media.id}"
            Log.d("SamsungCast", "Casting URL: $mediaUrl to")

            val success = sendMediaOpenCommand(media, mediaUrl)

            if (success) {
                _castState.value = CastState.Success(media)
            } else {
                _castState.value = CastState.Error("Failed to send command to TV")
            }
        }
    }

    private fun startServerIfNeeded(port: Int) {
        if (server != null) return

        server = embeddedServer(Netty, port = port) {
            install(PartialContent)
            routing {
                get("/ping") {
                    Log.i("SamsungCast", "Received /ping request")
                    call.respondText("pong")
                }
                get("/media") {
                    val id = call.parameters["id"]
                    Log.i("SamsungCast", "HTTP request for /media, id=$id, query=${call.request.queryParameters}")
                    if (id == null || currentMedia?.id != id) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }

                    val media = currentMedia ?: return@get
                    val contentResolver = this@SamsungCastServiceImpl.context.contentResolver

                    try {
                        val inputStream = contentResolver.openInputStream(media.uri)
                        if (inputStream != null) {
                            val mimeType = media.mimeType ?: if (media.type == MediaType.PHOTO) "image/jpeg" else "video/mp4"
                            val size = media.size

                            call.respond(object : OutgoingContent.ReadChannelContent() {
                                override val contentType: ContentType = ContentType.parse(mimeType)
                                override val contentLength: Long? = if (size > 0) size else null
                                override fun readFrom(): ByteReadChannel = inputStream.toByteReadChannel()
                            })
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                        }
                    } catch (e: Exception) {
                        Log.e("SamsungCast", "Error serving media", e)
                        // If we haven't responded yet, we could try, but usually it's a closed channel.
                    }
                }
            }
        }.start(wait = false)
        Log.i("SamsungCast", "Local media server started on port $port")
    }

    private fun sendMediaOpenCommand(media: MediaItem, url: String): Boolean {
        return try {
            val json = JSONObject().apply {
                put("method", "ms.channel.emit")
                put("params", JSONObject().apply {
                    put("event", "ms.media.open")
                    put("to", "host")
                    put("data", JSONObject().apply {
                        put("contentId", media.id)
                        put("title", media.title)
                        put("mediaUrl", url)
                        put("mimeType", media.mimeType ?: if (media.type == MediaType.PHOTO) "image/jpeg" else "video/mp4")
                    })
                })
            }
            Log.i("SamsungCast", "Sending media open command: ${json.toString()}")
            connectionService.sendMessage(json.toString())
        } catch (e: Exception) {
            Log.e("SamsungCast", "Error formatting media command", e)
            false
        }
    }

    override fun stopCasting() {
        // Optional: Send stop command to TV if supported
        server?.stop(1000, 2000)
        server = null
        _castState.value = CastState.Idle
    }
}
