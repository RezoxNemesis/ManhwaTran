package com.rezoxnemesis.muse.playback

import android.os.Bundle
import androidx.media3.session.SessionCommand

object SleepTimerProtocol {
    const val ActionGetState = "com.rezoxnemesis.muse.sleep.GET_STATE"
    const val ActionStart = "com.rezoxnemesis.muse.sleep.START"
    const val ActionCancel = "com.rezoxnemesis.muse.sleep.CANCEL"

    val GetStateCommand = SessionCommand(ActionGetState, Bundle.EMPTY)
    val StartCommand = SessionCommand(ActionStart, Bundle.EMPTY)
    val CancelCommand = SessionCommand(ActionCancel, Bundle.EMPTY)

    const val KeyActive = "active"
    const val KeyRemainingMs = "remaining_ms"
    const val KeyDeadlineWallMs = "deadline_wall_ms"
    const val KeyMinutes = "minutes"
    const val KeyMode = "mode"

    const val ModeDuration = "duration"
    const val ModeAfterCurrent = "after_current"
    const val ModeEndOfQueue = "end_of_queue"
}
