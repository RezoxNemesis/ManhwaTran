package com.rezoxnemesis.muse.ui

import androidx.compose.ui.graphics.Color
import com.rezoxnemesis.muse.playback.MuseAudioSpectrum as PlaybackMuseAudioSpectrum

internal typealias MuseAudioSpectrum = PlaybackMuseAudioSpectrum

internal enum class MuseWorldMaterialKind {
    WetRainforest,
    IridescentGlass,
    CharredCopper,
    LunarSilver,
    TidalTeal,
    VelvetRose,
}

internal enum class MuseWorldMotionKind {
    RainWeightedSway,
    RibbonDrift,
    ThermalLift,
    LunarFloat,
    TidalPulse,
    PetalOrbit,
}

internal enum class MuseWorldFoliageKind {
    WetBroadleaf,
    PrismBlade,
    CharredShard,
    MoonLance,
    TidalFrond,
    VelvetPetal,
}

internal enum class MuseWorldChromeKind {
    DewGlass,
    PrismFacet,
    ForgedEmber,
    LunarHalo,
    TidalLens,
    RoseVelvet,
}

internal data class MuseLivingWorldStyle(
    val materialKind: MuseWorldMaterialKind,
    val motionKind: MuseWorldMotionKind,
    val foliageKind: MuseWorldFoliageKind,
    val chromeKind: MuseWorldChromeKind,
    val greyscaleSignature: Float,
    val surfaceRoughness: Float,
    val subsurfaceLight: Float,
    val waterAdhesion: Float,
    val sparkDensity: Float,
    val volumetricMist: Float,
    val refractionWarp: Float,
    val petalDepth: Float,
    val foliageHueBias: Float,
    val edgeIrregularity: Float,
    val leafHighlight: Color,
    val leafPrimary: Color,
    val leafSecondary: Color,
    val leafShadow: Color,
    val vein: Color,
    val subsurfaceTint: Color,
    val specularTint: Color,
)

internal data class MuseWorldAudioResponse(
    val dropletImpulse: Float,
    val shimmer: Float,
    val particleImpulse: Float,
    val refractionImpulse: Float,
    val calmFactor: Float,
)

