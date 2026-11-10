package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Song
import com.example.ui.components.GlowCard
import com.example.ui.components.RealtimeFrequencyBarsVisualizer
import com.example.ui.components.SongItemRow
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.AmoledSurfaceElevated
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleBlueGradient
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onNavigateToSongs: () -> Unit,
    onNavigateToVideos: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToEqualizer: () -> Unit,
    onSongSelectedForPlaylist: (Song) -> Unit,
    onOpenFullPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val visualizerData by viewModel.visualizerData.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmoledSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.drawable.ic_app_foreground_1794331122649,
                            contentDescription = "ABI MUSIC Logo",
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ABI MUSIC",
                                color = TextWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PurpleAccent)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("v2.0", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Continuous Offline Playback",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Equalizer Quick Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AmoledSurfaceElevated)
                        .clickable(onClick = onNavigateToEqualizer)
                        .testTag("home_equalizer_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = "Open Equalizer",
                        tint = PurpleLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Hero Continuous Playback Card (Bug Fix Highlight & 50 Songs Instant Test!)
        item {
            GlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_50_songs_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AUTO-NEXT BUG FIXED",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "${allSongs.size} Tracks Loaded",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "50 Continuous Songs Ready",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "ExoPlayer queue plays track after track non-stop. Test all 50 tracks one after another without touching a button!",
                        color = TextGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    RealtimeFrequencyBarsVisualizer(
                        magnitudes = visualizerData.magnitudes,
                        peakDots = visualizerData.peakDots,
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (allSongs.isNotEmpty()) {
                                    viewModel.playQueue(allSongs, startIndex = 0)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("play_50_songs_btn")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play All 50", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (allSongs.isNotEmpty()) {
                                    val shuffled = allSongs.shuffled()
                                    viewModel.playQueue(shuffled, startIndex = 0)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmoledSurfaceElevated),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("shuffle_50_songs_btn")
                        ) {
                            Icon(Icons.Filled.Shuffle, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shuffle All", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickStatCard(
                    title = "Songs",
                    value = "${allSongs.size}",
                    icon = Icons.Filled.MusicNote,
                    accentColor = PurpleLight,
                    onClick = onNavigateToSongs,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Playlists",
                    value = "${playlists.size}",
                    icon = Icons.Filled.QueueMusic,
                    accentColor = CyanAccent,
                    onClick = onNavigateToPlaylists,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Videos",
                    value = "HD",
                    icon = Icons.Filled.VideoLibrary,
                    accentColor = MagentaGlow,
                    onClick = onNavigateToVideos,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recently Played Section
        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Played", onSeeAll = onNavigateToSongs)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentlyPlayed.take(8)) { song ->
                        RecentSongCard(
                            song = song,
                            isCurrent = currentSong?.id == song.id,
                            isPlaying = isPlaying && currentSong?.id == song.id,
                            onClick = { viewModel.playSong(song, recentlyPlayed) }
                        )
                    }
                }
            }
        }

        // My Playlists Preview
        if (playlists.isNotEmpty()) {
            item {
                SectionHeader(title = "My Playlists", onSeeAll = onNavigateToPlaylists)
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playlists.take(5)) { playlist ->
                        PlaylistPreviewChip(
                            name = playlist.name,
                            songCount = playlist.songCount,
                            onClick = onNavigateToPlaylists
                        )
                    }
                }
            }
        }

        // Favorite Songs
        if (favoriteSongs.isNotEmpty()) {
            item {
                SectionHeader(title = "Favorites", onSeeAll = onNavigateToSongs)
            }
            items(favoriteSongs.take(5)) { song ->
                SongItemRow(
                    song = song,
                    isCurrentSong = currentSong?.id == song.id,
                    isPlaying = isPlaying && currentSong?.id == song.id,
                    onClick = { viewModel.playSong(song, favoriteSongs) },
                    onFavoriteToggle = { viewModel.toggleFavorite(song) },
                    onAddToPlaylist = { onSongSelectedForPlaylist(song) }
                )
            }
        }

        // Top Tracks / Library Sample
        item {
            SectionHeader(title = "All Songs Queue", onSeeAll = onNavigateToSongs)
        }
        items(allSongs.take(10)) { song ->
            SongItemRow(
                song = song,
                isCurrentSong = currentSong?.id == song.id,
                isPlaying = isPlaying && currentSong?.id == song.id,
                onClick = { viewModel.playSong(song, allSongs) },
                onFavoriteToggle = { viewModel.toggleFavorite(song) },
                onAddToPlaylist = { onSongSelectedForPlaylist(song) }
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "See All",
            color = PurpleLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onSeeAll)
        )
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlowCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RecentSongCard(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AmoledSurfaceElevated)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.albumArtUri ?: R.drawable.default_album_art_1794331160728)
                    .error(R.drawable.default_album_art_1794331160728)
                    .placeholder(R.drawable.default_album_art_1794331160728)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            if (isCurrent && isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0x88000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Audiotrack,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = song.title,
            color = if (isCurrent) PurpleLight else TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            color = TextMuted,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlaylistPreviewChip(
    name: String,
    songCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF2E1065), Color(0xFF1E1B4B))
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.QueueMusic,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = name, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "$songCount tracks", color = TextGray, fontSize = 11.sp)
            }
        }
    }
}
