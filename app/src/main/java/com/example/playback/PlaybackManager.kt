package com.example.playback

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.audio.EqualizerController
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackManager private constructor(private val context: Context) {

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // Handle audio focus automatically
            )
            repeatMode = Player.REPEAT_MODE_ALL
            addListener(playerListener)
        }
    }

    val equalizerController = EqualizerController()
    val audioVisualizer = com.example.audio.RealtimeAudioVisualizer(context)

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_ALL)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var progressTrackingJob: Job? = null
    var onSongPlaybackEnded: ((Song) -> Unit)? = null

    private val playerListener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            audioVisualizer.updatePlaybackState(isPlaying)
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                    equalizerController.attachAudioSession(exoPlayer.audioSessionId)
                    audioVisualizer.attachAudioSession(exoPlayer.audioSessionId)
                }
                Player.STATE_ENDED -> {
                    // Critical Bug Fix: If repeat mode is REPEAT_MODE_ALL, ExoPlayer continues.
                    // If queue reached the end and repeat is not off, loop to first item
                    Log.d(TAG, "Queue reached STATE_ENDED, seamlessly looping queue")
                    if (exoPlayer.repeatMode == Player.REPEAT_MODE_ALL) {
                        exoPlayer.seekTo(0, 0L)
                        exoPlayer.play()
                    }
                }
                Player.STATE_BUFFERING -> {}
                Player.STATE_IDLE -> {}
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val mediaId = mediaItem?.mediaId
            Log.d(TAG, "Media item transitioned automatically. Reason: $reason, ID: $mediaId")
            val foundSong = _currentQueue.value.find { it.id.toString() == mediaId }
                ?: if (exoPlayer.currentMediaItemIndex in _currentQueue.value.indices) {
                    _currentQueue.value[exoPlayer.currentMediaItemIndex]
                } else null

            _currentSong.value = foundSong
            _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
            foundSong?.let { onSongPlaybackEnded?.invoke(it) }
        }

        override fun onPlayerError(error: PlaybackException) {
            // Critical Bug Fix: Automatically skip corrupt or unavailable tracks without stopping queue!
            Log.e(TAG, "Playback error on track: ${error.message}. Automatically skipping to next track.")
            Handler(Looper.getMainLooper()).postDelayed({
                if (exoPlayer.hasNextMediaItem()) {
                    exoPlayer.seekToNextMediaItem()
                    exoPlayer.play()
                } else if (_currentQueue.value.isNotEmpty()) {
                    exoPlayer.seekTo(0, 0L)
                    exoPlayer.play()
                }
            }, 300)
        }
    }

    /**
     * Prepares and starts playback for the entire queue of songs.
     * Passes all media items to ExoPlayer so ExoPlayer's internal queue engine
     * advances automatically from song 1 -> song 2 -> ... -> song 50 without stopping!
     */
    fun playQueue(songs: List<Song>, startIndex: Int = 0, initialPositionMs: Long = 0L) {
        if (songs.isEmpty()) return

        _currentQueue.value = songs
        val clampedIndex = startIndex.coerceIn(0, songs.size - 1)
        _currentSong.value = songs[clampedIndex]

        val mediaItems = songs.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .build()

            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(Uri.parse(song.dataUri))
                .setMediaMetadata(metadata)
                .build()
        }

        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItems(mediaItems, clampedIndex, initialPositionMs)
        exoPlayer.prepare()
        exoPlayer.play()

        _isPlaying.value = true
        startProgressTracker()
    }

    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0, 0L)
            }
            exoPlayer.play()
        }
    }

    fun playNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            exoPlayer.play()
        } else if (_currentQueue.value.isNotEmpty()) {
            // Loop back to beginning
            exoPlayer.seekTo(0, 0L)
            exoPlayer.play()
        }
    }

    fun playPrevious() {
        if (exoPlayer.currentPosition > 3000L) {
            exoPlayer.seekTo(0L)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
            exoPlayer.play()
        } else if (_currentQueue.value.isNotEmpty()) {
            val lastIndex = _currentQueue.value.size - 1
            exoPlayer.seekTo(lastIndex, 0L)
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs.coerceAtLeast(0L))
        _currentPositionMs.value = positionMs
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_ALL
        }
        _repeatMode.value = nextMode
        exoPlayer.repeatMode = nextMode
    }

    fun toggleShuffle() {
        val nextState = !_isShuffle.value
        _isShuffle.value = nextState
        exoPlayer.shuffleModeEnabled = nextState
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer.setPlaybackSpeed(speed)
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _currentPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    fun release() {
        stopProgressTracker()
        audioVisualizer.release()
        equalizerController.release()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }

    companion object {
        private const val TAG = "PlaybackManager"

        @Volatile
        private var INSTANCE: PlaybackManager? = null

        fun getInstance(context: Context): PlaybackManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PlaybackManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
