package com.example.data.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val coverGradientIndex: Int = 0
)

data class VideoItem(
    val id: Long,
    val title: String,
    val durationMs: Long,
    val dataUri: String,
    val thumbnailUri: String? = null,
    val lastPositionMs: Long = 0L,
    val resolution: String = "1080p",
    val folderName: String = "Videos",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis()
) {
    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%d:%02d".format(minutes, seconds)
            }
        }
}

data class EqualizerPreset(
    val name: String,
    val bands: List<Int>, // 5 band values in range [-10, 10] dB
    val bassBoost: Int,   // 0 - 100
    val virtualizer: Int  // 0 - 100
)
