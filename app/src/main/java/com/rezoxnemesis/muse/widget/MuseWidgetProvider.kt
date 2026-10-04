package com.rezoxnemesis.muse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.rezoxnemesis.muse.playback.MusePlaybackService

class MuseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        MuseWidgetUpdater.updateAll(context)
        connectToPlayback(
            context = context,
            action = null,
            pendingResult = goAsync(),
        )
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)

        val action = intent.action ?: return
        if (
            action != ActionToggle &&
            action != ActionPrevious &&
            action != ActionNext
        ) {
            return
        }

        connectToPlayback(
            context = context,
            action = action,
            pendingResult = goAsync(),
        )
    }

    private fun connectToPlayback(
        context: Context,
        action: String?,
        pendingResult: PendingResult,
    ) {
        val token = SessionToken(
            context,
            ComponentName(
                context,
                MusePlaybackService::class.java,
            ),
        )
        val controllerFuture = MediaController.Builder(
            context,
            token,
        ).buildAsync()

        controllerFuture.addListener(
            {
                try {
                    val controller = controllerFuture.get()
                    when (action) {
                        ActionToggle -> {
                            if (controller.isPlaying) {
                                controller.pause()
                            } else {
                                controller.play()
                            }
                        }
                        ActionPrevious ->
                            controller.seekToPreviousMediaItem()
                        ActionNext ->
                            controller.seekToNextMediaItem()
                    }
                    MuseWidgetUpdater.updateAll(
                        context,
                        controller,
                    )
                    controller.release()
                } catch (_: Exception) {
                    MuseWidgetUpdater.updateAll(context)
                } finally {
                    pendingResult.finish()
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    companion object {
        const val ActionToggle =
            "com.rezoxnemesis.muse.widget.TOGGLE"
        const val ActionPrevious =
            "com.rezoxnemesis.muse.widget.PREVIOUS"
        const val ActionNext =
            "com.rezoxnemesis.muse.widget.NEXT"
    }
}
