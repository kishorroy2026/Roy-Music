package com.example.data.model

data class FolderInfo(
    val name: String,
    val path: String,
    val songCount: Int,
    val totalDurationMs: Long,
    val songs: List<Song> = emptyList(),
    val subFolders: List<FolderInfo> = emptyList()
) {
    val formattedDuration: String
        get() {
            val totalMinutes = totalDurationMs / 1000 / 60
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) "%dh %dm".format(hours, minutes) else "%d mins".format(minutes)
        }
}
