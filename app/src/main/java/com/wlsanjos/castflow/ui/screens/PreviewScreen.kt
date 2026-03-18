package com.wlsanjos.castflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wlsanjos.castflow.R
import com.wlsanjos.castflow.model.MediaType
import com.wlsanjos.castflow.samsung.api.CastState
import com.wlsanjos.castflow.ui.components.CastFlowBottomBar
import com.wlsanjos.castflow.ui.components.PrimaryButton
import com.wlsanjos.castflow.viewmodel.PreviewViewModel

@Composable
fun PreviewScreen(
    onCastSuccess: () -> Unit,
    onBack: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is PreviewViewModel.UiEvent.NavigateToPlayback -> onCastSuccess()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF091013),
        bottomBar = {
            CastFlowBottomBar(
                currentRoute = "preview",
                onNavigate = { route ->
                    when (route) {
                        "library" -> onNavigateToLibrary()
                        "discover" -> onNavigateToDiscover()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            PreviewHeader(onBack = onBack)

            Spacer(modifier = Modifier.height(32.dp))

            // Media Preview
            MediaArtwork(
                uri = uiState.media?.uri,
                type = uiState.media?.type ?: MediaType.PHOTO
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Metadata
            MediaMetadata(
                title = uiState.media?.title ?: "Unknown",
                detail = if (uiState.media?.type == MediaType.VIDEO) "Video" else "Photo"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Connection Status
            ConnectionStatusInfo(deviceName = uiState.connectedDeviceName)

            Spacer(modifier = Modifier.height(32.dp))

            // Cast Button
            CastButton(
                castState = uiState.castState,
                deviceName = uiState.connectedDeviceName,
                onClick = { viewModel.castMedia() }
            )

            if (uiState.castState is CastState.Error) {
                Text(
                    text = (uiState.castState as CastState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PreviewHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.castflow),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF00F0FF),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
            Text(
                stringResource(R.string.preview).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 2.sp
            )
        }
        IconButton(onClick = { /* Share */ }) {
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun MediaArtwork(uri: android.net.Uri?, type: MediaType) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00F0FF).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    radius = size.width * 0.7f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
        ) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.05f)))
            }
            
            if (type == MediaType.VIDEO) {
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(64.dp).align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun MediaMetadata(title: String, detail: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            maxLines = 1
        )
        Text(
            detail,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun ConnectionStatusInfo(deviceName: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (deviceName != null) Icons.Default.Tv else Icons.Default.TvOff,
            contentDescription = null,
            tint = if (deviceName != null) Color(0xFF00F0FF) else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            if (deviceName != null) stringResource(R.string.connected_to, deviceName)
            else stringResource(R.string.not_connected_to_tv),
            style = MaterialTheme.typography.bodyMedium,
            color = if (deviceName != null) Color.White else Color.Gray
        )
    }
}

@Composable
fun CastButton(castState: CastState, deviceName: String?, onClick: () -> Unit) {
    val isLoading = castState is CastState.Casting
    
    PrimaryButton(
        text = if (isLoading) stringResource(R.string.loading) 
               else stringResource(R.string.cast_to_tv),
        onClick = onClick,
        enabled = deviceName != null && !isLoading,
        modifier = Modifier.fillMaxWidth()
    )
}
