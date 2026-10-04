package com.rezoxnemesis.muse.playback

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Bundle
import kotlin.math.abs
import kotlin.math.ln

class AudioEffectEngine(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )

    private var audioSessionId: Int = 0
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var masterEnabled: Boolean
        get() = preferences.getBoolean(KeyMasterEnabledPref, true)
        set(value) = preferences.edit().putBoolean(KeyMasterEnabledPref, value).apply()

    private var bypass: Boolean
        get() = preferences.getBoolean(KeyBypassPref, false)
        set(value) = preferences.edit().putBoolean(KeyBypassPref, value).apply()

    fun attach(sessionId: Int) {
        if (sessionId <= 0 || sessionId == audioSessionId) return

        releaseEffects()
        audioSessionId = sessionId

        equalizer = runCatching { Equalizer(0, sessionId) }.getOrNull()
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
            preferences.edit().putBoolean(
                KeyBassEnabledPref,
                args.getBoolean(AudioEffectProtocol.KeyBassEnabled),
            ).apply()
        }
        if (args.containsKey(AudioEffectProtocol.KeyBassStrength)) {
            val value = args.getInt(AudioEffectProtocol.KeyBassStrength)
                .coerceIn(0, MaxBassStrength)
            preferences.edit().putInt(KeyBassStrengthPref, value).apply()
            runCatching { bassBoost?.setStrength(value.toShort()) }
        }

        if (args.containsKey(AudioEffectProtocol.KeyVirtualizerEnabled)) {
            preferences.edit().putBoolean(
                KeyVirtualizerEnabledPref,
                args.getBoolean(AudioEffectProtocol.KeyVirtualizerEnabled),
            ).apply()
        }
        if (args.containsKey(AudioEffectProtocol.KeyVirtualizerStrength)) {
            val value = args.getInt(AudioEffectProtocol.KeyVirtualizerStrength)
                .coerceIn(0, 1000)
            preferences.edit().putInt(KeyVirtualizerStrengthPref, value).apply()
            runCatching { virtualizer?.setStrength(value.toShort()) }
        }

        if (args.containsKey(AudioEffectProtocol.KeyLoudnessEnabled)) {
            preferences.edit().putBoolean(
                KeyLoudnessEnabledPref,
                args.getBoolean(AudioEffectProtocol.KeyLoudnessEnabled),
            ).apply()
        }
        if (args.containsKey(AudioEffectProtocol.KeyLoudnessGainMb)) {
            val value = args.getInt(AudioEffectProtocol.KeyLoudnessGainMb)
                .coerceIn(0, MaxLoudnessGainMb)
            preferences.edit().putInt(KeyLoudnessGainPref, value).apply()
            runCatching { loudnessEnhancer?.setTargetGain(value) }
        }

        applyEnabledState()
        return snapshot()
    }

    fun snapshot(): Bundle {
        val eq = equalizer
        val bandCount = eq?.numberOfBands?.toInt() ?: 0
        val centers = IntArray(bandCount) { index ->
            runCatching {
                eq?.getCenterFreq(index.toShort())?.div(1000)
            }.getOrNull() ?: 0
        }
        val levels = IntArray(bandCount) { index ->
            runCatching {
                eq?.getBandLevel(index.toShort())?.toInt()
            }.getOrNull() ?: 0
        }
        val range = runCatching { eq?.bandLevelRange }.getOrNull()
        val presetNames = ArrayList<String>()
        val presetCount = runCatching { eq?.numberOfPresets?.toInt() }.getOrNull() ?: 0
        repeat(presetCount) { index ->
            presetNames += runCatching {
                eq?.getPresetName(index.toShort()).orEmpty()
            }.getOrDefault("Preset ${index + 1}")
        }

        return Bundle().apply {
            putBoolean(AudioEffectProtocol.KeySessionReady, audioSessionId > 0)
            putBoolean(AudioEffectProtocol.KeyMasterEnabled, masterEnabled)
            putBoolean(AudioEffectProtocol.KeyBypass, bypass)

            putBoolean(AudioEffectProtocol.KeyEqAvailable, eq != null)
            putIntArray(AudioEffectProtocol.KeyEqCentersHz, centers)
            putInt(
                AudioEffectProtocol.KeyEqMinMb,
                range?.getOrNull(0)?.toInt() ?: 0,
            )
            putInt(
                AudioEffectProtocol.KeyEqMaxMb,
                range?.getOrNull(1)?.toInt() ?: 0,
            )
            putIntArray(AudioEffectProtocol.KeyEqLevelsMb, levels)
            putStringArrayList(AudioEffectProtocol.KeyEqPresetNames, presetNames)

            putBoolean(AudioEffectProtocol.KeyBassAvailable, bassBoost != null)
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
                virtualizer != null,
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
                loudnessEnhancer != null,
            )
            putBoolean(
                AudioEffectProtocol.KeyLoudnessEnabled,
                preferences.getBoolean(KeyLoudnessEnabledPref, false),
            )
            putInt(
                AudioEffectProtocol.KeyLoudnessGainMb,
                preferences.getInt(KeyLoudnessGainPref, DefaultLoudnessGainMb),
            )
        }
    }

    private fun restoreValues() {
        val eq = equalizer
        if (eq != null) {
            val stored = preferences.getString(KeyEqLevelsPref, null)
                ?.split(',')
                ?.mapNotNull(String::toIntOrNull)
                .orEmpty()
            val range = runCatching { eq.bandLevelRange }.getOrNull()
            val minimum = range?.getOrNull(0)?.toInt() ?: 0
            val maximum = range?.getOrNull(1)?.toInt() ?: 0
            repeat(eq.numberOfBands.toInt()) { index ->
                val value = stored.getOrNull(index) ?: 0
                runCatching {
                    eq.setBandLevel(
                        index.toShort(),
                        value.coerceIn(minimum, maximum).toShort(),
                    )
                }
            }
        }

        val bassStrength = preferences
            .getInt(KeyBassStrengthPref, DefaultBassStrength)
            .coerceIn(0, MaxBassStrength)
        runCatching { bassBoost?.setStrength(bassStrength.toShort()) }

        val virtualizerStrength = preferences
            .getInt(KeyVirtualizerStrengthPref, DefaultVirtualizerStrength)
            .coerceIn(0, 1000)
        runCatching { virtualizer?.setStrength(virtualizerStrength.toShort()) }

        val loudnessGain = preferences
            .getInt(KeyLoudnessGainPref, DefaultLoudnessGainMb)
            .coerceIn(0, MaxLoudnessGainMb)
        runCatching { loudnessEnhancer?.setTargetGain(loudnessGain) }
    }

    private fun setEqualizerBand(
        bandIndex: Int,
        requestedLevelMb: Int,
    ) {
        val eq = equalizer ?: return
        if (bandIndex !in 0 until eq.numberOfBands.toInt()) return

        val range = runCatching { eq.bandLevelRange }.getOrNull() ?: return
        val minimum = range.getOrNull(0)?.toInt() ?: return
        val maximum = range.getOrNull(1)?.toInt() ?: return
        val level = requestedLevelMb.coerceIn(minimum, maximum)

        runCatching {
            eq.setBandLevel(bandIndex.toShort(), level.toShort())
        }.onSuccess {
            persistEqualizerLevels(eq)
        }
    }

    private fun useEqualizerPreset(presetIndex: Int) {
        val eq = equalizer ?: return
        if (presetIndex !in 0 until eq.numberOfPresets.toInt()) return
        runCatching {
            eq.usePreset(presetIndex.toShort())
        }.onSuccess {
            persistEqualizerLevels(eq)
        }
    }

    private fun applyEqualizerProfile(
        savedCentersHz: IntArray,
        savedLevelsMb: IntArray,
    ) {
        val eq = equalizer ?: return
        val range = runCatching { eq.bandLevelRange }.getOrNull() ?: return
        val minimum = range.getOrNull(0)?.toInt() ?: return
        val maximum = range.getOrNull(1)?.toInt() ?: return

        repeat(eq.numberOfBands.toInt()) { bandIndex ->
            val actualHz = runCatching {
                eq.getCenterFreq(bandIndex.toShort()) / 1000
            }.getOrNull()?.coerceAtLeast(1) ?: return@repeat

            val nearestSavedIndex = savedCentersHz.indices.minByOrNull { savedIndex ->
                val savedHz = savedCentersHz[savedIndex].coerceAtLeast(1)
                abs(ln(savedHz.toDouble() / actualHz.toDouble()))
            } ?: return@repeat

            val level = savedLevelsMb[nearestSavedIndex]
                .coerceIn(minimum, maximum)

            runCatching {
                eq.setBandLevel(
                    bandIndex.toShort(),
                    level.toShort(),
                )
            }
        }

        persistEqualizerLevels(eq)
    }

    private fun persistEqualizerLevels(eq: Equalizer) {
        val levels = buildList {
            repeat(eq.numberOfBands.toInt()) { index ->
                add(
                    runCatching {
                        eq.getBandLevel(index.toShort()).toInt()
                    }.getOrDefault(0)
                )
            }
        }
        preferences.edit().putString(
            KeyEqLevelsPref,
            levels.joinToString(","),
        ).apply()
    }

    private fun applyEnabledState() {
        val processingEnabled = masterEnabled && !bypass

        runCatching {
            equalizer?.enabled = processingEnabled
        }
        runCatching {
            bassBoost?.enabled = processingEnabled &&
                preferences.getBoolean(KeyBassEnabledPref, false)
        }
        runCatching {
            virtualizer?.enabled = processingEnabled &&
                preferences.getBoolean(KeyVirtualizerEnabledPref, false)
        }
        runCatching {
            loudnessEnhancer?.enabled = processingEnabled &&
                preferences.getBoolean(KeyLoudnessEnabledPref, false)
        }
    }

    private fun releaseEffects() {
        listOf(equalizer, bassBoost, virtualizer, loudnessEnhancer).forEach { effect ->
            runCatching { effect?.enabled = false }
            runCatching { effect?.release() }
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }

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

        const val DefaultBassStrength = 350
        const val DefaultVirtualizerStrength = 350
        const val DefaultLoudnessGainMb = 0

        // BassBoost accepts 0..1000, but Muse intentionally caps ordinary UI control
        // below the platform maximum to avoid aggressive gain changes.
        const val MaxBassStrength = 700

        // 600 mB = +6 dB. Keep the user-facing loudness enhancer conservative.
        const val MaxLoudnessGainMb = 600
    }
}
