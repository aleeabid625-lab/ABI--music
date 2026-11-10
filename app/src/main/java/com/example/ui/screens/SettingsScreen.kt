package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.GlowCard
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledSurfaceElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    onNavigateToEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isScanning by viewModel.isScanning.collectAsState()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsState()
    var showSleepMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Text(
                text = "Settings",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Customize your playback & audio experience",
                color = TextGray,
                fontSize = 13.sp
            )
        }

        // Continuous Playback Engine Status
        item {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x2210B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automatic Next-Song Engine",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Continuous queue active. Transitions automatically to next track upon finish.",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section: Audio & Playback
        item {
            Text(
                text = "AUDIO & PLAYBACK",
                color = PurpleLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsItemRow(
                        icon = Icons.Filled.GraphicEq,
                        iconTint = PurpleLight,
                        title = "Audio Equalizer",
                        subtitle = "5-band EQ, Bass Boost, 3D Virtualizer",
                        onClick = onNavigateToEqualizer
                    )

                    SettingsDivider()

                    // Sleep Timer
                    Box {
                        SettingsItemRow(
                            icon = Icons.Filled.Bedtime,
                            iconTint = CyanAccent,
                            title = "Sleep Timer",
                            subtitle = if (sleepTimerMinutes != null) "Turns off in $sleepTimerMinutes min" else "Off",
                            onClick = { showSleepMenu = true }
                        )

                        DropdownMenu(
                            expanded = showSleepMenu,
                            onDismissRequest = { showSleepMenu = false },
                            modifier = Modifier.background(AmoledSurfaceElevated)
                        ) {
                            listOf(
                                null to "Off",
                                15 to "15 minutes",
                                30 to "30 minutes",
                                45 to "45 minutes",
                                60 to "60 minutes"
                            ).forEach { (mins, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = TextWhite) },
                                    onClick = {
                                        viewModel.setSleepTimer(mins)
                                        showSleepMenu = false
                                        Toast.makeText(context, "Sleep Timer: $label", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    SettingsDivider()

                    SettingsItemRow(
                        icon = Icons.Filled.DarkMode,
                        iconTint = MagentaGlow,
                        title = "Theme",
                        subtitle = "AMOLED Pitch Black (Pure Contrast)",
                        onClick = {
                            Toast.makeText(context, "AMOLED Dark theme is permanently active", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Section: Media Library
        item {
            Text(
                text = "LIBRARY & STORAGE",
                color = PurpleLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    // Rescan
                    SettingsItemRow(
                        icon = Icons.Filled.Refresh,
                        iconTint = CyanAccent,
                        title = "Rescan Media Library",
                        subtitle = if (isScanning) "Scanning storage..." else "Scan for new songs and videos",
                        trailingContent = {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = CyanAccent,
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        onClick = {
                            if (!isScanning) {
                                viewModel.rescanMedia()
                                Toast.makeText(context, "Scanning media files...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    SettingsDivider()

                    // Clear cache
                    SettingsItemRow(
                        icon = Icons.Filled.CleaningServices,
                        iconTint = Color(0xFFF59E0B),
                        title = "Clear Cache",
                        subtitle = "Free temporary thumbnails & buffers",
                        onClick = {
                            try {
                                context.cacheDir.deleteRecursively()
                                Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        // Supported Formats Info
        item {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Supported Formats",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Audio: MP3, WAV, FLAC, AAC, M4A, OGG\nVideo: MP4, MKV, WEBM, 3GP",
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // About ABI MUSIC Card
        item {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AmoledSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.drawable.ic_app_foreground_1794331122649,
                            contentDescription = "ABI MUSIC",
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ABI MUSIC",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 2.0 (Build 2026)",
                        color = PurpleLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Premium offline audio and video player built with Jetpack Compose & Media3 ExoPlayer.",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = TextGray, fontSize = 12.sp)
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0x1A8B5CF6))
    )
}
