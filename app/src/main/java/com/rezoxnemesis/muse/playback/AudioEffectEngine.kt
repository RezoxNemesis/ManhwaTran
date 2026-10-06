package com.rezoxnemesis.muse.playback

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.media.audiofx.BassBoost
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Bundle

class AudioEffectEngine(
    private val context: Context,
    private val softwareProcessor: MuseSoftwareAudioProcessor,
) {
    private val preferences = context.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )

    private var audioSessionId: Int = 0
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var masterEnabled: Boolean
        get() = preferences.getBoolean(KeyMasterEnabledPref, true)
        set(value) = preferences.edit().putBoolean(KeyMasterEnabledPref, value).apply()

    private var bypass: Boolean
        get() = preferences.getBoolean(KeyBypassPref, false)
        set(value) = preferences.edit().putBoolean(KeyBypassPref, value).apply()

    init {
        restoreValues()
        applyEnabledState()
    }

    fun attach(sessionId: Int) {
        if (sessionId <= 0 || sessionId == audioSessionId) return

        releaseEffects()
        audioSessionId = sessionId

        bassBoost = runCatching { BassBoost(0, sessionId) }.getOrNull()
        virtualizer = runCatching { Virtualizer(0, sessionId) }.getOrNull()
        loudnessEnhancer = runCatching { LoudnessEnhancer(sessionId) }.getOrNull()

        restoreValues()
        applyEnabledState()
    }

    fun detach() {
        releaseEffects()
        audioSessionId = 0
    }

    fun release() {
        detach()
    }

    fun update(args: Bundle): Bundle {
        if (args.containsKey(AudioEffectProtocol.KeyMasterEnabled)) {
            masterEnabled = args.getBoolean(AudioEffectProtocol.KeyMasterEnabled)
        }
        if (args.containsKey(AudioEffectProtocol.KeyBypass)) {
            bypass = args.getBoolean(AudioEffectProtocol.KeyBypass)
        }

        if (
            args.containsKey(AudioEffectProtocol.KeyEqBandIndex) &&
            args.containsKey(AudioEffectProtocol.KeyEqBandLevelMb)
        ) {
            setEqualizerBand(
                args.getInt(AudioEffectProtocol.KeyEqBandIndex),
                args.getInt(AudioEffectProtocol.KeyEqBandLevelMb),
            )
        }

        if (args.containsKey(AudioEffectProtocol.KeyEqPresetIndex)) {
            useEqualizerPreset(args.getInt(AudioEffectProtocol.KeyEqPresetIndex))
        }

        val profileCenters = args.getIntArray(
            AudioEffectProtocol.KeyEqProfileCentersHz,
        )
        val profileLevels = args.getIntArray(
            AudioEffectProtocol.KeyEqProfileLevelsMb,
        )
        if (
            profileCenters != null &&
            profileLevels != null &&
            profileCenters.isNotEmpty() &&
            profileCenters.size == profileLevels.size
        ) {
            applyEqualizerProfile(
                savedCentersHz = profileCenters,
                savedLevelsMb = profileLevels,
            )
        }

        if (args.containsKey(AudioEffectProtocol.KeyBassEnabled)) {
            val enabled = args.getBoolean(AudioEffectProtocol.KeyBassEnabled)
            preferences.edit().putBoolean(
                KeyBassEnabledPref,
                enabled,
            ).apply()
            softwareProcessor.setBassEnabled(enabled)
        }
        if (args.containsKey(AudioEffectProtocol.KeyBassStrength)) {
            val value = args.getInt(AudioEffectProtocol.KeyBassStrength)
                .coerceIn(0, MaxBassStrength)
            preferences.edit().putInt(KeyBassStrengthPref, value).apply()
            softwareProcessor.setBassStrength(value)
        }

        if (args.containsKey(AudioEffectProtocol.KeyVirtualizerEnabled)) {
            val enabled = args.getBoolean(
                AudioEffectProtocol.KeyVirtualizerEnabled,
            )
            preferences.edit().putBoolean(
                KeyVirtualizerEnabledPref,
                enabled,
            ).apply()
            softwareProcessor.setVirtualizerEnabled(enabled)
        }
        if (args.containsKey(AudioEffectProtocol.KeyVirtualizerStrength)) {
            val value = args.getInt(AudioEffectProtocol.KeyVirtualizerStrength)
                .coerceIn(0, 1000)
            preferences.edit().putInt(KeyVirtualizerStrengthPref, value).apply()
            softwareProcessor.setVirtualizerStrength(value)
        }

        if (args.containsKey(AudioEffectProtocol.KeyLoudnessEnabled)) {
            val enabled = args.getBoolean(
                AudioEffectProtocol.KeyLoudnessEnabled,
            )
            preferences.edit().putBoolean(
                KeyLoudnessEnabledPref,
                enabled,
            ).apply()
            softwareProcessor.setLoudnessEnabled(enabled)
        }
        if (args.containsKey(AudioEffectProtocol.KeyLoudnessGainMb)) {
            val value = args.getInt(AudioEffectProtocol.KeyLoudnessGainMb)
                .coerceIn(0, MaxLoudnessGainMb)
            preferences.edit().putInt(KeyLoudnessGainPref, value).apply()
            softwareProcessor.setLoudnessGainMb(value)
        }

        if (args.containsKey(AudioEffectProtocol.KeySpatialEnabled)) {
            val enabled = args.getBoolean(AudioEffectProtocol.KeySpatialEnabled)
            preferences.edit().putBoolean(KeySpatialEnabledPref, enabled).apply()
            softwareProcessor.setSpatialEnabled(enabled)
        }
        if (args.containsKey(AudioEffectProtocol.KeySpatialWidth)) {
            val width = args.getInt(AudioEffectProtocol.KeySpatialWidth)
                .coerceIn(0, 1000)
            preferences.edit().putInt(KeySpatialWidthPref, width).apply()
            softwareProcessor.setSpatialWidth(width)
        }

        applyEnabledState()
        return snapshot()
    }

    fun snapshot(): Bundle {
        val centers = MuseSoftwareAudioProcessor.BandCentersHz.copyOf()
        val levels = softwareProcessor.currentLevels()
        val spatial = spatialCapability()
        val softwareSpatialEnabled = softwareProcessor.isSpatialEnabled()
        val compatibilityMode = softwareSpatialEnabled && !spatial.enabled

        return Bundle().apply {
            // The Muse software DSP is available as soon as the service is
            // connected, even before an OEM audio session effect can attach.
            putBoolean(AudioEffectProtocol.KeySessionReady, true)
            putInt(AudioEffectProtocol.KeyAudioSessionId, audioSessionId)
            putBoolean(AudioEffectProtocol.KeyMasterEnabled, masterEnabled)
            putBoolean(AudioEffectProtocol.KeyBypass, bypass)

            putBoolean(AudioEffectProtocol.KeyEqAvailable, true)
            putIntArray(AudioEffectProtocol.KeyEqCentersHz, centers)
            putInt(
                AudioEffectProtocol.KeyEqMinMb,
                MuseSoftwareAudioProcessor.MinLevelMb,
            )
            putInt(
                AudioEffectProtocol.KeyEqMaxMb,
                MuseSoftwareAudioProcessor.MaxLevelMb,
            )
            putIntArray(AudioEffectProtocol.KeyEqLevelsMb, levels)
            putStringArrayList(
                AudioEffectProtocol.KeyEqPresetNames,
                arrayListOf("Flat", "Warm", "Vocal", "Air", "Deep"),
            )

            putBoolean(AudioEffectProtocol.KeyBassAvailable, true)
            putBoolean(
                AudioEffectProtocol.KeyBassEnabled,
                preferences.getBoolean(KeyBassEnabledPref, false),
            )
            putInt(
                AudioEffectProtocol.KeyBassStrength,
                preferences.getInt(KeyBassStrengthPref, DefaultBassStrength),
            )

            putBoolean(
                AudioEffectProtocol.KeyVirtualizerAvailable,
                true,
            )
            putBoolean(
                AudioEffectProtocol.KeyVirtualizerEnabled,
                preferences.getBoolean(KeyVirtualizerEnabledPref, false),
            )
            putInt(
                AudioEffectProtocol.KeyVirtualizerStrength,
                preferences.getInt(
                    KeyVirtualizerStrengthPref,
                    DefaultVirtualizerStrength,
                ),
            )

            putBoolean(
                AudioEffectProtocol.KeyLoudnessAvailable,
                true,
            )
            putBoolean(
                AudioEffectProtocol.KeyLoudnessEnabled,
                preferences.getBoolean(KeyLoudnessEnabledPref, false),
            )
            putInt(
                AudioEffectProtocol.KeyLoudnessGainMb,
                preferences.getInt(KeyLoudnessGainPref, DefaultLoudnessGainMb),
            )

            // Always expose a usable spatial mode. Native Android Spatializer
            // state is reported when present; otherwise Muse's PCM widening DSP
            // provides compatibility mode for ordinary stereo tracks.
            putBoolean(AudioEffectProtocol.KeySpatialSupported, true)
            putBoolean(AudioEffectProtocol.KeySpatialAvailable, true)
            putBoolean(
                AudioEffectProtocol.KeySpatialEnabled,
                softwareSpatialEnabled || spatial.enabled,
            )
            putBoolean(
                AudioEffectProtocol.KeySpatialCompatibilityMode,
                compatibilityMode,
            )
            putInt(
                AudioEffectProtocol.KeySpatialWidth,
                preferences.getInt(KeySpatialWidthPref, DefaultSpatialWidth),
            )
            putBoolean(
                AudioEffectProtocol.KeyHeadTrackerAvailable,
                spatial.headTrackerAvailable,
            )
        }
    }

    private fun restoreValues() {
        val storedLevels = preferences.getString(KeyEqLevelsPref, null)
            ?.split(',')
            ?.mapNotNull(String::toIntOrNull)
            ?.toIntArray()
            ?: IntArray(MuseSoftwareAudioProcessor.BandCentersHz.size)
        softwareProcessor.setLevels(storedLevels)

        val spatialEnabled = preferences.getBoolean(
            KeySpatialEnabledPref,
            false,
        )
        val spatialWidth = preferences.getInt(
            KeySpatialWidthPref,
            DefaultSpatialWidth,
        )
        softwareProcessor.setSpatialEnabled(spatialEnabled)
        softwareProcessor.setSpatialWidth(spatialWidth)

        val bassEnabled = preferences.getBoolean(
            KeyBassEnabledPref,
            false,
        )
        val bassStrength = preferences
            .getInt(KeyBassStrengthPref, DefaultBassStrength)
            .coerceIn(0, MaxBassStrength)
        softwareProcessor.setBassEnabled(bassEnabled)
        softwareProcessor.setBassStrength(bassStrength)

        val virtualizerEnabled = preferences.getBoolean(
            KeyVirtualizerEnabledPref,
            false,
        )
        val virtualizerStrength = preferences
            .getInt(KeyVirtualizerStrengthPref, DefaultVirtualizerStrength)
            .coerceIn(0, 1000)
        softwareProcessor.setVirtualizerEnabled(virtualizerEnabled)
        softwareProcessor.setVirtualizerStrength(virtualizerStrength)

        val loudnessEnabled = preferences.getBoolean(
            KeyLoudnessEnabledPref,
            false,
        )
        val loudnessGain = preferences
            .getInt(KeyLoudnessGainPref, DefaultLoudnessGainMb)
            .coerceIn(0, MaxLoudnessGainMb)
        softwareProcessor.setLoudnessEnabled(loudnessEnabled)
        softwareProcessor.setLoudnessGainMb(loudnessGain)
    }

    private fun setEqualizerBand(
        bandIndex: Int,
        requestedLevelMb: Int,
    ) {
        if (bandIndex !in MuseSoftwareAudioProcessor.BandCentersHz.indices) return
        softwareProcessor.setBandLevel(
            bandIndex,
            requestedLevelMb.coerceIn(
                MuseSoftwareAudioProcessor.MinLevelMb,
                MuseSoftwareAudioProcessor.MaxLevelMb,
            ),
        )
        persistEqualizerLevels()
    }

    private fun useEqualizerPreset(presetIndex: Int) {
        val presets = listOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0),
            intArrayOf(350, 260, 120, 0, -80, -120, -150),
            intArrayOf(-120, -80, 60, 280, 340, 160, -60),
            intArrayOf(-140, -80, 0, 80, 160, 320, 420),
            intArrayOf(520, 380, 180, 0, -80, 40, 120),
        )
        val preset = presets.getOrNull(presetIndex) ?: return
        softwareProcessor.setLevels(preset)
        persistEqualizerLevels()
    }

    private fun applyEqualizerProfile(
        savedCentersHz: IntArray,
        savedLevelsMb: IntArray,
    ) {
        val mappedLevels = SoundProfileBandMapper.mapLevels(
            savedCentersHz = savedCentersHz,
            savedLevelsMb = savedLevelsMb,
            targetCentersHz = MuseSoftwareAudioProcessor.BandCentersHz,
            minimumLevelMb = MuseSoftwareAudioProcessor.MinLevelMb,
            maximumLevelMb = MuseSoftwareAudioProcessor.MaxLevelMb,
        )
        softwareProcessor.setLevels(mappedLevels)
        persistEqualizerLevels()
    }

    private fun persistEqualizerLevels() {
        preferences.edit().putString(
            KeyEqLevelsPref,
            softwareProcessor.currentLevels().joinToString(","),
        ).apply()
    }

    private fun applyEnabledState() {
        val processingEnabled = masterEnabled && !bypass

        softwareProcessor.setProcessingEnabled(
            enabled = masterEnabled,
            isBypassed = bypass,
        )
        softwareProcessor.setBassEnabled(
            processingEnabled &&
                preferences.getBoolean(KeyBassEnabledPref, false),
        )
        softwareProcessor.setVirtualizerEnabled(
            processingEnabled &&
                preferences.getBoolean(KeyVirtualizerEnabledPref, false),
        )
        softwareProcessor.setLoudnessEnabled(
            processingEnabled &&
                preferences.getBoolean(KeyLoudnessEnabledPref, false),
        )

        // Keep vendor AudioFX instances disabled. Muse uses one consistent PCM
        // DSP path so OEM availability cannot grey-out controls or double-apply
        // gain/effects on some phones.
        runCatching { bassBoost?.enabled = false }
        runCatching { virtualizer?.enabled = false }
        runCatching { loudnessEnhancer?.enabled = false }
    }

    private fun spatialCapability(): SpatialCapability {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return SpatialCapability()
        }
        return spatialCapabilityApi33()
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun spatialCapabilityApi33(): SpatialCapability {
        val audioManager = context.getSystemService(AudioManager::class.java)
            ?: return SpatialCapability()
        val spatializer = audioManager.spatializer
        val supported =
            spatializer.immersiveAudioLevel !=
                android.media.Spatializer.SPATIALIZER_IMMERSIVE_LEVEL_NONE

        return SpatialCapability(
            supported = supported,
            available = supported && spatializer.isAvailable,
            enabled = supported && spatializer.isEnabled,
            headTrackerAvailable =
                supported && spatializer.isHeadTrackerAvailable,
        )
    }

    private fun releaseEffects() {
        listOf(bassBoost, virtualizer, loudnessEnhancer).forEach { effect ->
            runCatching { effect?.enabled = false }
            runCatching { effect?.release() }
        }
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }

    private data class SpatialCapability(
        val supported: Boolean = false,
        val available: Boolean = false,
        val enabled: Boolean = false,
        val headTrackerAvailable: Boolean = false,
    )

    private companion object {
        const val PreferencesName = "muse_audio_effects"

        const val KeyMasterEnabledPref = "master_enabled"
        const val KeyBypassPref = "bypass"
        const val KeyEqLevelsPref = "eq_levels"
        const val KeyBassEnabledPref = "bass_enabled"
        const val KeyBassStrengthPref = "bass_strength"
        const val KeyVirtualizerEnabledPref = "virtualizer_enabled"
        const val KeyVirtualizerStrengthPref = "virtualizer_strength"
        const val KeyLoudnessEnabledPref = "loudness_enabled"
        const val KeyLoudnessGainPref = "loudness_gain_mb"
        const val KeySpatialEnabledPref = "spatial_enabled"
        const val KeySpatialWidthPref = "spatial_width"

        const val DefaultBassStrength = 350
        const val DefaultVirtualizerStrength = 350
        const val DefaultLoudnessGainMb = 0
        const val DefaultSpatialWidth = 680

        // BassBoost accepts 0..1000, but Muse intentionally caps ordinary UI control
        // below the platform maximum to avoid aggressive gain changes.
        const val MaxBassStrength = 700

        // 600 mB = +6 dB. Keep the user-facing loudness enhancer conservative.
        const val MaxLoudnessGainMb = 600
    }
}
