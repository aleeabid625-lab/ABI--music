package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VisualizerData
import com.example.audio.VisualizerMode
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.AmoledSurfaceElevated
import com.example.ui.theme.CardBorderBrush
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.TextGray
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        AmoledSurfaceElevated,
                        AmoledSurface
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = CardBorderBrush,
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Interactive Audio Visualizer supporting Frequency Spectrum Bars,
 * Symmetrical Mirrored Waveform, and Radial Halo modes.
 * Dynamically reacts to real-time audio playback frequency and amplitude!
 */
@Composable
fun InteractiveAudioVisualizer(
    visualizerData: VisualizerData,
    visualizerMode: VisualizerMode,
    isPlaying: Boolean,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 68.dp
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Visualizer Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x22100C22))
                .clickable(onClick = onToggleMode)
                .testTag("interactive_visualizer_box"),
            contentAlignment = Alignment.Center
        ) {
            when (visualizerMode) {
                VisualizerMode.FREQUENCY_BARS -> {
                    RealtimeFrequencyBarsVisualizer(
                        magnitudes = visualizerData.magnitudes,
                        peakDots = visualizerData.peakDots,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxWidth().height(height)
                    )
                }
                VisualizerMode.MIRRORED_WAVEFORM -> {
                    RealtimeMirroredWaveformVisualizer(
                        magnitudes = visualizerData.magnitudes,
                        amplitude = visualizerData.amplitude,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxWidth().height(height)
                    )
                }
                VisualizerMode.RADIAL_HALO -> {
                    RealtimeLinearWaveRipples(
                        magnitudes = visualizerData.magnitudes,
                        bassEnergy = visualizerData.bassEnergy,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxWidth().height(height)
                    )
                }
            }

            // Mode indicator pill
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x990A0718))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = visualizerMode.title,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Filled.SyncAlt,
                    contentDescription = "Switch Visualizer Mode",
                    tint = PurpleLight,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

/**
 * Real-time 32-band Frequency Spectrum Bars with glowing gradient and falling peak dots.
 */
@Composable
fun RealtimeFrequencyBarsVisualizer(
    magnitudes: FloatArray,
    peakDots: FloatArray,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barCount = magnitudes.size
        val barWidth = (totalWidth / (barCount * 1.55f)).coerceIn(4f, 22f)
        val spacing = ((totalWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)).coerceAtLeast(1f)

        val gradient = Brush.verticalGradient(
            colors = listOf(
                Color.White,
                CyanAccent,
                PurpleLight,
                PurpleAccent,
                AmoledSurfaceElevated
            ),
            startY = 0f,
            endY = canvasHeight
        )

        for (i in 0 until barCount) {
            val mag = if (isPlaying) magnitudes[i].coerceIn(0.04f, 1.0f) else 0.04f
            val peak = if (isPlaying) peakDots[i].coerceIn(0.04f, 1.0f) else 0.04f

            val barHeight = (canvasHeight - 12f) * mag
            val x = i * (barWidth + spacing)
            val y = canvasHeight - barHeight

            // Draw Frequency Bar
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )

            // Draw Floating Peak Dot (High-end Audio Equipment Style)
            if (isPlaying && peak > mag) {
                val peakY = canvasHeight - ((canvasHeight - 12f) * peak) - 4f
                drawCircle(
                    color = Color.White,
                    radius = (barWidth / 2).coerceAtMost(3.5f),
                    center = Offset(x + barWidth / 2, peakY.coerceAtLeast(4f))
                )
            }
        }
    }
}

/**
 * Real-time symmetrical mirrored waveform rippling up and down from central axis.
 */
