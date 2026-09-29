package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ListeningHistory
import com.example.data.model.Mood
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import com.example.data.model.SongMoodCrossRef
import com.example.data.model.SongStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Song::class,
        Mood::class,
        SongMoodCrossRef::class,
        Playlist::class,
        PlaylistSongCrossRef::class,
        SongStats::class,
        ListeningHistory::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RoyMusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun moodDao(): MoodDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun songStatsDao(): SongStatsDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: RoyMusicDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): RoyMusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoyMusicDatabase::class.java,
                    "roy_music_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default moods on database creation
                            scope.launch(Dispatchers.IO) {
                                INSTANCE?.moodDao()?.insertMoods(Mood.DEFAULT_MOODS)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
