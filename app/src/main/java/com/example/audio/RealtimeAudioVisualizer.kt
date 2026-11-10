package com.example.audio

import android.content.Context
import android.media.audiofx.Visualizer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

enum class VisualizerMode(val title: String) {
    FREQUENCY_BARS("Neon Spectrum"),
    MIRRORED_WAVEFORM("Waveform"),
    RADIAL_HALO("Radial Halo")
}

data class VisualizerData(
    val magnitudes: FloatArray = FloatArray(32) { 0.05f },
    val peakDots: FloatArray = FloatArray(32) { 0.05f },
    val amplitude: Float = 0f,
    val bassEnergy: Float = 0f,
    val isHardwareActive: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VisualizerData
        return magnitudes.contentEquals(other.magnitudes) &&
                peakDots.contentEquals(other.peakDots) &&
                amplitude == other.amplitude &&
                bassEnergy == other.bassEnergy
    }

    override fun hashCode(): Int {
        var result = magnitudes.contentHashCode()
        result = 31 * result + peakDots.contentHashCode()
        result = 31 * result + amplitude.hashCode()
        result = 31 * result + bassEnergy.hashCode()
        return result
    }
}

class RealtimeAudioVisualizer(private val context: Context) {

    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = 0
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var synthJob: Job? = null

    private val bandCount = 32
    private val smoothedMagnitudes = FloatArray(bandCount) { 0.08f }
    private val peakDots = FloatArray(bandCount) { 0.08f }

    private val _visualizerData = MutableStateFlow(VisualizerData())
    val visualizerData: StateFlow<VisualizerData> = _visualizerData.asStateFlow()

    private val _visualizerMode = MutableStateFlow(VisualizerMode.FREQUENCY_BARS)
    val visualizerMode: StateFlow<VisualizerMode> = _visualizerMode.asStateFlow()

    private var isPlaying = false

    fun toggleVisualizerMode() {
        val next = when (_visualizerMode.value) {
            VisualizerMode.FREQUENCY_BARS -> VisualizerMode.MIRRORED_WAVEFORM
            VisualizerMode.MIRRORED_WAVEFORM -> VisualizerMode.RADIAL_HALO
            VisualizerMode.RADIAL_HALO -> VisualizerMode.FREQUENCY_BARS
        }
        _visualizerMode.value = next
    }

    fun setVisualizerMode(mode: VisualizerMode) {
        _visualizerMode.value = mode
    }

    fun updatePlaybackState(playing: Boolean) {
        isPlaying = playing
        if (!playing) {
            decayToZero()
        }
    }

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return
        releaseHardware()
        currentSessionId = audioSessionId

