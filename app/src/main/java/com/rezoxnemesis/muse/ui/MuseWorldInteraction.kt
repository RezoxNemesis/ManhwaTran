package com.rezoxnemesis.muse.ui

import kotlin.math.abs
import kotlin.math.max

internal enum class MuseWorldEchoKind {
    WetFoliageTrail,
    PrismRibbon,
    EmberTrace,
    MistDisplacement,
    TidalRipple,
    PetalDisplacement,
}

internal enum class MuseMusicMemoryKind {
    WaterAccumulation,
    PrismAfterimage,
    HeatResidue,
    HazeBloom,
    InterferenceRing,
    PetalBloom,
}

internal data class MuseWorldInteractionLanguage(
    val echoKind: MuseWorldEchoKind,
    val memoryKind: MuseMusicMemoryKind,
    val echoLifetimeMillis: Int,
    val memoryDecayPerSecond: Float,
    val idleAmplitude: Float,
    val idleTempoScale: Float,
)

internal fun MuseWorldExperience.interactionLanguage(): MuseWorldInteractionLanguage =
    when (profile) {
        MuseVisualProfile.VerdantRain -> MuseWorldInteractionLanguage(
            echoKind = MuseWorldEchoKind.WetFoliageTrail,
            memoryKind = MuseMusicMemoryKind.WaterAccumulation,
            echoLifetimeMillis = 1_500,
            memoryDecayPerSecond = 0.16f,
            idleAmplitude = 0.18f,
            idleTempoScale = 0.62f,
        )
        MuseVisualProfile.AuroraGlass -> MuseWorldInteractionLanguage(
            echoKind = MuseWorldEchoKind.PrismRibbon,
            memoryKind = MuseMusicMemoryKind.PrismAfterimage,
            echoLifetimeMillis = 1_250,
            memoryDecayPerSecond = 0.20f,
            idleAmplitude = 0.15f,
            idleTempoScale = 0.76f,
        )
        MuseVisualProfile.MidnightEmber -> MuseWorldInteractionLanguage(
            echoKind = MuseWorldEchoKind.EmberTrace,
            memoryKind = MuseMusicMemoryKind.HeatResidue,
            echoLifetimeMillis = 1_050,
            memoryDecayPerSecond = 0.13f,
            idleAmplitude = 0.10f,
            idleTempoScale = 0.54f,
        )
        MuseVisualProfile.MoonlitViolet -> MuseWorldInteractionLanguage(
            echoKind = MuseWorldEchoKind.MistDisplacement,
            memoryKind = MuseMusicMemoryKind.HazeBloom,
            echoLifetimeMillis = 2_100,
            memoryDecayPerSecond = 0.10f,
            idleAmplitude = 0.08f,
            idleTempoScale = 0.42f,
        )
        MuseVisualProfile.OceanPulse -> MuseWorldInteractionLanguage(
            echoKind = MuseWorldEchoKind.TidalRipple,
            memoryKind = MuseMusicMemoryKind.InterferenceRing,
            echoLifetimeMillis = 1_800,
            memoryDecayPerSecond = 0.17f,
            idleAmplitude = 0.16f,
            idleTempoScale = 0.66f,
        )
        MuseVisualProfile.RoseNoir -> MuseWorldInteractionLanguage(
            echoKind = MuseWorldEchoKind.PetalDisplacement,
            memoryKind = MuseMusicMemoryKind.PetalBloom,
            echoLifetimeMillis = 2_350,
            memoryDecayPerSecond = 0.11f,
            idleAmplitude = 0.11f,
            idleTempoScale = 0.48f,
        )
    }

internal data class MuseMusicMemoryState(
    val kind: MuseMusicMemoryKind,
    val strength: Float,
    val decayPerSecond: Float,
)

internal fun emptyMuseMusicMemory(
    experience: MuseWorldExperience,
): MuseMusicMemoryState {
    val language = experience.interactionLanguage()
    return MuseMusicMemoryState(
        kind = language.memoryKind,
        strength = 0f,
        decayPerSecond = language.memoryDecayPerSecond,
    )
}

