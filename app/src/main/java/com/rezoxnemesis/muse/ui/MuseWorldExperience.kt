package com.rezoxnemesis.muse.ui

/**
 * V4 source-of-truth wrapper for the visual systems that already define one
 * Muse world. Keeping the existing V3 models inside this object lets V4
 * coordinate them without duplicating their values or weakening the V3
 * baseline.
 */
internal data class MuseWorldExperience(
    val profile: MuseVisualProfile,
    val style: MuseLivingWorldStyle,
    val sceneGeometry: MuseWorldSceneGeometry,
    val lightField: MuseWorldLightField,
    val transition: MuseWorldTransitionSignature,
    val atmosphere: MuseAtmosphereBehavior,
    val waterLanguage: MuseWaterLanguage,
) {
    val allowsPhysicalDroplets: Boolean
        get() = waterLanguage == MuseWaterLanguage.PhysicalDroplets

    val allowsWaterRefraction: Boolean
        get() = waterLanguage == MuseWaterLanguage.CondensationRefraction ||
            waterLanguage == MuseWaterLanguage.TidalRefraction
}

internal enum class MuseWaterLanguage {
    PhysicalDroplets,
    CondensationRefraction,
    DryHeat,
    LunarCondensation,
    TidalRefraction,
    RestrainedDew,
}

internal fun MuseVisualProfile.worldExperience(): MuseWorldExperience =
    MuseWorldExperience(
        profile = this,
        style = livingWorldStyle(),
        sceneGeometry = worldSceneGeometry(),
        lightField = worldLightField(),
        transition = transitionSignature(),
        atmosphere = atmosphereBehavior(),
        waterLanguage = when (this) {
            MuseVisualProfile.VerdantRain -> MuseWaterLanguage.PhysicalDroplets
            MuseVisualProfile.AuroraGlass -> MuseWaterLanguage.CondensationRefraction
            MuseVisualProfile.MidnightEmber -> MuseWaterLanguage.DryHeat
            MuseVisualProfile.MoonlitViolet -> MuseWaterLanguage.LunarCondensation
            MuseVisualProfile.OceanPulse -> MuseWaterLanguage.TidalRefraction
            MuseVisualProfile.RoseNoir -> MuseWaterLanguage.RestrainedDew
        },
    )
