package com.rezoxnemesis.muse.playback

import android.os.Bundle
import androidx.media3.session.SessionCommand

object AudioEffectProtocol {
    const val ActionGetState = "com.rezoxnemesis.muse.audio.GET_STATE"
    const val ActionUpdate = "com.rezoxnemesis.muse.audio.UPDATE"

    val GetStateCommand = SessionCommand(ActionGetState, Bundle.EMPTY)
    val UpdateCommand = SessionCommand(ActionUpdate, Bundle.EMPTY)

    const val KeySessionReady = "session_ready"
    const val KeyAudioSessionId = "audio_session_id"
    const val KeyMasterEnabled = "master_enabled"
    const val KeyBypass = "bypass"

    const val KeyEqAvailable = "eq_available"
    const val KeyEqCentersHz = "eq_centers_hz"
    const val KeyEqMinMb = "eq_min_mb"
    const val KeyEqMaxMb = "eq_max_mb"
    const val KeyEqLevelsMb = "eq_levels_mb"
    const val KeyEqBandIndex = "eq_band_index"
    const val KeyEqBandLevelMb = "eq_band_level_mb"
    const val KeyEqPresetIndex = "eq_preset_index"
    const val KeyEqPresetNames = "eq_preset_names"
    const val KeyEqProfileCentersHz = "eq_profile_centers_hz"
    const val KeyEqProfileLevelsMb = "eq_profile_levels_mb"

    const val KeyBassAvailable = "bass_available"
    const val KeyBassEnabled = "bass_enabled"
    const val KeyBassStrength = "bass_strength"

    const val KeyVirtualizerAvailable = "virtualizer_available"
    const val KeyVirtualizerEnabled = "virtualizer_enabled"
    const val KeyVirtualizerStrength = "virtualizer_strength"

    const val KeyLoudnessAvailable = "loudness_available"
    const val KeyLoudnessEnabled = "loudness_enabled"
    const val KeyLoudnessGainMb = "loudness_gain_mb"

    const val KeySpatialSupported = "spatial_supported"
    const val KeySpatialAvailable = "spatial_available"
    const val KeySpatialEnabled = "spatial_enabled"
    const val KeyHeadTrackerAvailable = "head_tracker_available"
}
