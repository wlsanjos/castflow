package com.wlsanjos.castflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.wlsanjos.castflow.ui.components.*
import com.wlsanjos.castflow.viewmodel.PlaybackViewModel

@Composable
fun PlaybackControlScreen(
    onBack: () -> Unit,
    viewModel: PlaybackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF091013),
        topBar = {
            PlaybackTopBar(onBack = {
                viewModel.stopCasting()
                onBack()
            })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.media?.uri != null) {
                    AsyncImage(
                        model = uiState.media?.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Metadata
            Text(
                uiState.media?.title ?: "No Title",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                stringResource(R.string.connected_to, uiState.deviceName ?: "TV"),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF00F0FF)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Controls
            PlaybackControls(
                isPlaying = uiState.isPlaying,
                onPlayPause = { if (uiState.isPlaying) viewModel.pause() else viewModel.play() }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Stop Casting
            PrimaryButton(
                text = stringResource(R.string.stop_casting).uppercase(),
                onClick = {
                    viewModel.stopCasting()
                    onBack()
                },
                containerColor = Color(0xFFFF4B4B)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PlaybackTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
        }
        Text(
            stringResource(R.string.now_playing).uppercase(),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color(0xFF00F0FF),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
    }
}

@Composable
fun PlaybackControls(isPlaying: Boolean, onPlayPause: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Previous Placeholder */ }, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        
        Spacer(modifier = Modifier.width(32.dp))
        
        Surface(
            shape = CircleShape,
            color = Color(0xFF00F0FF),
            modifier = Modifier
                .size(80.dp)
                .clickable(onClick = onPlayPause)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        IconButton(onClick = { /* Next Placeholder */ }, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}
