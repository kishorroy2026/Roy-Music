package com.example

import android.app.Application
import com.example.data.local.RoyMusicDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.MusicRepository
import com.example.playback.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RoyMusicApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var database: RoyMusicDatabase
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var musicRepository: MusicRepository
        private set

    lateinit var playbackController: PlaybackController
        private set

    override fun onCreate() {
        super.onCreate()

        database = RoyMusicDatabase.getDatabase(this, applicationScope)
        userPreferencesRepository = UserPreferencesRepository(this)

        musicRepository = MusicRepository(
            songDao = database.songDao(),
            moodDao = database.moodDao(),
            playlistDao = database.playlistDao(),
            songStatsDao = database.songStatsDao(),
            historyDao = database.historyDao(),
            preferencesRepository = userPreferencesRepository
        )

        playbackController = PlaybackController(
            context = this,
            musicRepository = musicRepository,
            scope = applicationScope
        )

        // Initial scan of device and demo tracks in background
        applicationScope.launch(Dispatchers.IO) {
            musicRepository.rescanLibrary(this@RoyMusicApplication)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        playbackController.release()
    }
}
