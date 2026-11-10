package com.example

import android.app.Application
import android.content.Intent
import android.os.Build
import com.example.data.local.MusicDatabase
import com.example.data.repository.MediaRepository
import com.example.playback.PlaybackManager
import com.example.service.MusicPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AbiMusicApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var mediaRepository: MediaRepository
        private set
    lateinit var playbackManager: PlaybackManager
        private set

    override fun onCreate() {
        super.onCreate()
        MusicDatabase.getInstance(this)
        mediaRepository = MediaRepository(this)
        playbackManager = PlaybackManager.getInstance(this)

        // Connect playback tracking to database history
        playbackManager.onSongPlaybackEnded = { song ->
            applicationScope.launch {
                mediaRepository.recordPlayback(song.id)
            }
        }

        // Start playback service for MediaSession lifecycle
        try {
            val serviceIntent = Intent(this, MusicPlaybackService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) {
            // Android 8+ foreground service rules handled when playback starts
        }

        // Trigger background initial media scan
        applicationScope.launch {
            mediaRepository.scanAudioFiles()
            mediaRepository.scanVideoFiles()
        }
    }
}
