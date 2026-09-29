package com.example.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class PlaybackController(
    private val context: Context,
    private val musicRepository: MusicRepository,
    private val scope: CoroutineScope
) {
    val audioEffectsManager = AudioEffectsManager()

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF) // 0: OFF, 1: ONE, 2: ALL
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

    private val _sleepTimerSecondsLeft = MutableStateFlow<Long?>(null)
    val sleepTimerSecondsLeft: StateFlow<Long?> = _sleepTimerSecondsLeft.asStateFlow()

    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    // Tracking variables for automatic favorite calculation
    private var sessionAccumulatedTimeMs = 0L
    private var lastTickTimestamp = 0L
    private var hasRecordedForCurrentSong = false

    init {
        audioEffectsManager.attachAudioSession(player.audioSessionId)

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _isBuffering.value = (playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    _duration.value = player.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    onSongEnded()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                commitCurrentSessionPlayback()
                val index = player.currentMediaItemIndex
                if (index in _queue.value.indices) {
                    _queueIndex.value = index
                    _currentSong.value = _queue.value[index]
                    _duration.value = _queue.value[index].duration
                    _currentPosition.value = 0L
                    resetPlayTracking()
                }
            }
        })
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        lastTickTimestamp = System.currentTimeMillis()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive && player.isPlaying) {
                val now = System.currentTimeMillis()
                val elapsed = now - lastTickTimestamp
                lastTickTimestamp = now

                if (elapsed in 1..2000) {
                    sessionAccumulatedTimeMs += elapsed
                }

                _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                if (player.duration > 0) {
                    _duration.value = player.duration
                }

                // Check qualifying threshold on the fly if not yet recorded
                checkQualifyingThreshold()

                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun checkQualifyingThreshold() {
        val song = _currentSong.value ?: return
        if (hasRecordedForCurrentSong) return

        val totalDuration = if (song.duration > 0) song.duration else _duration.value
        if (totalDuration <= 0) return

        val fraction = sessionAccumulatedTimeMs.toFloat() / totalDuration.toFloat()
        val seconds = sessionAccumulatedTimeMs / 1000

        // Qualifies if user played >= 70% or >= 60 seconds
        if (fraction >= 0.70f || seconds >= 60) {
            hasRecordedForCurrentSong = true
            scope.launch {
                musicRepository.recordPlayback(song.id, sessionAccumulatedTimeMs, totalDuration)
            }
        }
    }

    private fun commitCurrentSessionPlayback() {
        val song = _currentSong.value ?: return
        if (!hasRecordedForCurrentSong && sessionAccumulatedTimeMs >= 10000) {
            // Record even partial listen to update lastPlayed and play history
            val totalDuration = if (song.duration > 0) song.duration else _duration.value
            scope.launch {
                musicRepository.recordPlayback(song.id, sessionAccumulatedTimeMs, totalDuration)
            }
        }
    }

    private fun resetPlayTracking() {
        sessionAccumulatedTimeMs = 0L
        lastTickTimestamp = System.currentTimeMillis()
        hasRecordedForCurrentSong = false
    }

    private fun onSongEnded() {
        commitCurrentSessionPlayback()
        if (_repeatMode.value == Player.REPEAT_MODE_ONE) {
            seekTo(0)
            player.play()
        } else {
            skipNext()
        }
    }

    fun playSong(song: Song, newQueue: List<Song> = listOf(song), startIndex: Int = -1) {
        val index = if (startIndex >= 0) startIndex else newQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        _queue.value = newQueue
        _queueIndex.value = index
        _currentSong.value = song
        resetPlayTracking()

        val mediaItems = newQueue.map { s ->
            val uri = if (s.contentUri.isNotBlank()) {
                Uri.parse(s.contentUri)
            } else {
                Uri.fromFile(File(s.path))
            }
            MediaItem.Builder()
                .setMediaId(s.id.toString())
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .build()
                )
                .build()
        }

        player.setMediaItems(mediaItems, index, 0L)
        player.prepare()
        player.play()
    }

    fun playPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0L))
        _currentPosition.value = positionMs.coerceAtLeast(0L)
    }

    fun seekForward10() {
        seekTo(player.currentPosition + 10000L)
    }

    fun seekBackward10() {
        seekTo(player.currentPosition - 10000L)
    }

    fun skipNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        var nextIndex = _queueIndex.value + 1
        if (nextIndex >= q.size) {
            if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
                nextIndex = 0
            } else {
                player.pause()
                return
            }
        }

        _queueIndex.value = nextIndex
        val nextSong = q[nextIndex]
        _currentSong.value = nextSong
        resetPlayTracking()
        player.seekTo(nextIndex, 0L)
        player.play()
    }

    fun skipPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (player.currentPosition > 3000L) {
            seekTo(0)
            return
        }

        var prevIndex = _queueIndex.value - 1
        if (prevIndex < 0) {
            prevIndex = if (_repeatMode.value == Player.REPEAT_MODE_ALL) q.size - 1 else 0
        }

        _queueIndex.value = prevIndex
        val prevSong = q[prevIndex]
        _currentSong.value = prevSong
        resetPlayTracking()
        player.seekTo(prevIndex, 0L)
        player.play()
    }

    fun toggleShuffle() {
        val currentShuffle = !_isShuffle.value
        _isShuffle.value = currentShuffle
        val current = _currentSong.value
        val currentQ = _queue.value
        if (currentShuffle && currentQ.size > 1) {
            val shuffled = currentQ.toMutableList()
            if (current != null) {
                shuffled.remove(current)
                shuffled.shuffle()
                shuffled.add(0, current)
            } else {
                shuffled.shuffle()
            }
            _queue.value = shuffled
            _queueIndex.value = 0
        }
    }

    fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = next
        player.repeatMode = next
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        player.playbackParameters = PlaybackParameters(speed)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutes.value = minutes
        if (minutes <= 0) {
            _sleepTimerSecondsLeft.value = null
            return
        }

        var remainingSeconds = minutes * 60L
        _sleepTimerSecondsLeft.value = remainingSeconds

        sleepTimerJob = scope.launch(Dispatchers.Main) {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                _sleepTimerSecondsLeft.value = remainingSeconds
            }
            player.pause()
            _sleepTimerMinutes.value = null
            _sleepTimerSecondsLeft.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerMinutes.value = null
        _sleepTimerSecondsLeft.value = null
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val currentList = _queue.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            _queue.value = currentList
            val current = _currentSong.value
            if (current != null) {
                _queueIndex.value = currentList.indexOfFirst { it.id == current.id }
            }
        }
    }

    fun removeFromQueue(index: Int) {
        val currentList = _queue.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _queue.value = currentList
            val current = _currentSong.value
            if (current != null) {
                _queueIndex.value = currentList.indexOfFirst { it.id == current.id }
            }
        }
    }

    fun addToQueueNext(song: Song) {
        val currentList = _queue.value.toMutableList()
        val insertPos = (_queueIndex.value + 1).coerceAtMost(currentList.size)
        currentList.add(insertPos, song)
        _queue.value = currentList
    }

    fun addToQueueEnd(song: Song) {
        val currentList = _queue.value.toMutableList()
        currentList.add(song)
        _queue.value = currentList
    }

    fun release() {
        stopProgressTracker()
        cancelSleepTimer()
        audioEffectsManager.release()
        player.release()
    }
}
