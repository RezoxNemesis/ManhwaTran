package com.rezoxnemesis.muse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.media3.common.Player
import com.rezoxnemesis.muse.MainActivity
import com.rezoxnemesis.muse.R

object MuseWidgetUpdater {
    fun updateAll(
        context: Context,
        player: Player? = null,
    ) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(
            context,
            MuseWidgetProvider::class.java,
        )
        val ids = manager.getAppWidgetIds(component)
        if (ids.isEmpty()) return

        val views = buildViews(context, player)
        ids.forEach { id ->
            manager.updateAppWidget(id, views)
        }
    }

    fun buildViews(
        context: Context,
        player: Player?,
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            R.layout.muse_widget,
        )

        val title = player
            ?.mediaMetadata
            ?.title
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.app_name)

        val artist = player
            ?.mediaMetadata
            ?.artist
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.widget_idle)

        views.setTextViewText(R.id.widget_title, title)
        views.setTextViewText(R.id.widget_artist, artist)
        views.setImageViewResource(
            R.id.widget_play_pause,
            if (player?.isPlaying == true) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            },
        )

        views.setOnClickPendingIntent(
            R.id.widget_root,
            activityPendingIntent(context),
        )
        views.setOnClickPendingIntent(
            R.id.widget_previous,
            controlPendingIntent(
                context,
                MuseWidgetProvider.ActionPrevious,
                11,
            ),
        )
        views.setOnClickPendingIntent(
            R.id.widget_play_pause,
            controlPendingIntent(
                context,
                MuseWidgetProvider.ActionToggle,
                12,
            ),
        )
        views.setOnClickPendingIntent(
            R.id.widget_next,
            controlPendingIntent(
                context,
                MuseWidgetProvider.ActionNext,
                13,
            ),
        )

        return views
    }

    private fun activityPendingIntent(
        context: Context,
    ): PendingIntent {
        val intent = Intent(
            context,
            MainActivity::class.java,
        ).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            10,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun controlPendingIntent(
        context: Context,
        action: String,
        requestCode: Int,
    ): PendingIntent {
        val intent = Intent(
            context,
            MuseWidgetProvider::class.java,
        ).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
