package com.wlsanjos.castflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
fun LibraryScreen(
    onNavigateToPreview: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CastFlowBottomBar(
                currentRoute = "library",
                onNavigate = { route ->
                    when (route) {
                        "library" -> {}
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                }
                Text(
                    stringResource(R.string.castflow),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Streaming Status Card
            GlassCard(cornerRadius = 24.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = 0.65f,
                            modifier = Modifier.size(40.dp),
                            color = Color(0xFF00F0FF),
                            strokeWidth = 3.dp,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Icon(Icons.Default.Cast, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Samsung OLED TV", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(stringResource(R.string.streaming_status), style = MaterialTheme.typography.bodySmall, color = Color(0xFF00F0FF))
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.CloseFullscreen, contentDescription = null, tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = 0.65f,
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF00F0FF),
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tab Row
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            val tabsLabels = listOf(
                stringResource(R.string.tabs_photos),
                stringResource(R.string.tabs_videos),
                stringResource(R.string.tabs_albums)
            )
            
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF00F0FF),
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                tabsLabels.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedTabIndex == index) Color.White else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Media Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    SectionHeader(stringResource(R.string.section_today), Icons.Default.CalendarToday)
                }
                
                items(sampleMediaToday) { media ->
                    MediaItem(media, onNavigateToPreview)
                }

                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    SectionHeader(stringResource(R.string.section_yesterday), Icons.Default.History)
                }
                
                items(sampleMediaYesterday) { media ->
                    MediaItem(media, onNavigateToPreview)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MediaItem(imageUrl: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Duration Badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd)
        ) {
            Text(
                "02:45",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

val sampleMediaToday = listOf(
    "https://picsum.photos/id/20/400/400",
    "https://picsum.photos/id/21/400/400",
    "https://picsum.photos/id/22/400/400",
    "https://picsum.photos/id/23/400/400"
)

val sampleMediaYesterday = listOf(
    "https://picsum.photos/id/24/400/400",
    "https://picsum.photos/id/25/400/400",
    "https://picsum.photos/id/26/400/400",
    "https://picsum.photos/id/27/400/400"
)
