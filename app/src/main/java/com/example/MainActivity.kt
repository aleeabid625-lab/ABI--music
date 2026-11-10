package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.VideoItem
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreateOrRenamePlaylistDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.FullPlayerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SongsScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.VideosScreen
import com.example.ui.theme.AbiMusicTheme
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.VideoViewModel

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    SONGS("Songs", Icons.Filled.MusicNote),
    VIDEOS("Videos", Icons.Filled.VideoLibrary),
    PLAYLISTS("Playlists", Icons.Filled.QueueMusic),
    SETTINGS("Settings", Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {

    private val musicViewModel: MusicViewModel by viewModels()
    private val videoViewModel: VideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AbiMusicTheme {
                MainAppScreen(
                    musicViewModel = musicViewModel,
                    videoViewModel = videoViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    musicViewModel: MusicViewModel,
    videoViewModel: VideoViewModel
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var showFullPlayer by remember { mutableStateOf(false) }
    var showEqualizer by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var selectedVideo by remember { mutableStateOf<VideoItem?>(null) }
    var songForPlaylistDialog by remember { mutableStateOf<Song?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val currentSong by musicViewModel.currentSong.collectAsState()
    val isPlaying by musicViewModel.isPlaying.collectAsState()
    val currentPositionMs by musicViewModel.currentPositionMs.collectAsState()
    val durationMs by musicViewModel.durationMs.collectAsState()
    val playlists by musicViewModel.allPlaylists.collectAsState()

    // Storage and Notification permission launcher
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            musicViewModel.rescanMedia()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissionsToRequest)
    }

    // Add To Playlist Dialog
    songForPlaylistDialog?.let { targetSong ->
        AddToPlaylistDialog(
            song = targetSong,
            playlists = playlists,
            onDismiss = { songForPlaylistDialog = null },
            onPlaylistSelected = { pId ->
                musicViewModel.addSongToPlaylist(pId, targetSong.id)
                songForPlaylistDialog = null
            },
            onCreateNewPlaylist = {
                showCreatePlaylistDialog = true
            }
        )
    }

    // Create New Playlist Dialog
    if (showCreatePlaylistDialog) {
        CreateOrRenamePlaylistDialog(
            title = "Create New Playlist",
            confirmText = "Create",
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                musicViewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBackground)
    ) {
        // Base Content & Navigation
        Scaffold(
            bottomBar = {
                // Bottom bar container with mini-player docked above navigation items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmoledBackground)
                ) {
                    if (currentSong != null && !showFullPlayer && selectedVideo == null) {
                        MiniPlayer(
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            onPlayPause = { musicViewModel.playPause() },
                            onNext = { musicViewModel.playNext() },
                            onPrevious = { musicViewModel.playPrevious() },
                            onClick = { showFullPlayer = true }
                        )
                    }

                    NavigationBar(
                        containerColor = AmoledSurface,
                        contentColor = TextWhite,
                        tonalElevation = 0.dp
                    ) {
                        MainTab.values().forEach { tab ->
                            val isSelected = currentTab == tab && selectedPlaylist == null
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    selectedPlaylist = null
                                    currentTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) CyanAccent else TextMuted
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        color = if (isSelected) PurpleLight else TextMuted,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(0x338B5CF6)
                                ),
                                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // When a playlist is selected, show its detail view
                if (selectedPlaylist != null) {
                    PlaylistDetailScreen(
                        playlist = selectedPlaylist!!,
                        viewModel = musicViewModel,
                        onBack = { selectedPlaylist = null }
                    )
                } else {
                    when (currentTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                viewModel = musicViewModel,
                                onNavigateToSongs = { currentTab = MainTab.SONGS },
                                onNavigateToVideos = { currentTab = MainTab.VIDEOS },
                                onNavigateToPlaylists = { currentTab = MainTab.PLAYLISTS },
                                onNavigateToEqualizer = { showEqualizer = true },
                                onSongSelectedForPlaylist = { songForPlaylistDialog = it },
                                onOpenFullPlayer = { showFullPlayer = true }
                            )
                        }
                        MainTab.SONGS -> {
                            SongsScreen(
                                viewModel = musicViewModel,
                                onSongSelectedForPlaylist = { songForPlaylistDialog = it }
                            )
                        }
                        MainTab.VIDEOS -> {
                            VideosScreen(
                                videoViewModel = videoViewModel,
                                onVideoClick = { video ->
                                    if (musicViewModel.isPlaying.value) {
                                        musicViewModel.playPause()
                                    }
                                    selectedVideo = video
                                }
                            )
                        }
                        MainTab.PLAYLISTS -> {
                            PlaylistsScreen(
                                viewModel = musicViewModel,
                                onPlaylistClick = { playlist -> selectedPlaylist = playlist }
                            )
                        }
                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                viewModel = musicViewModel,
                                onNavigateToEqualizer = { showEqualizer = true }
                            )
                        }
                    }
                }
            }
        }

        // Equalizer Screen Overlay
        AnimatedVisibility(
            visible = showEqualizer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            val visualizerData by musicViewModel.visualizerData.collectAsState()
            EqualizerScreen(
                equalizerController = musicViewModel.equalizerController,
                visualizerData = visualizerData,
                isPlaying = isPlaying,
                onBack = { showEqualizer = false }
            )
        }

        // Fullscreen Music Player Overlay
        AnimatedVisibility(
            visible = showFullPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            FullPlayerScreen(
                viewModel = musicViewModel,
                onMinimize = { showFullPlayer = false },
                onNavigateToEqualizer = { showEqualizer = true }
            )
        }

        // Fullscreen Video Player Overlay
        AnimatedVisibility(
            visible = selectedVideo != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedVideo?.let { video ->
                VideoPlayerScreen(
                    video = video,
                    videoViewModel = videoViewModel,
                    onBack = { selectedVideo = null }
                )
            }
        }
    }
}
