package com.rezoxnemesis.muse.playback

object PlaybackTuning {
    const val DefaultSpeed = 1.0f
    const val MinSpeed = 0.5f
    const val MaxSpeed = 2.0f

    const val DefaultPitch = 1.0f
    const val MinPitch = 0.75f
    const val MaxPitch = 1.25f

    fun sanitizeSpeed(value: Float): Float =
        value
            .takeIf { it.isFinite() }
            ?.coerceIn(MinSpeed, MaxSpeed)
            ?: DefaultSpeed

    fun sanitizePitch(value: Float): Float =
        value
            .takeIf { it.isFinite() }
            ?.coerceIn(MinPitch, MaxPitch)
            ?: DefaultPitch
}
