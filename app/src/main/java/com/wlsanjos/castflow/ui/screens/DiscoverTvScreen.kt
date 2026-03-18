package com.wlsanjos.castflow.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wlsanjos.castflow.R
import com.wlsanjos.castflow.samsung.models.ConnectionState
import com.wlsanjos.castflow.samsung.models.SamsungTvDevice
import com.wlsanjos.castflow.ui.components.GlassCard
import com.wlsanjos.castflow.ui.components.PrimaryButton
import com.wlsanjos.castflow.viewmodel.DiscoverViewModel

@Composable
fun DiscoverTvScreen(
    onBack: () -> Unit,
    onConnect: () -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    var selectedDevice by remember { mutableStateOf<SamsungTvDevice?>(null) }

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToLibrary.collect {
            onConnect()
        }
    }

    Scaffold(
        containerColor = Color(0xFF091013),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                if (uiState.connectionMessage != null) {
                    Text(
                        text = when (connectionState) {
                            ConnectionState.Connecting -> stringResource(R.string.connecting_tv)
                            ConnectionState.WaitingForApproval -> stringResource(R.string.waiting_approval)
                            is ConnectionState.Connected -> stringResource(R.string.connected)
                            is ConnectionState.Failed -> stringResource(R.string.connection_error)
                            else -> uiState.connectionMessage ?: ""
                        },
                        color = if (connectionState is ConnectionState.Failed) Color.Red else Color(0xFF00F0FF),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                PrimaryButton(
                    text = when (connectionState) {
                        is ConnectionState.Connecting -> stringResource(R.string.discover_connecting)
                        is ConnectionState.WaitingForApproval -> "Awaiting Approval..."
                        is ConnectionState.Connected -> stringResource(R.string.discover_connected)
                        is ConnectionState.Failed -> "Failed. Tap to Reset"
                        else -> stringResource(R.string.connect_now)
                    },
                    onClick = {
                        if (connectionState is ConnectionState.Failed) {
                            viewModel.resetConnectionState()
                        } else if (selectedDevice != null) {
                            viewModel.connectTo(selectedDevice!!)
                        }
                    },
                    modifier = Modifier.alpha(if (selectedDevice != null || connectionState is ConnectionState.Failed) 1f else 0.5f),
                    containerColor = if (connectionState is ConnectionState.Failed) Color(0xFFFF4B4B) else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.wifi_network_hint),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Text(
                        text = stringResource(R.string.castflow),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    IconButton(
                        onClick = { viewModel.refreshScan() },
                        modifier = Modifier.align(Alignment.CenterEnd),
                        enabled = !uiState.isScanning
                    ) {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF00F0FF))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.connect_device),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = if (uiState.isScanning) stringResource(R.string.scanning_devices) else "Search completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(48.dp))
                ScanningAnimation()
                Spacer(modifier = Modifier.height(48.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.discovered_devices),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.devices_found, uiState.discoveredDevices.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00F0FF)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.discoveredDevices.isEmpty() && !uiState.isScanning) {
                    Column(
                        modifier = Modifier.padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.no_devices_found),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refreshScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.try_again), color = Color.White)
                        }
                    }
                }
            }

            items(uiState.discoveredDevices) { device ->
                DeviceItem(
                    device = device,
                    isSelected = selectedDevice?.id == device.id,
                    onSelect = { selectedDevice = device }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ScanningAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        // Outer rings
        repeat(3) { index ->
            val ringScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.6f + (index * 0.3f),
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearOutSlowInEasing, delayMillis = index * 800),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ring$index"
            )
            val ringAlpha by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearOutSlowInEasing, delayMillis = index * 800),
                    repeatMode = RepeatMode.Restart
                ),
                label = "alpha$index"
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF00F0FF).copy(alpha = ringAlpha),
                            radius = (size.minDimension / 2) * ringScale,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
            )
        }

        // Inner circle
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00F0FF).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF00F0FF),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.scanning_label),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Cast,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun DeviceItem(device: SamsungTvDevice, isSelected: Boolean, onSelect: () -> Unit) {
    GlassCard(cornerRadius = 16.dp, modifier = Modifier.clip(RoundedCornerShape(16.dp))) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF00F0FF) else Color.White.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else Color.White,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(device.host, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }

            Switch(
                checked = isSelected,
                onCheckedChange = { onSelect() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00F0FF),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
    }
}

