package com.rezoxnemesis.muse.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.rezoxnemesis.muse.MainActivity
import com.rezoxnemesis.muse.data.MusePreferences
import com.rezoxnemesis.muse.widget.MuseWidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MusePlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var snapshotStore: PlaybackSnapshotStore
    private lateinit var audioEffects: AudioEffectEngine
    private lateinit var softwareAudioProcessor: MuseSoftwareAudioProcessor
    private lateinit var sleepTimer: SleepTimerEngine
    private lateinit var preferences: MusePreferences
    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO,
    )
    private var lastRecordedTrackId: Long? = null

    private val snapshotTicker = object : Runnable {
        override fun run() {
            mediaSession?.player?.let(snapshotStore::save)
            if (mediaSession?.player?.isPlaying == true) {
                handler.postDelayed(this, SnapshotIntervalMs)
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            MuseWidgetUpdater.updateAll(this@MusePlaybackService, player)
            if (
                events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) ||
                events.contains(Player.EVENT_TIMELINE_CHANGED) ||
                events.contains(Player.EVENT_REPEAT_MODE_CHANGED) ||
                events.contains(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED) ||
                events.contains(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED) ||
                events.contains(Player.EVENT_POSITION_DISCONTINUITY)
            ) {
                snapshotStore.save(player)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            handler.removeCallbacks(snapshotTicker)
            mediaSession?.player?.let { player ->
                snapshotStore.save(player)
                if (isPlaying) {
                    recordCurrentTrackIfNeeded(player)
                }
            }
            if (isPlaying) {
                handler.postDelayed(snapshotTicker, SnapshotIntervalMs)
            }
        }

        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            audioEffects.attach(audioSessionId)
        }

        override fun onMediaItemTransition(
            mediaItem: androidx.media3.common.MediaItem?,
            reason: Int,
        ) {
            lastRecordedTrackId = null
            mediaSession?.player
                ?.takeIf { it.isPlaying }
                ?.let(::recordCurrentTrackIfNeeded)

            val completedNaturally =
                reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                    reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
            if (
                sleepTimer.onMediaItemTransition(
                    completedNaturally = completedNaturally,
                )
            ) {
                mediaSession?.player?.let { player ->
                    player.pause()
                    snapshotStore.save(player)
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                lastRecordedTrackId = null
            }
            if (
                playbackState == Player.STATE_ENDED &&
                sleepTimer.onPlaybackEnded()
            ) {
                mediaSession?.player?.let { player ->
                    player.pause()
                    snapshotStore.save(player)
                }
            }
        }
    }

    private val sessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val defaultResult = super.onConnect(session, controller)
            if (!defaultResult.isAccepted) return defaultResult

            val commandsBuilder = defaultResult
                .availableSessionCommands
                .buildUpon()

            if (
                controller.packageName ==
                this@MusePlaybackService.packageName
            ) {
                commandsBuilder
                    .add(AudioEffectProtocol.GetStateCommand)
                    .add(AudioEffectProtocol.UpdateCommand)
                    .add(SleepTimerProtocol.GetStateCommand)
                    .add(SleepTimerProtocol.StartCommand)
                    .add(SleepTimerProtocol.CancelCommand)
            }

            return MediaSession.ConnectionResult.accept(
                commandsBuilder.build(),
                defaultResult.availablePlayerCommands,
            )
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            command: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            if (
                controller.packageName !=
                this@MusePlaybackService.packageName
            ) {
                return super.onCustomCommand(
                    session,
                    controller,
                    command,
                    args,
                )
            }

            return when (command.customAction) {
                AudioEffectProtocol.ActionGetState -> {
                    Futures.immediateFuture(
                        SessionResult(
                            SessionResult.RESULT_SUCCESS,
                            audioEffects.snapshot(),
                        )
                    )
                }

                AudioEffectProtocol.ActionUpdate -> {
                    Futures.immediateFuture(
                        SessionResult(
                            SessionResult.RESULT_SUCCESS,
                            audioEffects.update(args),
                        )
                    )
                }

                SleepTimerProtocol.ActionGetState -> {
                    Futures.immediateFuture(
                        SessionResult(
                            SessionResult.RESULT_SUCCESS,
                            sleepTimer.snapshot(),
                        )
                    )
                }

                SleepTimerProtocol.ActionStart -> {
                    val minutes = args.getInt(SleepTimerProtocol.KeyMinutes, 30)
                    val mode = args.getString(
                        SleepTimerProtocol.KeyMode,
                        SleepTimerProtocol.ModeDuration,
                    )
                    Futures.immediateFuture(
                        SessionResult(
                            SessionResult.RESULT_SUCCESS,
                            sleepTimer.start(mode, minutes),
                        )
                    )
                }

                SleepTimerProtocol.ActionCancel -> {
                    Futures.immediateFuture(
                        SessionResult(
                            SessionResult.RESULT_SUCCESS,
                            sleepTimer.cancel(),
                        )
                    )
                }

                else -> super.onCustomCommand(session, controller, command, args)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        snapshotStore = PlaybackSnapshotStore(this)
        softwareAudioProcessor = MuseSoftwareAudioProcessor()
        audioEffects = AudioEffectEngine(
            context = this,
            softwareProcessor = softwareAudioProcessor,
        )
        preferences = MusePreferences(this)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val renderersFactory = object : DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: android.content.Context,
                enableFloatOutput: Boolean,
                enableAudioOutputPlaybackParameters: Boolean,
            ): AudioSink {
                return DefaultAudioSink.Builder(context)
                    // Muse DSP intentionally operates on PCM16. Keeping float
                    // output disabled guarantees a stable processor input format.
                    .setEnableFloatOutput(false)
                    .setEnableAudioTrackPlaybackParams(
                        enableAudioOutputPlaybackParameters,
                    )
                    .setAudioProcessors(
                        arrayOf(softwareAudioProcessor),
                    )
                    .build()
            }
        }

        val player = ExoPlayer.Builder(
            this,
            renderersFactory,
        )
            .build()
            .apply {
                setAudioAttributes(audioAttributes, true)
                setHandleAudioBecomingNoisy(true)
                setWakeMode(C.WAKE_MODE_LOCAL)
                playWhenReady = false
            }

        sleepTimer = SleepTimerEngine(this) {
            player.pause()
            snapshotStore.save(player)
        }
        snapshotStore.restore(player)
        player.addListener(playerListener)
        audioEffects.attach(player.audioSessionId)

        val sessionIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.ExtraOpenRoute, "nowPlaying")
        }
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            sessionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(sessionCallback)
            .setSessionActivity(sessionActivity)
            .build()

        MuseWidgetUpdater.updateAll(this, player)
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo,
    ): MediaSession? = mediaSession

    override fun onDestroy() {
        handler.removeCallbacks(snapshotTicker)
        mediaSession?.run {
            snapshotStore.save(player)
            player.removeListener(playerListener)
            audioEffects.release()
            sleepTimer.release()
            player.release()
            release()
        }
        mediaSession = null
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun recordCurrentTrackIfNeeded(
        player: Player,
    ) {
        val trackId = player.currentMediaItem
            ?.mediaId
            ?.toLongOrNull()
            ?.takeIf { it != 0L }
            ?: return
        if (trackId == lastRecordedTrackId) return

        lastRecordedTrackId = trackId
        serviceScope.launch {
            preferences.recordPlayed(trackId)
        }
    }

    private companion object {
        const val SnapshotIntervalMs = 5_000L
    }
}
