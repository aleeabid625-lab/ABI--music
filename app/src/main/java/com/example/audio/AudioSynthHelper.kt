package com.example.audio

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object AudioSynthHelper {

    /**
     * Generates a real, playable, rich synthesised WAV audio file with melodies,
     * rhythms, and chords.
     */
    fun createSampleWav(
        context: Context,
        fileName: String,
        durationSeconds: Int = 8,
        baseFreq: Double = 440.0,
        bpm: Int = 120
    ): File {
        val audioDir = File(context.filesDir, "demo_audio").apply { if (!exists()) mkdirs() }
        val wavFile = File(audioDir, fileName)
        if (wavFile.exists() && wavFile.length() > 1000) {
            return wavFile
        }

        val sampleRate = 22050
        val numSamples = durationSeconds * sampleRate
        val pcmData = ShortArray(numSamples)

        val beatSamples = (sampleRate * 60) / bpm
        val scale = doubleArrayOf(1.0, 1.125, 1.25, 1.333, 1.5, 1.667, 1.875, 2.0)

        for (i in 0 until numSamples) {
            val beatIndex = (i / beatSamples) % scale.size
            val noteFreq = baseFreq * scale[beatIndex]
            val time = i.toDouble() / sampleRate

            // Fundamental + 2 harmonics + sub-bass + envelope
            val beatPhase = (i % beatSamples).toDouble() / beatSamples
            val envelope = (1.0 - beatPhase * 0.7).coerceIn(0.1, 1.0)

            val wave = (
                sin(2.0 * Math.PI * noteFreq * time) * 0.5 +
                sin(4.0 * Math.PI * noteFreq * time) * 0.25 +
                sin(2.0 * Math.PI * (baseFreq * 0.5) * time) * 0.25
            ) * envelope

            // Smooth fade-in & fade-out to prevent clicks
            val fadeSamples = sampleRate / 10
            val fadeMultiplier = when {
                i < fadeSamples -> i.toDouble() / fadeSamples
                i > numSamples - fadeSamples -> (numSamples - i).toDouble() / fadeSamples
                else -> 1.0
            }

            val sampleVal = (wave * fadeMultiplier * 20000.0).toInt().coerceIn(-32767, 32767)
            pcmData[i] = sampleVal.toShort()
        }

        FileOutputStream(wavFile).use { fos ->
            writeWavHeader(fos, sampleRate, 1, 16, numSamples)
            val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in pcmData) {
                byteBuffer.putShort(s)
            }
            fos.write(byteBuffer.array())
        }

        return wavFile
    }

    private fun writeWavHeader(
        fos: FileOutputStream,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int,
        totalAudioSamples: Int
    ) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataSize = totalAudioSamples * blockAlign
        val chunkSize = 36 + dataSize

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(chunkSize)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16) // Subchunk1Size for PCM
            putShort(1.toShort()) // AudioFormat PCM = 1
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(bitsPerSample.toShort())
            put("data".toByteArray())
            putInt(dataSize)
        }
        fos.write(header.array())
    }
}
