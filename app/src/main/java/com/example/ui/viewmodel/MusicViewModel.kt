package com.example.ui.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AbiMusicApplication
import com.example.data.model.Playlist
import com.example.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SongSortOption {
    DATE_ADDED, TITLE, ARTIST, DURATION
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AbiMusicApplication
    val repository = app.mediaRepository
    val playbackManager = app.playbackManager
    val equalizerController = playbackManager.equalizerController
    val audioVisualizer = playbackManager.audioVisualizer
    val visualizerData = audioVisualizer.visualizerData
    val visualizerMode = audioVisualizer.visualizerMode

    val currentSong = playbackManager.currentSong
    val isPlaying = playbackManager.isPlaying
    val currentPositionMs = playbackManager.currentPositionMs
    val durationMs = playbackManager.durationMs
    val repeatMode = playbackManager.repeatMode
    val isShuffle = playbackManager.isShuffle
    val currentQueue = playbackManager.currentQueue

    fun toggleVisualizerMode() = audioVisualizer.toggleVisualizerMode()
    fun setVisualizerMode(mode: com.example.audio.VisualizerMode) = audioVisualizer.setVisualizerMode(mode)

    val allSongs = repository.allSongs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val favoriteSongs = repository.favoriteSongs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentlyPlayed = repository.recentlyPlayed.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allPlaylists = repository.allPlaylists.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SongSortOption.DATE_ADDED)
    val sortOption: StateFlow<SongSortOption> = _sortOption.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()
    private var sleepTimer: CountDownTimer? = null

    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, _searchQuery, _sortOption) { songs, query, sort ->
        val filtered = if (query.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
        }
        when (sort) {
            SongSortOption.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SongSortOption.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
            SongSortOption.DURATION -> filtered.sortedByDescending { it.durationMs }
            SongSortOption.DATE_ADDED -> filtered.sortedByDescending { it.dateAdded }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SongSortOption) {
        _sortOption.value = option
    }

    fun playSong(song: Song, queue: List<Song> = allSongs.value) {
        val targetQueue = if (queue.contains(song)) queue else listOf(song) + queue
        val index = targetQueue.indexOf(song).coerceAtLeast(0)
        playbackManager.playQueue(targetQueue, startIndex = index)
    }

    fun playQueue(queue: List<Song>, startIndex: Int = 0) {
        playbackManager.playQueue(queue, startIndex = startIndex)
    }

    fun playPause() = playbackManager.playPause()
    fun playNext() = playbackManager.playNext()
    fun playPrevious() = playbackManager.playPrevious()
    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)
    fun toggleRepeatMode() = playbackManager.toggleRepeatMode()
    fun toggleShuffle() = playbackManager.toggleShuffle()

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavoriteSong(song.id, song.isFavorite)
        }
    }

    fun createPlaylist(name: String, gradientIndex: Int = 0) {
        viewModelScope.launch {
            repository.createPlaylist(name, gradientIndex)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newName)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun toggleFavoritePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.toggleFavoritePlaylist(playlist.id, playlist.isFavorite)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun rescanMedia() {
        viewModelScope.launch {
            _isScanning.value = true
            repository.scanAudioFiles()
            repository.scanVideoFiles()
            _isScanning.value = false
        }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimer?.cancel()
        _sleepTimerMinutes.value = minutes
        if (minutes != null && minutes > 0) {
            val millis = minutes * 60 * 1000L
            sleepTimer = object : CountDownTimer(millis, 60000L) {
                override fun onTick(millisUntilFinished: Long) {
                    _sleepTimerMinutes.value = (millisUntilFinished / 60000L).toInt() + 1
                }
                override fun onFinish() {
                    _sleepTimerMinutes.value = null
                    if (playbackManager.isPlaying.value) {
                        playbackManager.playPause()
                    }
                }
            }.start()
        }
    }

    override fun onCleared() {
        sleepTimer?.cancel()
        super.onCleared()
    }
}
