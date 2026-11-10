package com.example.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.data.model.EqualizerPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EqualizerController {

    private var hardwareEqualizer: Equalizer? = null
    private var hardwareBassBoost: BassBoost? = null
    private var hardwareVirtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    // 5 frequency bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz (values in dB: -10 to +10)
    private val _bandLevels = MutableStateFlow(listOf(0, 0, 0, 0, 0))
    val bandLevels: StateFlow<List<Int>> = _bandLevels.asStateFlow()

    private val _bassBoost = MutableStateFlow(40) // 0 - 100
    val bassBoost: StateFlow<Int> = _bassBoost.asStateFlow()

    private val _virtualizer = MutableStateFlow(35) // 0 - 100
    val virtualizer: StateFlow<Int> = _virtualizer.asStateFlow()

    private val _currentPresetName = MutableStateFlow("Normal")
    val currentPresetName: StateFlow<String> = _currentPresetName.asStateFlow()

    val presets: List<EqualizerPreset> = listOf(
        EqualizerPreset("Normal", listOf(0, 0, 0, 0, 0), 20, 20),
        EqualizerPreset("Rock", listOf(5, 3, -1, 4, 6), 65, 45),
        EqualizerPreset("Pop", listOf(-1, 2, 5, 2, -2), 40, 30),
        EqualizerPreset("Jazz", listOf(4, 2, -2, 2, 5), 35, 40),
        EqualizerPreset("Classical", listOf(5, 3, -2, 4, 4), 30, 25),
        EqualizerPreset("Dance", listOf(6, 4, 0, 4, 5), 80, 50)
    )

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return
        release()
        currentSessionId = audioSessionId

        try {
            hardwareEqualizer = Equalizer(0, audioSessionId).apply {
                enabled = _isEnabled.value
            }
            hardwareBassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _isEnabled.value
                if (strengthSupported) {
                    setStrength((_bassBoost.value * 10).toShort())
                }
            }
            hardwareVirtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _isEnabled.value
                if (strengthSupported) {
                    setStrength((_virtualizer.value * 10).toShort())
                }
            }
            applyBandsToHardware(_bandLevels.value)
        } catch (e: Exception) {
            Log.w("EqualizerController", "AudioFx initialization note: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        try {
            hardwareEqualizer?.enabled = enabled
            hardwareBassBoost?.enabled = enabled
            hardwareVirtualizer?.enabled = enabled
        } catch (e: Exception) {
            Log.w("EqualizerController", "Failed to toggle AudioFx: ${e.message}")
        }
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        val clamped = levelDb.coerceIn(-10, 10)
        val current = _bandLevels.value.toMutableList()
        if (bandIndex in current.indices) {
            current[bandIndex] = clamped
            _bandLevels.value = current
            _currentPresetName.value = "Custom"
            applyBandToHardware(bandIndex, clamped)
        }
    }

    fun setBassBoost(level: Int) {
        val clamped = level.coerceIn(0, 100)
        _bassBoost.value = clamped
        try {
            if (hardwareBassBoost?.strengthSupported == true) {
                hardwareBassBoost?.setStrength((clamped * 10).toShort())
            }
        } catch (e: Exception) {
            Log.w("EqualizerController", "BassBoost error: ${e.message}")
        }
    }

    fun setVirtualizer(level: Int) {
        val clamped = level.coerceIn(0, 100)
        _virtualizer.value = clamped
        try {
            if (hardwareVirtualizer?.strengthSupported == true) {
                hardwareVirtualizer?.setStrength((clamped * 10).toShort())
            }
        } catch (e: Exception) {
            Log.w("EqualizerController", "Virtualizer error: ${e.message}")
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        _currentPresetName.value = preset.name
        _bandLevels.value = preset.bands
        _bassBoost.value = preset.bassBoost
        _virtualizer.value = preset.virtualizer

        applyBandsToHardware(preset.bands)
        setBassBoost(preset.bassBoost)
        setVirtualizer(preset.virtualizer)
    }

    private fun applyBandsToHardware(bands: List<Int>) {
        bands.forEachIndexed { index, levelDb ->
            applyBandToHardware(index, levelDb)
        }
    }

    private fun applyBandToHardware(bandIndex: Int, levelDb: Int) {
        try {
            val eq = hardwareEqualizer ?: return
            val minBandLevel = eq.bandLevelRange?.get(0) ?: -1500
            val maxBandLevel = eq.bandLevelRange?.get(1) ?: 1500
            if (bandIndex < eq.numberOfBands) {
                val mapped = (levelDb * 100).coerceIn(minBandLevel.toInt(), maxBandLevel.toInt()).toShort()
                eq.setBandLevel(bandIndex.toShort(), mapped)
            }
        } catch (e: Exception) {
            Log.w("EqualizerController", "Equalizer band error: ${e.message}")
        }
    }

    fun release() {
        try {
            hardwareEqualizer?.release()
            hardwareBassBoost?.release()
            hardwareVirtualizer?.release()
        } catch (e: Exception) {
            // Ignored
        } finally {
            hardwareEqualizer = null
            hardwareBassBoost = null
            hardwareVirtualizer = null
        }
    }
}
