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

        updateProvider(
            context = context,
            manager = manager,
            providerClass = MuseWidgetProvider::class.java,
            views = buildStandardViews(context, player),
        )
        updateProvider(
            context = context,
            manager = manager,
            providerClass = MuseCompactWidgetProvider::class.java,
            views = buildCompactViews(context, player),
        )
        updateProvider(
            context = context,
            manager = manager,
            providerClass = MuseLargeWidgetProvider::class.java,
            views = buildLargeViews(context, player),
        )
        updateProvider(
            context = context,
            manager = manager,
            providerClass = MuseActionsWidgetProvider::class.java,
            views = buildActionsViews(context),
        )
    }

    fun buildViews(
        context: Context,
        player: Player?,
    ): RemoteViews = buildStandardViews(context, player)

    private fun updateProvider(
        context: Context,
        manager: AppWidgetManager,
        providerClass: Class<*>,
        views: RemoteViews,
    ) {
        val component = ComponentName(
            context,
            providerClass,
        )
        val ids = manager.getAppWidgetIds(component)
        ids.forEach { id ->
            manager.updateAppWidget(id, views)
        }
    }

    private fun titleFor(
        context: Context,
        player: Player?,
    ): String =
        player
            ?.mediaMetadata
            ?.title
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.app_name)

    private fun artistFor(
        context: Context,
        player: Player?,
    ): String =
        player
            ?.mediaMetadata
            ?.artist
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.widget_idle)

    private fun buildStandardViews(
        context: Context,
        player: Player?,
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            R.layout.muse_widget,
        )
        bindCommonPlayback(
            context = context,
            views = views,
            player = player,
        )
        return views
    }

    private fun buildCompactViews(
        context: Context,
        player: Player?,
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            R.layout.muse_widget_compact,
        )

        views.setTextViewText(
            R.id.widget_title,
            titleFor(context, player),
        )
        views.setTextViewText(
            R.id.widget_artist,
            artistFor(context, player),
        )
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
            R.id.widget_play_pause,
            controlPendingIntent(
                context,
                MuseWidgetProvider.ActionToggle,
                22,
            ),
        )
        return views
    }

    private fun buildLargeViews(
        context: Context,
        player: Player?,
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            R.layout.muse_widget_large,
        )
        bindCommonPlayback(
            context = context,
            views = views,
            player = player,
        )

        val duration = player
            ?.duration
            ?.takeIf { it > 0L }
            ?: 0L
        val position = player
            ?.currentPosition
            ?.coerceAtLeast(0L)
            ?: 0L

        val max = if (duration > Int.MAX_VALUE) {
            Int.MAX_VALUE
        } else {
            duration.toInt().coerceAtLeast(1)
        }
        val progress = if (duration > 0L) {
            (
                position.toDouble() /
                    duration.toDouble() *
                    max.toDouble()
                )
                .toInt()
                .coerceIn(0, max)
        } else {
            0
        }

        views.setProgressBar(
            R.id.widget_progress,
            max,
            progress,
            duration <= 0L,
        )
        return views
    }

    private fun buildActionsViews(
        context: Context,
    ): RemoteViews {
        val views = RemoteViews(
            context.packageName,
            R.layout.muse_widget_actions,
        )
        views.setOnClickPendingIntent(
            R.id.widget_root,
            routePendingIntent(context, "home", 30),
        )
        views.setOnClickPendingIntent(
            R.id.widget_action_library,
            routePendingIntent(context, "library", 31),
        )
        views.setOnClickPendingIntent(
            R.id.widget_action_downloads,
            routePendingIntent(context, "downloads", 32),
        )
        views.setOnClickPendingIntent(
            R.id.widget_action_equalizer,
            routePendingIntent(context, "equalizer", 33),
        )
        views.setOnClickPendingIntent(
            R.id.widget_action_sleep,
            routePendingIntent(context, "sleep", 34),
        )
        return views
    }

    private fun bindCommonPlayback(
        context: Context,
        views: RemoteViews,
        player: Player?,
    ) {
        views.setTextViewText(
            R.id.widget_title,
            titleFor(context, player),
        )
        views.setTextViewText(
            R.id.widget_artist,
            artistFor(context, player),
        )
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
    }

    private fun activityPendingIntent(
        context: Context,
    ): PendingIntent =
        routePendingIntent(
            context = context,
            route = "nowPlaying",
            requestCode = 10,
        )

    private fun routePendingIntent(
        context: Context,
        route: String,
        requestCode: Int,
    ): PendingIntent {
        val intent = Intent(
            context,
            MainActivity::class.java,
        ).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(
                MainActivity.ExtraOpenRoute,
                route,
            )
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
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
