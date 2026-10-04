package com.rezoxnemesis.muse.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.museDataStore by preferencesDataStore(name = "muse_preferences")
private val FavoriteTrackIds = stringSetPreferencesKey("favorite_track_ids")
private val RecentTrackIds = stringPreferencesKey("recent_track_ids")
private val PlaylistsJson = stringPreferencesKey("playlists_json")
private val ManagedMediaUris = stringSetPreferencesKey("managed_media_uris")
private val SoundProfilesJson = stringPreferencesKey("sound_profiles_json")
private val SelectedSoundProfileId = stringPreferencesKey("selected_sound_profile_id")
private val PlayCountsJson = stringPreferencesKey("play_counts_json")
private val ListeningSignalsJson = stringPreferencesKey("listening_signals_json")

data class UserPlaylist(
    val id: String,
    val name: String,
    val trackIds: List<Long>,
)

data class MuseSoundProfile(
    val id: String,
    val name: String,
    val eqCentersHz: List<Int>,
    val eqLevelsMb: List<Int>,
    val bassEnabled: Boolean,
    val bassStrength: Int,
    val virtualizerEnabled: Boolean,
    val virtualizerStrength: Int,
    val loudnessEnabled: Boolean,
    val loudnessGainMb: Int,
)

data class MuseBackupSummary(
    val favorites: Int,
    val recentTracks: Int,
    val playlists: Int,
    val soundProfiles: Int,
)

