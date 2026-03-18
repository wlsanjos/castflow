package com.wlsanjos.castflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wlsanjos.castflow.R
import com.wlsanjos.castflow.ui.components.CastFlowBottomBar
import com.wlsanjos.castflow.ui.components.GlassCard

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToDiscover: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF091013),
        bottomBar = {
            CastFlowBottomBar(
                currentRoute = "settings",
                onNavigate = { route ->
                    when (route) {
                        "library" -> onNavigateToLibrary()
                        "discover" -> onNavigateToDiscover()
                        "settings" -> {}
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text(
                    stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Devices Section
            SectionTitle(stringResource(R.string.section_devices))
            Spacer(modifier = Modifier.height(12.dp))
            
            ConnectedDeviceItem(
                name = "Samsung OLED 8K",
                model = "Living Room • 192.168.1.15",
                status = stringResource(R.string.status_connected),
                imageUrl = "https://picsum.photos/id/2/200/120"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PairDeviceItem(
                name = "Kitchen Echo Show",
                model = stringResource(R.string.status_ready),
                icon = Icons.Default.Speaker
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Preferences Section
            SectionTitle(stringResource(R.string.section_preferences))
            Spacer(modifier = Modifier.height(12.dp))
            
            PreferenceSwitchItem(
                stringResource(R.string.pref_auto_connect),
                stringResource(R.string.pref_auto_connect_desc),
                true
            )
            PreferenceSwitchItem(
                stringResource(R.string.pref_high_quality),
                stringResource(R.string.pref_high_quality_desc),
                true
            )
            PreferenceClickItem(
                stringResource(R.string.pref_clear_cache),
                stringResource(R.string.pref_clear_cache_desc)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // About Section
            SectionTitle(stringResource(R.string.section_about))
            Spacer(modifier = Modifier.height(12.dp))
            
            AboutItem()

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Out
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { /* TODO */ },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF4B4B), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(stringResource(R.string.sign_out), color = Color(0xFFFF4B4B), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF00F0FF),
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
fun ConnectedDeviceItem(name: String, model: String, status: String, imageUrl: String) {
    GlassCard(cornerRadius = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00F0FF)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(model, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(status, style = MaterialTheme.typography.labelSmall, color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold)
            }
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp, 50.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun PairDeviceItem(name: String, model: String, icon: ImageVector) {
    GlassCard(cornerRadius = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(model, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            TextButton(onClick = { /* TODO */ }) {
                Text(stringResource(R.string.action_pair), color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PreferenceSwitchItem(title: String, subtitle: String, initialValue: Boolean) {
    var checked by remember { mutableStateOf(initialValue) }
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Switch(
                checked = checked,
                onCheckedChange = { checked = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00F0FF),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun PreferenceClickItem(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp).clickable { /* TODO */ }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
        Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun AboutItem() {
    GlassCard(cornerRadius = 24.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF00F0FF)
            ) {
                Icon(Icons.Default.CastConnected, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(14.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.version_info), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.terms_of_service), color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.privacy_policy), color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}
