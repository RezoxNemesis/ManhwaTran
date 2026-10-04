package com.rezoxnemesis.muse.playback

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.session.MediaController
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.rezoxnemesis.muse.data.MuseSoundProfile
import com.rezoxnemesis.muse.data.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class QueueUiItem(
    val mediaId: Long?,
    val title: String,
    val artist: String,
)

data class PlaybackUiState(
    val connected: Boolean = false,
    val currentMediaId: Long? = null,
    val currentIndex: Int = -1,
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queue: List<QueueUiItem> = emptyList(),
    val errorMessage: String? = null,
)

data class SleepTimerUiState(
    val connected: Boolean = false,
    val active: Boolean = false,
    val remainingMs: Long = 0L,
    val deadlineWallMs: Long = 0L,
)

data class AudioEffectsUiState(
    val connected: Boolean = false,
    val sessionReady: Boolean = false,
    val masterEnabled: Boolean = true,
    val bypass: Boolean = false,
    val equalizerAvailable: Boolean = false,
    val bandCentersHz: List<Int> = emptyList(),
    val bandMinMb: Int = 0,
    val bandMaxMb: Int = 0,
    val bandLevelsMb: List<Int> = emptyList(),
    val presetNames: List<String> = emptyList(),
    val bassAvailable: Boolean = false,
    val bassEnabled: Boolean = false,
    val bassStrength: Int = 0,
    val virtualizerAvailable: Boolean = false,
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 0,
    val loudnessAvailable: Boolean = false,
    val loudnessEnabled: Boolean = false,
    val loudnessGainMb: Int = 0,
)

