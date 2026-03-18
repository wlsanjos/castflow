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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wlsanjos.castflow.ui.components.*
import com.wlsanjos.castflow.viewmodel.PlaybackViewModel

@Composable
fun PlaybackControlScreen(onBack: () -> Unit = {}, viewModel: PlaybackViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF091013),
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text(
                    "PLAYING",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassCard(cornerRadius = 32.dp) {
                Box(contentAlignment = Alignment.Center) {
                    ImagePlaceholder(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp)))
                    
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00F0FF),
                        modifier = Modifier.size(80.dp).clickable { 
                            if (state.isPlaying) viewModel.pause() else viewModel.play() 
                        }
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.padding(20.dp).size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Media Title", // In a real app we'd get this from state
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Text(
                text = "Streaming to Living Room TV",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF00F0FF)
            )

            Spacer(modifier = Modifier.height(48.dp))

            PrimaryButton(
                text = "STOP CASTING",
                onClick = onBack,
                containerColor = Color(0xFFFF4B4B)
            )
        }
    }
}
