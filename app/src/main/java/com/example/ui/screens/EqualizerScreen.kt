package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.EqualizerController
import com.example.ui.components.GlowCard
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledSurfaceElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

import com.example.audio.VisualizerData
import com.example.ui.components.RealtimeFrequencyBarsVisualizer

@Composable
fun EqualizerScreen(
    equalizerController: EqualizerController,
    visualizerData: VisualizerData = VisualizerData(),
    isPlaying: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val isEnabled by equalizerController.isEnabled.collectAsState()
    val bandLevels by equalizerController.bandLevels.collectAsState()
    val bassBoost by equalizerController.bassBoost.collectAsState()
    val virtualizer by equalizerController.virtualizer.collectAsState()
    val currentPresetName by equalizerController.currentPresetName.collectAsState()

    val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Audio Equalizer",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ABI MUSIC 2.0 HD Audio Engine",
                        color = CyanAccent,
                        fontSize = 12.sp
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { equalizerController.setEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PurpleAccent,
                    uncheckedThumbColor = TextGray,
                    uncheckedTrackColor = AmoledSurfaceElevated
                ),
                modifier = Modifier.testTag("equalizer_master_switch")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live visualizer preview card
        GlowCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRESET: $currentPresetName",
                        color = PurpleLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isEnabled) "ACTIVE" else "BYPASSED",
                        color = if (isEnabled) CyanAccent else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                RealtimeFrequencyBarsVisualizer(
                    magnitudes = visualizerData.magnitudes,
                    peakDots = visualizerData.peakDots,
                    isPlaying = isPlaying && isEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Presets Chips
        Text(
            text = "EQUALIZER PRESETS",
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(equalizerController.presets) { preset ->
                val isSelected = currentPresetName == preset.name
                FilterChip(
                    selected = isSelected,
                    onClick = { equalizerController.applyPreset(preset) },
                    label = { Text(preset.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                    enabled = isEnabled,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurpleAccent,
                        selectedLabelColor = Color.White,
                        containerColor = AmoledSurfaceElevated,
                        labelColor = TextGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5 Frequency Bands Sliders
        Text(
            text = "FREQUENCY BANDS (dB)",
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        GlowCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                bandFrequencies.forEachIndexed { index, freqLabel ->
                    val bandVal = bandLevels.getOrElse(index) { 0 }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = freqLabel, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = if (bandVal > 0) "+$bandVal dB" else "$bandVal dB",
                                color = if (bandVal != 0) CyanAccent else TextGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = bandVal.toFloat(),
                            onValueChange = { newVal ->
                                equalizerController.setBandLevel(index, newVal.toInt())
                            },
                            valueRange = -10f..10f,
                            steps = 19,
                            enabled = isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = PurpleLight,
                                activeTrackColor = PurpleAccent,
                                inactiveTrackColor = Color(0x338B5CF6)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bass Boost & Virtualizer
        Text(
            text = "AUDIO ENHANCEMENTS",
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        GlowCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bass Boost
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = PurpleLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bass Boost", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("$bassBoost%", color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = bassBoost.toFloat(),
                        onValueChange = { equalizerController.setBassBoost(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyanAccent,
                            inactiveTrackColor = Color(0x3306B6D4)
                        )
                    )
                }

                // 3D Virtualizer
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SurroundSound, contentDescription = null, tint = MagentaGlow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3D Virtualizer", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("$virtualizer%", color = MagentaGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = virtualizer.toFloat(),
                        onValueChange = { equalizerController.setVirtualizer(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = MagentaGlow,
                            activeTrackColor = MagentaGlow,
                            inactiveTrackColor = Color(0x33D946EF)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