class MusePlaybackController(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val mainExecutor = ContextCompat.getMainExecutor(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val token = SessionToken(
        appContext,
        ComponentName(appContext, MusePlaybackService::class.java),
    )
    private val controllerFuture = MediaController.Builder(appContext, token).buildAsync()
    private var controller: MediaController? = null
    private var tickerJob: Job? = null

    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private val _audioEffects = MutableStateFlow(AudioEffectsUiState())
    val audioEffects: StateFlow<AudioEffectsUiState> = _audioEffects.asStateFlow()

    private val _sleepTimer = MutableStateFlow(SleepTimerUiState())
    val sleepTimer: StateFlow<SleepTimerUiState> = _sleepTimer.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncFrom(player)
        }

        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            refreshAudioEffects()
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int,
        ) {
            _state.value = _state.value.copy(errorMessage = null)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                _state.value = _state.value.copy(errorMessage = null)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.value = _state.value.copy(
                errorMessage = "Muse could not play this track. You can skip it or remove it from the queue.",
            )
        }
    }

    init {
        controllerFuture.addListener(
            {
                runCatching { controllerFuture.get() }
                    .onSuccess { mediaController ->
                        controller = mediaController
                        mediaController.addListener(listener)
                        syncFrom(mediaController)
                        startTicker()
                        refreshAudioEffects()
                        refreshSleepTimer()
                    }
            },
            mainExecutor,
        )
    }

    fun playTracks(tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty()) return
        controller?.apply {
            val safeIndex = startIndex.coerceIn(tracks.indices)
            setMediaItems(tracks.map(::toMediaItem), safeIndex, 0L)
            prepare()
            play()
        }
    }

    fun playPause() {
        controller?.let { player ->
            if (player.isPlaying) player.pause() else player.play()
        }
    }

    fun pause() {
        controller?.pause()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
    }

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun cycleRepeat() {
        controller?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun playNext(track: Track) {
        controller?.let { player ->
            val insertionIndex = if (player.currentMediaItemIndex >= 0) {
                (player.currentMediaItemIndex + 1).coerceAtMost(player.mediaItemCount)
            } else {
                0
            }
            player.addMediaItem(insertionIndex, toMediaItem(track))
        }
    }

    fun addToQueue(track: Track) {
        controller?.addMediaItem(toMediaItem(track))
    }

    fun removeQueueItemsByMediaId(mediaId: Long) {
        controller?.let { player ->
            for (index in player.mediaItemCount - 1 downTo 0) {
                if (player.getMediaItemAt(index).mediaId.toLongOrNull() == mediaId) {
                    player.removeMediaItem(index)
                }
            }
        }
    }

    fun removeQueueItem(index: Int) {
        controller?.let { player ->
            if (index in 0 until player.mediaItemCount) player.removeMediaItem(index)
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        controller?.let { player ->
            if (
                fromIndex in 0 until player.mediaItemCount &&
                toIndex in 0 until player.mediaItemCount
            ) {
                player.moveMediaItem(fromIndex, toIndex)
            }
        }
    }

    fun clearUpcomingQueue() {
        controller?.let { player ->
            val currentIndex = player.currentMediaItemIndex
            if (currentIndex >= 0 && currentIndex < player.mediaItemCount - 1) {
                player.removeMediaItems(currentIndex + 1, player.mediaItemCount)
            }
        }
    }

    fun refreshSleepTimer() {
        val mediaController = controller ?: return
        val future = mediaController.sendCustomCommand(
            SleepTimerProtocol.GetStateCommand,
            Bundle.EMPTY,
        )
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess(::applySleepTimerResult)
            },
            mainExecutor,
        )
    }

    fun startSleepTimer(minutes: Int) {
        val mediaController = controller ?: return
        val future = mediaController.sendCustomCommand(
            SleepTimerProtocol.StartCommand,
            Bundle().apply {
                putInt(SleepTimerProtocol.KeyMinutes, minutes)
            },
        )
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess(::applySleepTimerResult)
            },
            mainExecutor,
        )
    }

    fun cancelSleepTimer() {
        val mediaController = controller ?: return
        val future = mediaController.sendCustomCommand(
            SleepTimerProtocol.CancelCommand,
            Bundle.EMPTY,
        )
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess(::applySleepTimerResult)
            },
            mainExecutor,
        )
    }

    fun refreshAudioEffects() {
        val mediaController = controller ?: return
        val future = mediaController.sendCustomCommand(
            AudioEffectProtocol.GetStateCommand,
            Bundle.EMPTY,
        )
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess(::applyAudioEffectResult)
            },
            mainExecutor,
        )
    }

    fun applySoundProfile(profile: MuseSoundProfile) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putBoolean(AudioEffectProtocol.KeyMasterEnabled, true)
                putBoolean(AudioEffectProtocol.KeyBypass, false)
                putIntArray(
                    AudioEffectProtocol.KeyEqProfileCentersHz,
                    profile.eqCentersHz.toIntArray(),
                )
                putIntArray(
                    AudioEffectProtocol.KeyEqProfileLevelsMb,
                    profile.eqLevelsMb.toIntArray(),
                )
                putBoolean(
                    AudioEffectProtocol.KeyBassEnabled,
                    profile.bassEnabled,
                )
                putInt(
                    AudioEffectProtocol.KeyBassStrength,
                    profile.bassStrength,
                )
                putBoolean(
                    AudioEffectProtocol.KeyVirtualizerEnabled,
                    profile.virtualizerEnabled,
                )
                putInt(
                    AudioEffectProtocol.KeyVirtualizerStrength,
                    profile.virtualizerStrength,
                )
                putBoolean(
                    AudioEffectProtocol.KeyLoudnessEnabled,
                    profile.loudnessEnabled,
                )
                putInt(
                    AudioEffectProtocol.KeyLoudnessGainMb,
                    profile.loudnessGainMb,
                )
            }
        )
    }

    fun setAudioEffectsEnabled(enabled: Boolean) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putBoolean(AudioEffectProtocol.KeyMasterEnabled, enabled)
            }
        )
    }

    fun setAudioBypass(bypass: Boolean) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putBoolean(AudioEffectProtocol.KeyBypass, bypass)
            }
        )
    }

    fun setEqualizerBand(
        index: Int,
        levelMb: Int,
    ) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putInt(AudioEffectProtocol.KeyEqBandIndex, index)
                putInt(AudioEffectProtocol.KeyEqBandLevelMb, levelMb)
            }
        )
    }

    fun useEqualizerPreset(index: Int) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putInt(AudioEffectProtocol.KeyEqPresetIndex, index)
            }
        )
    }

    fun setBassEnabled(enabled: Boolean) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putBoolean(AudioEffectProtocol.KeyBassEnabled, enabled)
            }
        )
    }

    fun setBassStrength(strength: Int) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putInt(AudioEffectProtocol.KeyBassStrength, strength)
            }
        )
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putBoolean(AudioEffectProtocol.KeyVirtualizerEnabled, enabled)
            }
        )
    }

    fun setVirtualizerStrength(strength: Int) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putInt(AudioEffectProtocol.KeyVirtualizerStrength, strength)
            }
        )
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putBoolean(AudioEffectProtocol.KeyLoudnessEnabled, enabled)
            }
        )
    }

    fun setLoudnessGainMb(gainMb: Int) {
        sendAudioEffectUpdate(
            Bundle().apply {
                putInt(AudioEffectProtocol.KeyLoudnessGainMb, gainMb)
            }
        )
    }

    fun release() {
        tickerJob?.cancel()
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        scope.coroutineContext[Job]?.cancel()
    }

    private fun sendAudioEffectUpdate(args: Bundle) {
        val mediaController = controller ?: return
        val future = mediaController.sendCustomCommand(
            AudioEffectProtocol.UpdateCommand,
            args,
        )
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess(::applyAudioEffectResult)
            },
            mainExecutor,
        )
    }

    private fun applySleepTimerResult(result: SessionResult) {
        if (result.resultCode != SessionResult.RESULT_SUCCESS) return
        val extras = result.extras
        _sleepTimer.value = SleepTimerUiState(
            connected = true,
            active = extras.getBoolean(SleepTimerProtocol.KeyActive),
            remainingMs = extras.getLong(SleepTimerProtocol.KeyRemainingMs),
            deadlineWallMs = extras.getLong(SleepTimerProtocol.KeyDeadlineWallMs),
        )
    }

    private fun syncSleepTimerCountdown() {
        val state = _sleepTimer.value
        if (!state.active || state.deadlineWallMs <= 0L) return

        val remaining = (state.deadlineWallMs - System.currentTimeMillis())
            .coerceAtLeast(0L)

        _sleepTimer.value = state.copy(
            active = remaining > 0L,
            remainingMs = remaining,
            deadlineWallMs = if (remaining > 0L) state.deadlineWallMs else 0L,
        )
    }

    private fun applyAudioEffectResult(result: SessionResult) {
        if (result.resultCode != SessionResult.RESULT_SUCCESS) return
        val extras = result.extras

        val centers = extras
            .getIntArray(AudioEffectProtocol.KeyEqCentersHz)
            ?.toList()
            .orEmpty()
        val levels = extras
            .getIntArray(AudioEffectProtocol.KeyEqLevelsMb)
            ?.toList()
            .orEmpty()
        val presetNames = extras
            .getStringArrayList(AudioEffectProtocol.KeyEqPresetNames)
            ?.toList()
            .orEmpty()

        _audioEffects.value = AudioEffectsUiState(
            connected = true,
            sessionReady = extras.getBoolean(AudioEffectProtocol.KeySessionReady),
            masterEnabled = extras.getBoolean(
                AudioEffectProtocol.KeyMasterEnabled,
                true,
            ),
            bypass = extras.getBoolean(AudioEffectProtocol.KeyBypass),
            equalizerAvailable = extras.getBoolean(
                AudioEffectProtocol.KeyEqAvailable,
            ),
            bandCentersHz = centers,
            bandMinMb = extras.getInt(AudioEffectProtocol.KeyEqMinMb),
            bandMaxMb = extras.getInt(AudioEffectProtocol.KeyEqMaxMb),
            bandLevelsMb = levels,
            presetNames = presetNames,
            bassAvailable = extras.getBoolean(
                AudioEffectProtocol.KeyBassAvailable,
            ),
            bassEnabled = extras.getBoolean(
                AudioEffectProtocol.KeyBassEnabled,
            ),
            bassStrength = extras.getInt(
                AudioEffectProtocol.KeyBassStrength,
            ),
            virtualizerAvailable = extras.getBoolean(
                AudioEffectProtocol.KeyVirtualizerAvailable,
            ),
            virtualizerEnabled = extras.getBoolean(
                AudioEffectProtocol.KeyVirtualizerEnabled,
            ),
            virtualizerStrength = extras.getInt(
                AudioEffectProtocol.KeyVirtualizerStrength,
            ),
            loudnessAvailable = extras.getBoolean(
                AudioEffectProtocol.KeyLoudnessAvailable,
            ),
            loudnessEnabled = extras.getBoolean(
                AudioEffectProtocol.KeyLoudnessEnabled,
            ),
            loudnessGainMb = extras.getInt(
                AudioEffectProtocol.KeyLoudnessGainMb,
            ),
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                controller?.let(::syncFrom)
                syncSleepTimerCountdown()
                delay(500)
            }
        }
    }

    private fun syncFrom(player: Player) {
        val metadata = player.mediaMetadata
        val queue = buildList {
            repeat(player.mediaItemCount) { index ->
                val item = player.getMediaItemAt(index)
                add(
                    QueueUiItem(
                        mediaId = item.mediaId.toLongOrNull(),
                        title = item.mediaMetadata.title?.toString().orEmpty(),
                        artist = item.mediaMetadata.artist?.toString().orEmpty(),
                    )
                )
            }
        }
        _state.value = PlaybackUiState(
            connected = true,
            currentMediaId = player.currentMediaItem?.mediaId?.toLongOrNull(),
            currentIndex = player.currentMediaItemIndex,
            title = metadata.title?.toString().orEmpty(),
            artist = metadata.artist?.toString().orEmpty(),
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0L } ?: 0L,
            shuffleEnabled = player.shuffleModeEnabled,
            repeatMode = player.repeatMode,
            queue = queue,
            errorMessage = _state.value.errorMessage,
        )
    }

    private fun toMediaItem(track: Track): MediaItem =
        MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setExtras(
                        Bundle().apply {
                            putString(SourceUriExtraKey, track.uri.toString())
                        }
                    )
                    .build()
            )
            .build()

    private companion object {
        const val SourceUriExtraKey = "muse.source_uri"
    }
}
