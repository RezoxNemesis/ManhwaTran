package com.rezoxnemesis.muse.playback

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SleepTimerEngine(
    context: Context,
    private val onExpired: () -> Unit,
) {
    private val preferences = context.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )
    private val handler = Handler(Looper.getMainLooper())

    private val expireRunnable = Runnable {
        val deadline = deadlineWallMs()
        if (deadline > 0L && deadline <= System.currentTimeMillis()) {
            clearDeadline()
            onExpired()
        } else {
            schedule()
        }
    }

    init {
        restore()
    }

    fun start(minutes: Int): Bundle {
        val safeMinutes = minutes.coerceIn(MinMinutes, MaxMinutes)
        val deadline = System.currentTimeMillis() + safeMinutes * 60_000L
        preferences.edit()
            .putLong(KeyDeadlineWallMsPref, deadline)
            .apply()
        schedule()
        return snapshot()
    }

    fun cancel(): Bundle {
        handler.removeCallbacks(expireRunnable)
        clearDeadline()
        return snapshot()
    }

    fun snapshot(): Bundle {
        val now = System.currentTimeMillis()
        val deadline = deadlineWallMs()
        val remaining = (deadline - now).coerceAtLeast(0L)
        val active = deadline > now

        if (!active && deadline > 0L) {
            clearDeadline()
        }

        return Bundle().apply {
            putBoolean(SleepTimerProtocol.KeyActive, active)
            putLong(SleepTimerProtocol.KeyRemainingMs, remaining)
            putLong(
                SleepTimerProtocol.KeyDeadlineWallMs,
                if (active) deadline else 0L,
            )
        }
    }

    fun release() {
        handler.removeCallbacks(expireRunnable)
    }

    private fun restore() {
        val deadline = deadlineWallMs()
        if (deadline <= 0L) return

        if (deadline <= System.currentTimeMillis()) {
            clearDeadline()
            return
        }

        schedule()
    }

    private fun schedule() {
        handler.removeCallbacks(expireRunnable)
        val remaining = deadlineWallMs() - System.currentTimeMillis()
        if (remaining <= 0L) {
            handler.post(expireRunnable)
        } else {
            handler.postDelayed(expireRunnable, remaining)
        }
    }

    private fun deadlineWallMs(): Long =
        preferences.getLong(KeyDeadlineWallMsPref, 0L)

    private fun clearDeadline() {
        preferences.edit().remove(KeyDeadlineWallMsPref).apply()
    }

    private companion object {
        const val PreferencesName = "muse_sleep_timer"
        const val KeyDeadlineWallMsPref = "deadline_wall_ms"
        const val MinMinutes = 1
        const val MaxMinutes = 12 * 60
    }
}