@Composable
fun RealtimeMirroredWaveformVisualizer(
    magnitudes: FloatArray,
    amplitude: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val centerY = size.height / 2f
        val count = magnitudes.size
        val barWidth = (totalWidth / (count * 1.4f)).coerceIn(3f, 16f)
        val spacing = ((totalWidth - (barWidth * count)) / (count - 1).coerceAtLeast(1)).coerceAtLeast(1f)

        val upperGradient = Brush.verticalGradient(
            colors = listOf(CyanAccent, PurpleLight),
            startY = 0f,
            endY = centerY
        )
        val lowerGradient = Brush.verticalGradient(
            colors = listOf(PurpleLight, MagentaGlow),
            startY = centerY,
            endY = size.height
        )

        // Draw central glowing baseline
        drawLine(
            color = Color(0x558B5CF6),
            start = Offset(0f, centerY),
            end = Offset(totalWidth, centerY),
            strokeWidth = 1.5f
        )

        for (i in 0 until count) {
            val mag = if (isPlaying) magnitudes[i].coerceIn(0.05f, 1f) else 0.05f
            val halfHeight = (centerY * 0.9f) * mag
            val x = i * (barWidth + spacing)

            // Upper mirror
            drawRoundRect(
                brush = upperGradient,
                topLeft = Offset(x, centerY - halfHeight),
                size = Size(barWidth, halfHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )

            // Lower mirror
            drawRoundRect(
                brush = lowerGradient,
                topLeft = Offset(x, centerY),
                size = Size(barWidth, halfHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

/**
 * Continuous flowing neon audio wave visualizer path.
 */
@Composable
fun RealtimeLinearWaveRipples(
    magnitudes: FloatArray,
    bassEnergy: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val count = magnitudes.size

        val path = Path()
        val pathGlow = Path()

        val stepX = width / (count - 1).coerceAtLeast(1).toFloat()

        for (i in 0 until count) {
            val mag = if (isPlaying) magnitudes[i] else 0.05f
            val offsetVal = (centerY * 0.85f) * mag * if (i % 2 == 0) 1f else -1f
            val x = i * stepX
            val y = centerY + offsetVal

            if (i == 0) {
                path.moveTo(x, y)
                pathGlow.moveTo(x, y)
            } else {
                val prevX = (i - 1) * stepX
                val prevMag = if (isPlaying) magnitudes[i - 1] else 0.05f
                val prevY = centerY + (centerY * 0.85f) * prevMag * if ((i - 1) % 2 == 0) 1f else -1f
                val cx = (prevX + x) / 2f
                path.quadraticTo(cx, prevY, x, y)
                pathGlow.quadraticTo(cx, prevY, x, y)
            }
        }

        // Draw ambient glow stroke
        drawPath(
            path = pathGlow,
            brush = Brush.horizontalGradient(listOf(CyanAccent, PurpleLight, MagentaGlow)),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        // Draw crisp core stroke
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Small animated sound wave indicator for currently playing song list item
 */
@Composable
fun MiniSoundWaveIndicator(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "MiniWave")
    val anim1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse),
        label = "m1"
    )
    val anim2 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "m2"
    )
    val anim3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(380), RepeatMode.Reverse),
        label = "m3"
    )

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val h1 = if (isPlaying) (18 * anim1).dp else 4.dp
        val h2 = if (isPlaying) (18 * anim2).dp else 8.dp
        val h3 = if (isPlaying) (18 * anim3).dp else 6.dp

        Box(Modifier.width(3.dp).height(h1).background(CyanAccent, RoundedCornerShape(2.dp)))
        Box(Modifier.width(3.dp).height(h2).background(PurpleLight, RoundedCornerShape(2.dp)))
        Box(Modifier.width(3.dp).height(h3).background(MagentaGlow, RoundedCornerShape(2.dp)))
    }
}

/**
 * Backward compatible WaveformVisualizer for home and card previews
 */
@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 24,
    maxHeight: Dp = 60.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformTransition")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase2"
    )

    Canvas(modifier = modifier.height(maxHeight)) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.6f)).coerceIn(3f, 16f)
        val spacing = ((totalWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)).coerceAtLeast(1f)

        val gradient = Brush.verticalGradient(
            colors = listOf(CyanAccent, PurpleLight, PurpleAccent, AmoledSurface),
            startY = 0f,
            endY = canvasHeight
        )

        for (i in 0 until barCount) {
            val factor = if (i % 2 == 0) phase1 else phase2
            val dynamicScale = if (isPlaying) {
                val waveFactor = kotlin.math.sin((i.toDouble() / barCount) * Math.PI).toFloat()
                (0.2f + 0.8f * factor * waveFactor).coerceIn(0.15f, 1.0f)
            } else {
                0.15f
            }

            val barHeight = canvasHeight * dynamicScale
            val x = i * (barWidth + spacing)
            val y = canvasHeight - barHeight

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