class MusePreferences(
    private val context: Context,
) {
    val favoriteIds: Flow<Set<Long>> = context.museDataStore.data.map { prefs ->
        prefs[FavoriteTrackIds].orEmpty().mapNotNull(String::toLongOrNull).toSet()
    }

    val recentTrackIds: Flow<List<Long>> = context.museDataStore.data.map { prefs ->
        prefs[RecentTrackIds]
            .orEmpty()
            .split(',')
            .mapNotNull(String::toLongOrNull)
    }

    val playlists: Flow<List<UserPlaylist>> = context.museDataStore.data.map { prefs ->
        decodePlaylists(prefs[PlaylistsJson].orEmpty())
    }

    val managedMediaUris: Flow<Set<String>> = context.museDataStore.data.map { prefs ->
        prefs[ManagedMediaUris].orEmpty()
    }

    val soundProfiles: Flow<List<MuseSoundProfile>> = context.museDataStore.data.map { prefs ->
        decodeSoundProfiles(prefs[SoundProfilesJson].orEmpty())
    }

    val selectedSoundProfileId: Flow<String?> = context.museDataStore.data.map { prefs ->
        prefs[SelectedSoundProfileId]
            ?.takeIf { it.isNotBlank() }
    }

    val playCounts: Flow<Map<Long, Int>> = context.museDataStore.data.map { prefs ->
        decodePlayCounts(prefs[PlayCountsJson].orEmpty())
    }

    val listeningSignals: Flow<Map<Long, MuseListeningSignal>> =
        context.museDataStore.data.map { prefs ->
            decodeListeningSignals(prefs[ListeningSignalsJson].orEmpty())
        }

    suspend fun exportBackupJson(): String {
        val prefs = context.museDataStore.data.first()

        val favorites = prefs[FavoriteTrackIds]
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .filter { it != 0L }
            .distinct()

        val recent = prefs[RecentTrackIds]
            .orEmpty()
            .split(',')
            .mapNotNull(String::toLongOrNull)
            .filter { it != 0L }
            .distinct()
            .take(100)

        val playlists = decodePlaylists(
            prefs[PlaylistsJson].orEmpty(),
        )
        val profiles = decodeSoundProfiles(
            prefs[SoundProfilesJson].orEmpty(),
        )
        val selectedProfileId = prefs[SelectedSoundProfileId]
            .orEmpty()
            .takeIf { selected ->
                profiles.any { it.id == selected }
            }
            .orEmpty()

        val playCounts = decodePlayCounts(
            prefs[PlayCountsJson].orEmpty(),
        )
        val listeningSignals = decodeListeningSignals(
            prefs[ListeningSignalsJson].orEmpty(),
        )

        return JSONObject()
            .put("schema", BackupSchemaVersion)
            .put("kind", "muse-local-backup")
            .put("favorites", JSONArray(favorites))
            .put("recentTracks", JSONArray(recent))
            .put("playlists", JSONArray(encodePlaylists(playlists)))
            .put("soundProfiles", JSONArray(encodeSoundProfiles(profiles)))
            .put("selectedSoundProfileId", selectedProfileId)
            .put("playCounts", JSONObject().apply {
                playCounts.forEach { (trackId, count) ->
                    put(trackId.toString(), count)
                }
            })
            .put(
                "listeningSignals",
                JSONObject(encodeListeningSignals(listeningSignals)),
            )
            .toString(2)
    }

    suspend fun restoreBackupJson(
        raw: String,
    ): MuseBackupSummary {
        require(raw.length <= MaxBackupChars) {
            "Muse backup is too large."
        }

        val root = JSONObject(raw)
        require(root.optString("kind") == "muse-local-backup") {
            "This is not a Muse backup."
        }
        require(root.optInt("schema", -1) == BackupSchemaVersion) {
            "Unsupported Muse backup version."
        }

        val favorites = root
            .optJSONArray("favorites")
            .toLongList()
            .filter { it != 0L }
            .distinct()

        val recent = root
            .optJSONArray("recentTracks")
            .toLongList()
            .filter { it != 0L }
            .distinct()
            .take(100)

        val playlists = decodePlaylists(
            root.optJSONArray("playlists")
                ?.toString()
                .orEmpty(),
        )

        val profiles = decodeSoundProfiles(
            root.optJSONArray("soundProfiles")
                ?.toString()
                .orEmpty(),
        )
        val selectedProfileId = root
            .optString("selectedSoundProfileId")
            .takeIf { selected ->
                selected.isNotBlank() &&
                    profiles.any { it.id == selected }
            }
            .orEmpty()

        val playCounts = decodePlayCountsObject(
            root.optJSONObject("playCounts"),
        )
        val listeningSignals = decodeListeningSignalsObject(
            root.optJSONObject("listeningSignals"),
        )

        context.museDataStore.edit { prefs ->
            prefs[FavoriteTrackIds] = favorites
                .map(Long::toString)
                .toSet()
            prefs[RecentTrackIds] = recent.joinToString(",")
            prefs[PlaylistsJson] = encodePlaylists(playlists)
            prefs[SoundProfilesJson] = encodeSoundProfiles(profiles)
            prefs[SelectedSoundProfileId] = selectedProfileId
            prefs[PlayCountsJson] = encodePlayCounts(playCounts)
            prefs[ListeningSignalsJson] = encodeListeningSignals(listeningSignals)
        }

        return MuseBackupSummary(
            favorites = favorites.size,
            recentTracks = recent.size,
            playlists = playlists.size,
            soundProfiles = profiles.size,
        )
    }

    suspend fun addManagedMediaUri(uri: String) {
        if (uri.isBlank()) return
        context.museDataStore.edit { prefs ->
            prefs[ManagedMediaUris] = prefs[ManagedMediaUris].orEmpty() + uri
        }
    }

    suspend fun removeManagedMediaUri(uri: String) {
        context.museDataStore.edit { prefs ->
            prefs[ManagedMediaUris] = prefs[ManagedMediaUris].orEmpty() - uri
        }
    }

    suspend fun removeTrackReferences(trackId: Long) {
        context.museDataStore.edit { prefs ->
            val favoriteValue = trackId.toString()
            prefs[FavoriteTrackIds] = prefs[FavoriteTrackIds]
                .orEmpty()
                .filterNot { it == favoriteValue }
                .toSet()

            prefs[RecentTrackIds] = prefs[RecentTrackIds]
                .orEmpty()
                .split(',')
                .mapNotNull(String::toLongOrNull)
                .filterNot { it == trackId }
                .joinToString(",")

            val counts = decodePlayCounts(
                prefs[PlayCountsJson].orEmpty(),
            ).toMutableMap()
            counts.remove(trackId)
            prefs[PlayCountsJson] = encodePlayCounts(counts)

            val listeningSignals = decodeListeningSignals(
                prefs[ListeningSignalsJson].orEmpty(),
            ).toMutableMap()
            listeningSignals.remove(trackId)
            prefs[ListeningSignalsJson] = encodeListeningSignals(listeningSignals)

            val playlists = decodePlaylists(prefs[PlaylistsJson].orEmpty())
                .map { playlist ->
                    playlist.copy(
                        trackIds = playlist.trackIds.filterNot { it == trackId },
                    )
                }
            prefs[PlaylistsJson] = encodePlaylists(playlists)
        }
    }

    suspend fun saveSoundProfile(
        name: String,
        eqCentersHz: List<Int>,
        eqLevelsMb: List<Int>,
        bassEnabled: Boolean,
        bassStrength: Int,
        virtualizerEnabled: Boolean,
        virtualizerStrength: Int,
        loudnessEnabled: Boolean,
        loudnessGainMb: Int,
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return

        mutateSoundProfiles { current ->
            val existing = current.firstOrNull {
                it.name.equals(cleanName, ignoreCase = true)
            }
            val profile = MuseSoundProfile(
                id = existing?.id ?: UUID.randomUUID().toString(),
                name = cleanName,
                eqCentersHz = eqCentersHz,
                eqLevelsMb = eqLevelsMb,
                bassEnabled = bassEnabled,
                bassStrength = bassStrength,
                virtualizerEnabled = virtualizerEnabled,
                virtualizerStrength = virtualizerStrength,
                loudnessEnabled = loudnessEnabled,
                loudnessGainMb = loudnessGainMb,
            )
            if (existing == null) {
                current + profile
            } else {
                current.map { if (it.id == existing.id) profile else it }
            }
        }
    }

    suspend fun deleteSoundProfile(profileId: String) {
        context.museDataStore.edit { prefs ->
            val current = decodeSoundProfiles(
                prefs[SoundProfilesJson].orEmpty(),
            )
            prefs[SoundProfilesJson] = encodeSoundProfiles(
                current.filterNot { it.id == profileId },
            )
            if (prefs[SelectedSoundProfileId] == profileId) {
                prefs[SelectedSoundProfileId] = ""
            }
        }
    }

    suspend fun selectSoundProfile(profileId: String?) {
        context.museDataStore.edit { prefs ->
            val profiles = decodeSoundProfiles(
                prefs[SoundProfilesJson].orEmpty(),
            )
            prefs[SelectedSoundProfileId] = profileId
                ?.takeIf { selected ->
                    profiles.any { it.id == selected }
                }
                .orEmpty()
        }
    }

    suspend fun toggleFavorite(trackId: Long) {
        context.museDataStore.edit { prefs ->
            val current = prefs[FavoriteTrackIds].orEmpty().toMutableSet()
            val value = trackId.toString()
            if (!current.add(value)) current.remove(value)
            prefs[FavoriteTrackIds] = current
        }
    }

    suspend fun recordPlayed(trackId: Long) {
        context.museDataStore.edit { prefs ->
            val current = prefs[RecentTrackIds]
                .orEmpty()
                .split(',')
                .mapNotNull(String::toLongOrNull)
                .toMutableList()
            current.remove(trackId)
            current.add(0, trackId)
            prefs[RecentTrackIds] = current.take(100).joinToString(",")

            val counts = decodePlayCounts(
                prefs[PlayCountsJson].orEmpty(),
            ).toMutableMap()
            counts[trackId] = (counts[trackId] ?: 0)
                .plus(1)
                .coerceAtMost(Int.MAX_VALUE)
            prefs[PlayCountsJson] = encodePlayCounts(counts)
        }
    }

    suspend fun recordListeningOutcome(
        trackId: Long,
        completionFraction: Double,
    ) {
        if (trackId == 0L || !completionFraction.isFinite()) return

        context.museDataStore.edit { prefs ->
            val signals = decodeListeningSignals(
                prefs[ListeningSignalsJson].orEmpty(),
            ).toMutableMap()
            val current = signals[trackId] ?: MuseListeningSignal()
            signals[trackId] = current.record(completionFraction)
            prefs[ListeningSignalsJson] = encodeListeningSignals(signals)
        }
    }

    suspend fun createPlaylist(
        name: String,
        trackIds: List<Long> = emptyList(),
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        mutatePlaylists { current ->
            current + UserPlaylist(
                id = UUID.randomUUID().toString(),
                name = cleanName,
                trackIds = trackIds.distinct(),
            )
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        mutatePlaylists { current -> current.filterNot { it.id == playlistId } }
    }

    suspend fun renamePlaylist(playlistId: String, name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        mutatePlaylists { current ->
            current.map { playlist ->
                if (playlist.id == playlistId) playlist.copy(name = cleanName) else playlist
            }
        }
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: Long) {
        mutatePlaylists { current ->
            current.map { playlist ->
                if (playlist.id != playlistId || trackId in playlist.trackIds) playlist
                else playlist.copy(trackIds = playlist.trackIds + trackId)
            }
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: Long) {
        mutatePlaylists { current ->
            current.map { playlist ->
                if (playlist.id != playlistId) playlist
                else playlist.copy(trackIds = playlist.trackIds.filterNot { it == trackId })
            }
        }
    }

    suspend fun moveTrackInPlaylist(
        playlistId: String,
        fromIndex: Int,
        toIndex: Int,
    ) {
        mutatePlaylists { current ->
            current.map { playlist ->
                if (playlist.id != playlistId) return@map playlist
                if (
                    fromIndex !in playlist.trackIds.indices ||
                    toIndex !in playlist.trackIds.indices ||
                    fromIndex == toIndex
                ) {
                    return@map playlist
                }

                val reordered = playlist.trackIds.toMutableList()
                val moved = reordered.removeAt(fromIndex)
                reordered.add(toIndex, moved)
                playlist.copy(trackIds = reordered)
            }
        }
    }

    private fun encodePlayCounts(
        counts: Map<Long, Int>,
    ): String =
        JSONObject().apply {
            counts.forEach { (trackId, count) ->
                if (trackId != 0L && count > 0) {
                    put(trackId.toString(), count)
                }
            }
        }.toString()

    private fun decodePlayCounts(
        raw: String,
    ): Map<Long, Int> {
        if (raw.isBlank()) return emptyMap()
        return runCatching {
            decodePlayCountsObject(JSONObject(raw))
        }.getOrDefault(emptyMap())
    }

    private fun decodePlayCountsObject(
        source: JSONObject?,
    ): Map<Long, Int> {
        if (source == null) return emptyMap()
        return buildMap {
            source.keys().forEach { key ->
                val trackId = key.toLongOrNull()
                val count = source.optInt(key, 0)
                if (trackId != null && trackId != 0L && count > 0) {
                    put(trackId, count)
                }
            }
        }
    }

    private fun encodeListeningSignals(
        signals: Map<Long, MuseListeningSignal>,
    ): String =
        JSONObject().apply {
            signals.forEach { (trackId, signal) ->
                if (trackId != 0L && signal.observedSessions > 0) {
                    put(
                        trackId.toString(),
                        JSONObject()
                            .put("observed", signal.observedSessions)
                            .put("completed", signal.completedSessions)
                            .put("skipped", signal.skippedSessions)
                            .put(
                                "completionSum",
                                signal.completionFractionSum.coerceIn(
                                    0.0,
                                    signal.observedSessions.toDouble(),
                                ),
                            ),
                    )
                }
            }
        }.toString()

    private fun decodeListeningSignals(
        raw: String,
    ): Map<Long, MuseListeningSignal> {
        if (raw.isBlank()) return emptyMap()
        return runCatching {
            decodeListeningSignalsObject(JSONObject(raw))
        }.getOrDefault(emptyMap())
    }

    private fun decodeListeningSignalsObject(
        source: JSONObject?,
    ): Map<Long, MuseListeningSignal> {
        if (source == null) return emptyMap()
        return buildMap {
            source.keys().forEach { key ->
                val trackId = key.toLongOrNull()
                val item = source.optJSONObject(key)
                if (trackId == null || trackId == 0L || item == null) {
                    return@forEach
                }

                val observed = item.optInt("observed", 0)
                    .coerceAtLeast(0)
                if (observed <= 0) return@forEach

                val completed = item.optInt("completed", 0)
                    .coerceIn(0, observed)
                val skipped = item.optInt("skipped", 0)
                    .coerceIn(0, observed)
                val completionSum = item
                    .optDouble("completionSum", 0.0)
                    .takeIf(Double::isFinite)
                    ?.coerceIn(0.0, observed.toDouble())
                    ?: 0.0

                put(
                    trackId,
                    MuseListeningSignal(
                        observedSessions = observed,
                        completedSessions = completed,
                        skippedSessions = skipped,
                        completionFractionSum = completionSum,
                    ),
                )
            }
        }
    }

    private suspend fun mutateSoundProfiles(
        transform: (List<MuseSoundProfile>) -> List<MuseSoundProfile>,
    ) {
        context.museDataStore.edit { prefs ->
            val current = decodeSoundProfiles(prefs[SoundProfilesJson].orEmpty())
            prefs[SoundProfilesJson] = encodeSoundProfiles(transform(current))
        }
    }

    private fun encodeSoundProfiles(
        profiles: List<MuseSoundProfile>,
    ): String {
        val array = JSONArray()
        profiles.forEach { profile ->
            array.put(
                JSONObject()
                    .put("id", profile.id)
                    .put("name", profile.name)
                    .put("eqCentersHz", JSONArray(profile.eqCentersHz))
                    .put("eqLevelsMb", JSONArray(profile.eqLevelsMb))
                    .put("bassEnabled", profile.bassEnabled)
                    .put("bassStrength", profile.bassStrength)
                    .put("virtualizerEnabled", profile.virtualizerEnabled)
                    .put("virtualizerStrength", profile.virtualizerStrength)
                    .put("loudnessEnabled", profile.loudnessEnabled)
                    .put("loudnessGainMb", profile.loudnessGainMb)
            )
        }
        return array.toString()
    }

    private fun decodeSoundProfiles(
        raw: String,
    ): List<MuseSoundProfile> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    val name = item.optString("name")
                    if (id.isBlank() || name.isBlank()) continue

                    val centers = item.optJSONArray("eqCentersHz")
                        .toIntList()
                    val levels = item.optJSONArray("eqLevelsMb")
                        .toIntList()

                    add(
                        MuseSoundProfile(
                            id = id,
                            name = name,
                            eqCentersHz = centers,
                            eqLevelsMb = levels,
                            bassEnabled = item.optBoolean("bassEnabled", false),
                            bassStrength = item.optInt("bassStrength", 0),
                            virtualizerEnabled = item.optBoolean(
                                "virtualizerEnabled",
                                false,
                            ),
                            virtualizerStrength = item.optInt(
                                "virtualizerStrength",
                                0,
                            ),
                            loudnessEnabled = item.optBoolean(
                                "loudnessEnabled",
                                false,
                            ),
                            loudnessGainMb = item.optInt(
                                "loudnessGainMb",
                                0,
                            ),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun JSONArray?.toLongList(): List<Long> {
        val source = this ?: return emptyList()
        return buildList {
            for (index in 0 until source.length()) {
                val raw = source.opt(index)
                val value = when (raw) {
                    is Number -> raw.toLong()
                    is String -> raw.toLongOrNull()
                    else -> null
                }
                if (value != null) add(value)
            }
        }
    }

    private fun JSONArray?.toIntList(): List<Int> {
        val source = this ?: return emptyList()
        return buildList {
            for (index in 0 until source.length()) {
                val raw = source.opt(index)
                val value = when (raw) {
                    is Number -> raw.toInt()
                    is String -> raw.toIntOrNull()
                    else -> null
                }
                if (value != null) add(value)
            }
        }
    }

    private suspend fun mutatePlaylists(
        transform: (List<UserPlaylist>) -> List<UserPlaylist>,
    ) {
        context.museDataStore.edit { prefs ->
            val current = decodePlaylists(prefs[PlaylistsJson].orEmpty())
            prefs[PlaylistsJson] = encodePlaylists(transform(current))
        }
    }

    private fun encodePlaylists(playlists: List<UserPlaylist>): String {
        val array = JSONArray()
        playlists.forEach { playlist ->
            val tracks = JSONArray()
            playlist.trackIds.forEach { tracks.put(it) }
            array.put(
                JSONObject()
                    .put("id", playlist.id)
                    .put("name", playlist.name)
                    .put("tracks", tracks)
            )
        }
        return array.toString()
    }

    private fun decodePlaylists(raw: String): List<UserPlaylist> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val tracks = item.optJSONArray("tracks") ?: JSONArray()
                    val trackIds = buildList {
                        for (trackIndex in 0 until tracks.length()) {
                            val rawValue = tracks.opt(trackIndex)
                            val value = when (rawValue) {
                                is Number -> rawValue.toLong()
                                is String -> rawValue.toLongOrNull()
                                else -> null
                            }
                            if (value != null && value != 0L) add(value)
                        }
                    }
                    val id = item.optString("id")
                    val name = item.optString("name")
                    if (id.isNotBlank() && name.isNotBlank()) {
                        add(UserPlaylist(id = id, name = name, trackIds = trackIds))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
    private companion object {
        const val BackupSchemaVersion = 1
        const val MaxBackupChars = 2 * 1024 * 1024
    }

}