internal fun accumulateMuseMusicMemory(
    state: MuseMusicMemoryState,
    spectrum: MuseAudioSpectrum,
): MuseMusicMemoryState {
    val impulse = when (state.kind) {
        MuseMusicMemoryKind.WaterAccumulation ->
            spectrum.bass * 0.18f + spectrum.transient * 0.24f + spectrum.energy * 0.10f
        MuseMusicMemoryKind.PrismAfterimage ->
            spectrum.high * 0.24f + spectrum.mid * 0.16f + spectrum.transient * 0.18f
        MuseMusicMemoryKind.HeatResidue ->
            spectrum.bass * 0.22f + spectrum.mid * 0.16f + spectrum.transient * 0.28f
        MuseMusicMemoryKind.HazeBloom ->
            spectrum.mid * 0.17f + spectrum.energy * 0.13f + spectrum.high * 0.08f
        MuseMusicMemoryKind.InterferenceRing ->
            spectrum.bass * 0.18f + spectrum.mid * 0.20f + spectrum.transient * 0.24f
        MuseMusicMemoryKind.PetalBloom ->
            spectrum.mid * 0.20f + spectrum.high * 0.14f + spectrum.energy * 0.12f
    }
    return state.copy(
        strength = (state.strength + impulse.coerceAtLeast(0f)).coerceIn(0f, 1f),
    )
}

internal fun decayMuseMusicMemory(
    state: MuseMusicMemoryState,
    deltaMillis: Int,
): MuseMusicMemoryState {
    val seconds = deltaMillis.coerceAtLeast(0) / 1_000f
    return state.copy(
        strength = (state.strength - state.decayPerSecond * seconds)
            .coerceIn(0f, 1f),
    )
}

internal data class MuseWorldBreathingState(
    val amplitude: Float,
    val tempoScale: Float,
)

internal fun resolveMuseWorldBreathing(
    experience: MuseWorldExperience,
    isPlaying: Boolean,
    audioEnergy: Float,
): MuseWorldBreathingState {
    val language = experience.interactionLanguage()
    val energy = audioEnergy.coerceIn(0f, 1f)
    val playbackLift = if (isPlaying) 1f else 0f

    return MuseWorldBreathingState(
        amplitude = (
            language.idleAmplitude +
                playbackLift * (0.08f + energy * 0.26f)
            ).coerceIn(0.02f, 1f),
        tempoScale = (
            language.idleTempoScale +
                playbackLift * (0.12f + energy * 0.36f)
            ).coerceIn(0.20f, 1.45f),
    )
}

internal enum class MuseFocusRegion {
    Home,
    Explore,
    Library,
    Equalizer,
    MuseLab,
    Navigation,
    NowPlaying,
}

internal data class MuseLivingFocusState(
    val region: MuseFocusRegion?,
    val strength: Float,
)

internal fun focusMuseRegion(
    region: MuseFocusRegion,
): MuseLivingFocusState =
    MuseLivingFocusState(
        region = region,
        strength = 1f,
    )

internal fun decayMuseLivingFocus(
    state: MuseLivingFocusState,
    deltaMillis: Int,
): MuseLivingFocusState {
    val strength = (state.strength - deltaMillis.coerceAtLeast(0) / 1_800f)
        .coerceIn(0f, 1f)
    return MuseLivingFocusState(
        region = if (strength > 0f) state.region else null,
        strength = strength,
    )
}

internal data class MuseGestureMomentum(
    val x: Float,
    val y: Float,
    val magnitude: Float,
)

internal fun resolveMuseGestureMomentum(
    deltaX: Float,
    deltaY: Float,
    velocityX: Float,
    velocityY: Float,
): MuseGestureMomentum {
    val x = (deltaX / 420f + velocityX / 7_500f).coerceIn(-1f, 1f)
    val y = (deltaY / 420f + velocityY / 7_500f).coerceIn(-1f, 1f)
    return MuseGestureMomentum(
        x = x,
        y = y,
        magnitude = max(abs(x), abs(y)).coerceIn(0f, 1f),
    )
}