internal fun MuseVisualProfile.livingWorldStyle(): MuseLivingWorldStyle =
    when (this) {
        MuseVisualProfile.VerdantRain -> MuseLivingWorldStyle(
            materialKind = MuseWorldMaterialKind.WetRainforest,
            motionKind = MuseWorldMotionKind.RainWeightedSway,
            foliageKind = MuseWorldFoliageKind.WetBroadleaf,
            chromeKind = MuseWorldChromeKind.DewGlass,
            greyscaleSignature = 0.93f,
            surfaceRoughness = 0.72f,
            subsurfaceLight = 0.82f,
            waterAdhesion = 1.00f,
            sparkDensity = 0.08f,
            volumetricMist = 0.46f,
            refractionWarp = 0.18f,
            petalDepth = 0.04f,
            foliageHueBias = 0.26f,
            edgeIrregularity = 0.78f,
            leafHighlight = Color(0xFFC7F49A),
            leafPrimary = Color(0xFF5EAF4C),
            leafSecondary = Color(0xFF236733),
            leafShadow = Color(0xFF07180D),
            vein = Color(0xFFA9D982),
            subsurfaceTint = Color(0xFF78CB55),
            specularTint = Color(0xFFE9FFDB),
        )
        MuseVisualProfile.AuroraGlass -> MuseLivingWorldStyle(
            materialKind = MuseWorldMaterialKind.IridescentGlass,
            motionKind = MuseWorldMotionKind.RibbonDrift,
            foliageKind = MuseWorldFoliageKind.PrismBlade,
            chromeKind = MuseWorldChromeKind.PrismFacet,
            greyscaleSignature = 0.91f,
            surfaceRoughness = 0.22f,
            subsurfaceLight = 0.94f,
            waterAdhesion = 0.68f,
            sparkDensity = 0.24f,
            volumetricMist = 0.62f,
            refractionWarp = 0.56f,
            petalDepth = 0.08f,
            foliageHueBias = 0.48f,
            edgeIrregularity = 0.34f,
            leafHighlight = Color(0xFFD9FFF5),
            leafPrimary = Color(0xFF6BC9B9),
            leafSecondary = Color(0xFF2C7580),
            leafShadow = Color(0xFF07191F),
            vein = Color(0xFFB8E8E2),
            subsurfaceTint = Color(0xFF6CE0D1),
            specularTint = Color(0xFFF2FFFF),
        )
        MuseVisualProfile.MidnightEmber -> MuseLivingWorldStyle(
            materialKind = MuseWorldMaterialKind.CharredCopper,
            motionKind = MuseWorldMotionKind.ThermalLift,
            foliageKind = MuseWorldFoliageKind.CharredShard,
            chromeKind = MuseWorldChromeKind.ForgedEmber,
            greyscaleSignature = 0.98f,
            surfaceRoughness = 0.88f,
            subsurfaceLight = 0.34f,
            waterAdhesion = 0.32f,
            sparkDensity = 1.00f,
            volumetricMist = 0.18f,
            refractionWarp = 0.12f,
            petalDepth = 0.02f,
            foliageHueBias = 0.72f,
            edgeIrregularity = 0.92f,
            leafHighlight = Color(0xFFFFBE82),
            leafPrimary = Color(0xFF9C5035),
            leafSecondary = Color(0xFF4A241D),
            leafShadow = Color(0xFF0F0907),
            vein = Color(0xFFDC7A4A),
            subsurfaceTint = Color(0xFFA3452D),
            specularTint = Color(0xFFFFD1A5),
        )
        MuseVisualProfile.MoonlitViolet -> MuseLivingWorldStyle(
            materialKind = MuseWorldMaterialKind.LunarSilver,
            motionKind = MuseWorldMotionKind.LunarFloat,
            foliageKind = MuseWorldFoliageKind.MoonLance,
            chromeKind = MuseWorldChromeKind.LunarHalo,
            greyscaleSignature = 0.88f,
            surfaceRoughness = 0.48f,
            subsurfaceLight = 0.70f,
            waterAdhesion = 0.58f,
            sparkDensity = 0.05f,
            volumetricMist = 1.00f,
            refractionWarp = 0.24f,
            petalDepth = 0.10f,
            foliageHueBias = 0.88f,
            edgeIrregularity = 0.42f,
            leafHighlight = Color(0xFFE8DEFF),
            leafPrimary = Color(0xFF8E80A9),
            leafSecondary = Color(0xFF47405D),
            leafShadow = Color(0xFF100D19),
            vein = Color(0xFFC9B8E8),
            subsurfaceTint = Color(0xFF8777B0),
            specularTint = Color(0xFFF7F1FF),
        )
        MuseVisualProfile.OceanPulse -> MuseLivingWorldStyle(
            materialKind = MuseWorldMaterialKind.TidalTeal,
            motionKind = MuseWorldMotionKind.TidalPulse,
            foliageKind = MuseWorldFoliageKind.TidalFrond,
            chromeKind = MuseWorldChromeKind.TidalLens,
            greyscaleSignature = 1.00f,
            surfaceRoughness = 0.28f,
            subsurfaceLight = 0.90f,
            waterAdhesion = 0.76f,
            sparkDensity = 0.12f,
            volumetricMist = 0.54f,
            refractionWarp = 1.00f,
            petalDepth = 0.03f,
            foliageHueBias = 0.58f,
            edgeIrregularity = 0.38f,
            leafHighlight = Color(0xFFC7FBFF),
            leafPrimary = Color(0xFF42AEB3),
            leafSecondary = Color(0xFF155B65),
            leafShadow = Color(0xFF03151B),
            vein = Color(0xFF86DCE1),
            subsurfaceTint = Color(0xFF31C3C4),
            specularTint = Color(0xFFE2FFFF),
        )
        MuseVisualProfile.RoseNoir -> MuseLivingWorldStyle(
            materialKind = MuseWorldMaterialKind.VelvetRose,
            motionKind = MuseWorldMotionKind.PetalOrbit,
            foliageKind = MuseWorldFoliageKind.VelvetPetal,
            chromeKind = MuseWorldChromeKind.RoseVelvet,
            greyscaleSignature = 0.90f,
            surfaceRoughness = 0.62f,
            subsurfaceLight = 0.58f,
            waterAdhesion = 0.64f,
            sparkDensity = 0.10f,
            volumetricMist = 0.42f,
            refractionWarp = 0.20f,
            petalDepth = 1.00f,
            foliageHueBias = 0.96f,
            edgeIrregularity = 0.66f,
            leafHighlight = Color(0xFFFFD3E1),
            leafPrimary = Color(0xFF9D526F),
            leafSecondary = Color(0xFF572539),
            leafShadow = Color(0xFF160911),
            vein = Color(0xFFD98AA6),
            subsurfaceTint = Color(0xFFB64D76),
            specularTint = Color(0xFFFFE9F0),
        )
    }

