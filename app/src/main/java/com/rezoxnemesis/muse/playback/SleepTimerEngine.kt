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
        val mode = mode()
        val deadline = deadlineWallMs()
        if (
            mode == SleepTimerProtocol.ModeDuration &&
            deadline > 0L &&
            deadline <= System.currentTimeMillis()
        ) {
            clearState()
            onExpired()
        } else {
            schedule()
        }
    }

    init {
        restore()
    }

    fun start(
        mode: String,
        minutes: Int,
    ): Bundle {
        return when (mode) {
            SleepTimerProtocol.ModeAfterCurrent -> {
                storeMode(SleepTimerProtocol.ModeAfterCurrent)
                clearDeadline()
                handler.removeCallbacks(expireRunnable)
                snapshot()
            }

            SleepTimerProtocol.ModeEndOfQueue -> {
                storeMode(SleepTimerProtocol.ModeEndOfQueue)
                clearDeadline()
                handler.removeCallbacks(expireRunnable)
                snapshot()
            }

            else -> {
                val safeMinutes = minutes.coerceIn(MinMinutes, MaxMinutes)
                val deadline = System.currentTimeMillis() + safeMinutes * 60_000L
                preferences.edit()
                    .putString(KeyModePref, SleepTimerProtocol.ModeDuration)
                    .putLong(KeyDeadlineWallMsPref, deadline)
                    .apply()
                schedule()
                snapshot()
            }
        }
    }

    fun onMediaItemTransition(): Boolean {
        if (mode() != SleepTimerProtocol.ModeAfterCurrent) return false
        clearState()
        return true
    }

    fun onPlaybackEnded(): Boolean {
        if (mode() != SleepTimerProtocol.ModeEndOfQueue) return false
        clearState()
        return true
    }

    fun cancel(): Bundle {
        handler.removeCallbacks(expireRunnable)
        clearState()
        return snapshot()
    }

    fun snapshot(): Bundle {
        val currentMode = mode()
        val now = System.currentTimeMillis()
        val deadline = deadlineWallMs()

        val active = when (currentMode) {
            SleepTimerProtocol.ModeDuration -> deadline > now
            SleepTimerProtocol.ModeAfterCurrent,
            SleepTimerProtocol.ModeEndOfQueue -> true
            else -> false
        }

        if (
            currentMode == SleepTimerProtocol.ModeDuration &&
            !active &&
            deadline > 0L
        ) {
            clearState()
        }

        val remaining = if (
            currentMode == SleepTimerProtocol.ModeDuration &&
            active
        ) {
            (deadline - now).coerceAtLeast(0L)
        } else {
            0L
        }

        return Bundle().apply {
            putBoolean(SleepTimerProtocol.KeyActive, active)
            putLong(SleepTimerProtocol.KeyRemainingMs, remaining)
            putLong(
                SleepTimerProtocol.KeyDeadlineWallMs,
                if (active && currentMode == SleepTimerProtocol.ModeDuration) {
                    deadline
                } else {
                    0L
                },
            )
            putString(
                SleepTimerProtocol.KeyMode,
                if (active) currentMode else "",
            )
        }
    }

    fun release() {
        handler.removeCallbacks(expireRunnable)
    }

    private fun restore() {
        when (mode()) {
            SleepTimerProtocol.ModeDuration -> {
                val deadline = deadlineWallMs()
                if (deadline <= System.currentTimeMillis()) {
                    clearState()
                } else {
                    schedule()
                }
            }

            SleepTimerProtocol.ModeAfterCurrent,
            SleepTimerProtocol.ModeEndOfQueue -> {
                handler.removeCallbacks(expireRunnable)
            }

            else -> clearState()
        }
    }

    private fun schedule() {
        handler.removeCallbacks(expireRunnable)
        if (mode() != SleepTimerProtocol.ModeDuration) return

        val remaining = deadlineWallMs() - System.currentTimeMillis()
        if (remaining <= 0L) {
            handler.post(expireRunnable)
        } else {
            handler.postDelayed(expireRunnable, remaining)
        }
    }

    private fun mode(): String =
        preferences.getString(KeyModePref, "").orEmpty()

    private fun storeMode(value: String) {
        preferences.edit().putString(KeyModePref, value).apply()
    }

    private fun deadlineWallMs(): Long =
        preferences.getLong(KeyDeadlineWallMsPref, 0L)

    private fun clearDeadline() {
        preferences.edit().remove(KeyDeadlineWallMsPref).apply()
    }

    private fun clearState() {
        handler.removeCallbacks(expireRunnable)
        preferences.edit()
            .remove(KeyModePref)
            .remove(KeyDeadlineWallMsPref)
            .apply()
    }

    private companion object {
        const val PreferencesName = "muse_sleep_timer"
        const val KeyModePref = "mode"
        const val KeyDeadlineWallMsPref = "deadline_wall_ms"
        const val MinMinutes = 1
        const val MaxMinutes = 12 * 60
    }
}
