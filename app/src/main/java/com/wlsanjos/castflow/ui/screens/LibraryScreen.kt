package com.wlsanjos.castflow.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wlsanjos.castflow.R
import com.wlsanjos.castflow.model.Album
import com.wlsanjos.castflow.model.MediaItem
import com.wlsanjos.castflow.model.MediaType
import com.wlsanjos.castflow.ui.components.CastFlowBottomBar
import com.wlsanjos.castflow.ui.components.GlassCard
import com.wlsanjos.castflow.ui.components.PrimaryButton
import com.wlsanjos.castflow.viewmodel.LibraryTab
import com.wlsanjos.castflow.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    onNavigateToPreview: (MediaItem) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        viewModel.updatePermissionStatus(granted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }

    Scaffold(
        containerColor = Color(0xFF091013),
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
            HeaderSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Row
            LibraryTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.onTabSelected(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Box(modifier = Modifier.weight(1f)) {
                if (!uiState.hasPermission) {
                    PermissionDeniedView { launcher.launch(permissions) }
                } else if (uiState.isLoading && uiState.photos.isEmpty() && uiState.videos.isEmpty()) {
                    LoadingView()
                } else {
                    MediaContent(
                        uiState = uiState,
                        onMediaClick = { media ->
                            viewModel.onMediaClick(media)
                            onNavigateToPreview(media)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            stringResource(R.string.castflow),
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color(0xFF00F0FF),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* Search Placeholder */ }) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(32.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
            }
        }
    }
}

@Composable
fun LibraryTabs(selectedTab: LibraryTab, onTabSelected: (LibraryTab) -> Unit) {
    val tabs = listOf(
        LibraryTab.PHOTOS to stringResource(R.string.tabs_photos),
        LibraryTab.VIDEOS to stringResource(R.string.tabs_videos),
        LibraryTab.ALBUMS to stringResource(R.string.tabs_albums)
    )

    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                color = Color(0xFF00F0FF),
                height = 3.dp
            )
        },
        divider = {}
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selectedTab == tab) Color.White else Color.Gray
                    )
                }
            )
        }
    }
}

@Composable
fun MediaContent(
    uiState: com.wlsanjos.castflow.viewmodel.LibraryUiState,
    onMediaClick: (MediaItem) -> Unit
) {
    when (uiState.selectedTab) {
        LibraryTab.PHOTOS -> {
            if (uiState.photos.isEmpty()) EmptyView(stringResource(R.string.no_media_title))
            else MediaGrid(uiState.photos, onMediaClick)
        }
        LibraryTab.VIDEOS -> {
            if (uiState.videos.isEmpty()) EmptyView(stringResource(R.string.no_media_title))
            else MediaGrid(uiState.videos, onMediaClick)
        }
        LibraryTab.ALBUMS -> {
            if (uiState.albums.isEmpty()) EmptyView(stringResource(R.string.no_albums_found))
            else AlbumGrid(uiState.albums)
        }
    }
}

@Composable
fun MediaGrid(items: List<MediaItem>, onMediaClick: (MediaItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(items) { item ->
            MediaThumbnail(item) { onMediaClick(item) }
        }
    }
}

@Composable
fun AlbumGrid(albums: List<Album>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(albums) { album ->
            AlbumItem(album)
        }
    }
}

@Composable
fun MediaThumbnail(item: MediaItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        if (item.type == MediaType.VIDEO) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun AlbumItem(album: Album) {
    Column {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            if (album.thumbnailUri != null) {
                AsyncImage(
                    model = album.thumbnailUri,
                    contentDescription = album.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd)
            ) {
                Text(
                    album.mediaCount.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(album.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PermissionDeniedView(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.permission_rationale_title),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.permission_rationale_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(text = stringResource(R.string.request_permission), onClick = onRequestPermission)
    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF00F0FF))
    }
}

@Composable
fun EmptyView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
    }
}