internal fun resolveWorldAudioResponse(
    profile: MuseVisualProfile,
    spectrum: MuseAudioSpectrum,
): MuseWorldAudioResponse {
    val e = spectrum.energy.coerceIn(0f, 1f)
    val bass = spectrum.bass.coerceIn(0f, 1f)
    val mid = spectrum.mid.coerceIn(0f, 1f)
    val high = spectrum.high.coerceIn(0f, 1f)
    val transient = spectrum.transient.coerceIn(0f, 1f)

    val response = when (profile) {
        MuseVisualProfile.VerdantRain -> MuseWorldAudioResponse(
            dropletImpulse = bass * 0.58f + transient * 0.42f,
            shimmer = high * 0.24f + mid * 0.10f,
            particleImpulse = transient * 0.18f,
            refractionImpulse = bass * 0.08f + mid * 0.05f,
            calmFactor = 0.58f + (1f - transient) * 0.24f,
        )
        MuseVisualProfile.AuroraGlass -> MuseWorldAudioResponse(
            dropletImpulse = bass * 0.18f + transient * 0.16f,
            shimmer = high * 0.68f + mid * 0.24f + transient * 0.08f,
            particleImpulse = high * 0.18f,
            refractionImpulse = mid * 0.24f + high * 0.22f,
            calmFactor = 0.48f + (1f - transient) * 0.20f,
        )
        MuseVisualProfile.MidnightEmber -> MuseWorldAudioResponse(
            dropletImpulse = bass * 0.12f,
            shimmer = high * 0.15f + mid * 0.10f,
            particleImpulse = transient * 0.60f + bass * 0.30f + e * 0.10f,
            refractionImpulse = transient * 0.06f,
            calmFactor = 0.30f + (1f - transient) * 0.20f,
        )
        MuseVisualProfile.MoonlitViolet -> MuseWorldAudioResponse(
            dropletImpulse = bass * 0.25f + transient * 0.15f,
            shimmer = high * 0.18f + mid * 0.08f,
            particleImpulse = transient * 0.10f + mid * 0.08f,
            refractionImpulse = mid * 0.10f,
            calmFactor = 1f - transient * 0.45f - bass * 0.20f + (1f - e) * 0.25f,
        )
        MuseVisualProfile.OceanPulse -> MuseWorldAudioResponse(
            dropletImpulse = bass * 0.34f + transient * 0.28f,
            shimmer = high * 0.36f + mid * 0.18f,
            particleImpulse = transient * 0.22f,
            refractionImpulse = bass * 0.55f + transient * 0.35f + mid * 0.10f,
            calmFactor = 1f - transient * 0.75f - bass * 0.25f,
        )
        MuseVisualProfile.RoseNoir -> MuseWorldAudioResponse(
            dropletImpulse = bass * 0.22f + transient * 0.16f,
            shimmer = high * 0.30f + mid * 0.20f,
            particleImpulse = mid * 0.34f + transient * 0.22f,
            refractionImpulse = bass * 0.12f,
            calmFactor = 0.54f + (1f - transient) * 0.24f,
        )
    }

    return response.copy(
        dropletImpulse = response.dropletImpulse.coerceIn(0f, 1f),
        shimmer = response.shimmer.coerceIn(0f, 1f),
        particleImpulse = response.particleImpulse.coerceIn(0f, 1f),
        refractionImpulse = response.refractionImpulse.coerceIn(0f, 1f),
        calmFactor = response.calmFactor.coerceIn(0f, 1f),
    )
}
