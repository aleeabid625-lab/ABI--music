package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AbiMusicApplication
import com.example.data.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AbiMusicApplication
    private val repository = app.mediaRepository

    val allVideos = repository.allVideos.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder.asStateFlow()

    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()

    val filteredVideos: StateFlow<List<VideoItem>> = combine(allVideos, _searchQuery, _selectedFolder) { videos, query, folder ->
        videos.filter { video ->
            val matchesQuery = query.isBlank() || video.title.contains(query, ignoreCase = true)
            val matchesFolder = folder == null || video.folderName.equals(folder, ignoreCase = true)
            matchesQuery && matchesFolder
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val videoFolders: StateFlow<List<String>> = allVideos.combine(_selectedFolder) { videos, _ ->
        videos.map { it.folderName }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFolder(folder: String?) {
        _selectedFolder.value = folder
    }

    fun selectVideo(video: VideoItem) {
        _currentVideo.value = video
    }

    fun clearCurrentVideo() {
        _currentVideo.value = null
    }

    fun saveLastPosition(videoId: Long, positionMs: Long) {
        viewModelScope.launch {
            repository.updateVideoLastPosition(videoId, positionMs)
        }
    }
}
