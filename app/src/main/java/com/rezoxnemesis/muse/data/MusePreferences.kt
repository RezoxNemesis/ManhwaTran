package com.rezoxnemesis.muse.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.museDataStore by preferencesDataStore(name = "muse_preferences")
private val FavoriteTrackIds = stringSetPreferencesKey("favorite_track_ids")
private val RecentTrackIds = stringPreferencesKey("recent_track_ids")
private val PlaylistsJson = stringPreferencesKey("playlists_json")
private val ManagedMediaUris = stringSetPreferencesKey("managed_media_uris")
private val SoundProfilesJson = stringPreferencesKey("sound_profiles_json")

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
        mutateSoundProfiles { current ->
            current.filterNot { it.id == profileId }
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
}
