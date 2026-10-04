package com.rezoxnemesis.muse.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.rezoxnemesis.muse.MainActivity

class MusePlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var snapshotStore: PlaybackSnapshotStore
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
    }

    override fun onCreate() {
        super.onCreate()

        snapshotStore = PlaybackSnapshotStore(this)

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

        snapshotStore.restore(player)
        player.addListener(playerListener)

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
            .setSessionActivity(sessionActivity)
            .build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo,
    ): MediaSession? = mediaSession

    override fun onDestroy() {
        handler.removeCallbacks(snapshotTicker)
        mediaSession?.run {
            snapshotStore.save(player)
            player.removeListener(playerListener)
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
