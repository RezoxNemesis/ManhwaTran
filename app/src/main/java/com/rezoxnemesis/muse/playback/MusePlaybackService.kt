@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.rezoxnemesis.muse.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.rezoxnemesis.muse.MainActivity
import com.rezoxnemesis.muse.widget.MuseWidgetUpdater

class MusePlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var snapshotStore: PlaybackSnapshotStore
    private lateinit var audioEffects: AudioEffectEngine
    private lateinit var sleepTimer: SleepTimerEngine
    private val handler = Handler(Looper.getMainLooper())

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
                events.contains(Player.EVENT_POSITION_DISCONTINUITY)
            ) {
                snapshotStore.save(player)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            handler.removeCallbacks(snapshotTicker)
            mediaSession?.player?.let(snapshotStore::save)
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
            if (sleepTimer.onMediaItemTransition()) {
                mediaSession?.player?.let { player ->
                    player.pause()
                    snapshotStore.save(player)
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
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
        audioEffects = AudioEffectEngine(this)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(this)
            .build()
            .apply {
                setAudioAttributes(audioAttributes, true)
                setHandleAudioBecomingNoisy(true)
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
        super.onDestroy()
    }

    private companion object {
        const val SnapshotIntervalMs = 5_000L
    }
}
