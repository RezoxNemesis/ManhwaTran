package com.rezoxnemesis.muse.data

data class MuseListeningSignal(
    val observedSessions: Int = 0,
    val completedSessions: Int = 0,
    val skippedSessions: Int = 0,
    val completionFractionSum: Double = 0.0,
) {
    val averageCompletionRatio: Double
        get() = if (observedSessions <= 0) {
            0.0
        } else {
            (completionFractionSum / observedSessions)
                .coerceIn(0.0, 1.0)
        }

    val skipRatio: Double
        get() = if (observedSessions <= 0) {
            0.0
        } else {
            (skippedSessions.toDouble() / observedSessions)
                .coerceIn(0.0, 1.0)
        }

    fun record(completionFraction: Double): MuseListeningSignal {
        if (!completionFraction.isFinite()) return this

        val normalized = completionFraction.coerceIn(0.0, 1.0)
        return copy(
            observedSessions = observedSessions.safeIncrement(),
            completedSessions = completedSessions.safeIncrementIf(
                normalized >= CompletedThreshold,
            ),
            skippedSessions = skippedSessions.safeIncrementIf(
                normalized <= SkippedThreshold,
            ),
            completionFractionSum = (completionFractionSum + normalized)
                .coerceAtMost(Int.MAX_VALUE.toDouble()),
        )
    }

    private fun Int.safeIncrement(): Int =
        if (this == Int.MAX_VALUE) this else this + 1

    private fun Int.safeIncrementIf(condition: Boolean): Int =
        if (!condition || this == Int.MAX_VALUE) this else this + 1

    companion object {
        const val CompletedThreshold = 0.85
        const val SkippedThreshold = 0.35
    }
}
