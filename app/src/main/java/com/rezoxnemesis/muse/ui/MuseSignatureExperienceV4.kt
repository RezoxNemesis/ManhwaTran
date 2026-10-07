package com.rezoxnemesis.muse.ui

internal data class MuseV4RenderPolicy(
    val waterLanguage: MuseWaterLanguage,
    val physicalDropletScale: Float,
    val waterRefractionScale: Float,
    val condensationHazeScale: Float,
    val secondaryParticleScale: Float,
    val bokehScale: Float,
    val microDetailScale: Float,
    val secondaryShimmerScale: Float,
    val heroLightingEnabled: Boolean,
    val heroGeometryEnabled: Boolean,
    val interactionEnabled: Boolean,
    val morphEnabled: Boolean,
    val echoKind: MuseWorldEchoKind,
    val memoryKind: MuseMusicMemoryKind,
)

internal fun resolveMuseV4RenderPolicy(
    experience: MuseWorldExperience,
    budget: MuseVisualQualityBudget,
    isForeground: Boolean,
): MuseV4RenderPolicy {
    val visibilityScale = if (isForeground) 1f else 0.72f
    val language = experience.interactionLanguage()

    val water = when (experience.waterLanguage) {
        MuseWaterLanguage.PhysicalDroplets -> Triple(1.00f, 0.34f, 0.12f)
        MuseWaterLanguage.CondensationRefraction -> Triple(0f, 0.60f, 0.28f)
        MuseWaterLanguage.DryHeat -> Triple(0f, 0f, 0f)
        MuseWaterLanguage.LunarCondensation -> Triple(0f, 0.10f, 0.82f)
        MuseWaterLanguage.TidalRefraction -> Triple(0f, 1.00f, 0.18f)
        MuseWaterLanguage.RestrainedDew -> Triple(0.16f, 0.12f, 0.10f)
    }

    return MuseV4RenderPolicy(
        waterLanguage = experience.waterLanguage,
        physicalDropletScale = water.first,
        waterRefractionScale = water.second,
        condensationHazeScale = water.third,
        secondaryParticleScale = (
            budget.secondaryParticleScale * visibilityScale
            ).coerceIn(0f, 1f),
        bokehScale = (budget.bokehScale * visibilityScale).coerceIn(0f, 1f),
        microDetailScale = (budget.microDetailScale * visibilityScale).coerceIn(0f, 1f),
        secondaryShimmerScale = (
            budget.secondaryShimmerScale * visibilityScale
            ).coerceIn(0f, 1f),
        heroLightingEnabled = budget.heroLightingEnabled,
        heroGeometryEnabled = budget.heroGeometryEnabled,
        interactionEnabled = budget.interactionEnabled,
        morphEnabled = budget.morphEnabled,
        echoKind = language.echoKind,
        memoryKind = language.memoryKind,
    )
}