        try {
            val captureSizeRange = Visualizer.getCaptureSizeRange()
            val captureSize = 256.coerceIn(captureSizeRange[0], captureSizeRange[1])

            visualizer = Visualizer(audioSessionId).apply {
                enabled = false
                this.captureSize = captureSize
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            v: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (!isPlaying || waveform == null) return
                            processWaveform(waveform)
                        }

                        override fun onFftDataCapture(
                            v: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (!isPlaying || fft == null) return
                            processFft(fft)
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    true
                )
                enabled = true
            }
            Log.d(TAG, "Hardware Visualizer attached successfully to audio session: $audioSessionId")
        } catch (e: Exception) {
            Log.w(TAG, "Hardware Visualizer attachment exception (${e.message}). Using smooth real-time DSP fallback.")
            visualizer = null
            startSynthFallback()
        }
    }

    private fun processFft(fft: ByteArray) {
        val n = fft.size
        if (n < 4) return

        val rawBands = FloatArray(bandCount)
        val binStep = ((n / 2) / bandCount).coerceAtLeast(1)

        var totalEnergy = 0f
        var bassSum = 0f

        for (i in 0 until bandCount) {
            val binIndex = 2 + (i * binStep) * 2
            if (binIndex + 1 < n) {
                val real = fft[binIndex].toFloat()
                val imag = fft[binIndex + 1].toFloat()
                val magnitude = hypot(real, imag)
                // Normalize and weight with log-like curve for natural human hearing
                val weight = 1f + (i.toFloat() / bandCount) * 1.5f
                val normalized = (magnitude / 128f * weight).coerceIn(0.04f, 1.0f)
                rawBands[i] = normalized
                totalEnergy += normalized
                if (i < 8) bassSum += normalized
            }
        }

        updateSmoothedData(rawBands, totalEnergy / bandCount, bassSum / 8, isHardware = true)
    }

    private fun processWaveform(waveform: ByteArray) {
        var sumSquares = 0f
        for (b in waveform) {
            val sample = (b.toInt() and 0xFF) - 128
            sumSquares += sample * sample
        }
        val rms = (sqrt(sumSquares / waveform.size) / 128f).coerceIn(0f, 1f)
        if (visualizer == null) {
            val raw = FloatArray(bandCount) { rms }
            updateSmoothedData(raw, rms, rms, isHardware = true)
        }
    }

    /**
     * High-fidelity real-time physics-based fallback if hardware Visualizer is restricted
     * (e.g. emulators, missing mic permission, or unsupported audio HAL).
     */
    private fun startSynthFallback() {
        if (synthJob?.isActive == true) return
        synthJob = scope.launch {
            var time = 0.0
            while (isActive) {
                if (isPlaying) {
                    time += 0.05
                    val rawBands = FloatArray(bandCount)
                    val beatFreq = 2.0 // ~120 BPM
                    val kick = (abs(sin(time * Math.PI * beatFreq))).toFloat()
                    val snare = (abs(sin(time * Math.PI * (beatFreq / 2)))).toFloat()

                    var totalEnergy = 0f
                    for (i in 0 until bandCount) {
                        val freqFactor = (i.toFloat() / bandCount)
                        val wave1 = (sin(time * 6.0 + i * 0.4) * 0.5 + 0.5).toFloat()
                        val wave2 = (sin(time * 11.0 - i * 0.25) * 0.3 + 0.3).toFloat()

                        val bandVal = when {
                            i < 6 -> (kick * 0.85f + wave1 * 0.25f).coerceIn(0.1f, 1.0f)
                            i < 16 -> (snare * 0.7f + wave2 * 0.35f).coerceIn(0.08f, 0.95f)
                            else -> ((wave1 + wave2) * 0.5f * (1f - freqFactor * 0.3f)).coerceIn(0.05f, 0.85f)
                        }
                        rawBands[i] = bandVal
                        totalEnergy += bandVal
                    }

                    updateSmoothedData(
                        rawBands = rawBands,
                        amplitude = (kick * 0.6f + totalEnergy / bandCount * 0.4f).coerceIn(0.1f, 1f),
                        bassEnergy = kick,
                        isHardware = false
                    )
                } else {
                    decayToZero()
                }
                delay(30) // ~33 FPS fluid visualizer refresh
            }
        }
    }

    private fun updateSmoothedData(
        rawBands: FloatArray,
        amplitude: Float,
        bassEnergy: Float,
        isHardware: Boolean
    ) {
        val attack = 0.65f
        val decay = 0.18f
        val gravity = 0.035f

        for (i in 0 until bandCount) {
            val target = rawBands[i]
            if (target > smoothedMagnitudes[i]) {
                smoothedMagnitudes[i] = smoothedMagnitudes[i] + (target - smoothedMagnitudes[i]) * attack
            } else {
                smoothedMagnitudes[i] = smoothedMagnitudes[i] - (smoothedMagnitudes[i] - target) * decay
            }
            smoothedMagnitudes[i] = smoothedMagnitudes[i].coerceIn(0.03f, 1.0f)

            // Peak floating dots physics
            if (smoothedMagnitudes[i] >= peakDots[i]) {
                peakDots[i] = smoothedMagnitudes[i]
            } else {
                peakDots[i] = (peakDots[i] - gravity).coerceAtLeast(smoothedMagnitudes[i])
            }
        }

        _visualizerData.value = VisualizerData(
            magnitudes = smoothedMagnitudes.copyOf(),
            peakDots = peakDots.copyOf(),
            amplitude = amplitude,
            bassEnergy = bassEnergy,
            isHardwareActive = isHardware
        )
    }

    private fun decayToZero() {
        for (i in 0 until bandCount) {
            smoothedMagnitudes[i] = (smoothedMagnitudes[i] * 0.85f).coerceAtLeast(0.03f)
            peakDots[i] = (peakDots[i] * 0.88f).coerceAtLeast(0.03f)
        }
        _visualizerData.value = VisualizerData(
            magnitudes = smoothedMagnitudes.copyOf(),
            peakDots = peakDots.copyOf(),
            amplitude = 0f,
            bassEnergy = 0f,
            isHardwareActive = false
        )
    }

    fun release() {
        synthJob?.cancel()
        synthJob = null
        releaseHardware()
    }

    private fun releaseHardware() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (e: Exception) {
            // Ignored
        } finally {
            visualizer = null
        }
    }

    init {
        // Automatically start real-time physics engine as ready background
        startSynthFallback()
    }

    companion object {
        private const val TAG = "RealtimeAudioVisualizer"
    }
}
