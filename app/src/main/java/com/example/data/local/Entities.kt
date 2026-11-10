package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.VideoItem

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val dataUri: String,
    val albumArtUri: String?,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
) {
    fun toSong(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        dataUri = dataUri,
        albumArtUri = albumArtUri,
        isFavorite = isFavorite,
        playCount = playCount,
        lastPlayedTimestamp = lastPlayedTimestamp,
        dateAdded = dateAdded,
        isDemo = isDemo
    )

    companion object {
        fun fromSong(song: Song): SongEntity = SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            durationMs = song.durationMs,
            dataUri = song.dataUri,
            albumArtUri = song.albumArtUri,
            isFavorite = song.isFavorite,
            playCount = song.playCount,
            lastPlayedTimestamp = song.lastPlayedTimestamp,
            dateAdded = song.dateAdded,
            isDemo = song.isDemo
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val coverGradientIndex: Int = 0
) {
    fun toPlaylist(songCount: Int = 0): Playlist = Playlist(
        id = id,
        name = name,
        songCount = songCount,
        createdAt = createdAt,
        isFavorite = isFavorite,
        coverGradientIndex = coverGradientIndex
    )
}

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val durationMs: Long,
    val dataUri: String,
    val thumbnailUri: String?,
    val lastPositionMs: Long = 0L,
    val resolution: String = "1080p",
    val folderName: String = "Videos",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis()
) {
    fun toVideoItem(): VideoItem = VideoItem(
        id = id,
        title = title,
        durationMs = durationMs,
        dataUri = dataUri,
        thumbnailUri = thumbnailUri,
        lastPositionMs = lastPositionMs,
        resolution = resolution,
        folderName = folderName,
        sizeBytes = sizeBytes,
        dateAdded = dateAdded
    )

    companion object {
        fun fromVideoItem(video: VideoItem): VideoEntity = VideoEntity(
            id = video.id,
            title = video.title,
            durationMs = video.durationMs,
            dataUri = video.dataUri,
            thumbnailUri = video.thumbnailUri,
            lastPositionMs = video.lastPositionMs,
            resolution = video.resolution,
            folderName = video.folderName,
            sizeBytes = video.sizeBytes,
            dateAdded = video.dateAdded
        )
    }
}
